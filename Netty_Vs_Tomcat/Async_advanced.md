


# 1) How many Threads to configure:


**Test Setup:**  The following is perf test with Async Rest controller , and with varying number of threads.

<table border=1>
<tr>
<th> Concurrent calls </th>
<th> Number of Threads </th>
<th>  Context switches and futex calls </th>
<th>  Latency and CPU utilization </th>
<th> Description </th>
</tr>
<tr>
<th> 1500 requests/sec </th>
<th> Threads: 2/8/16 </th>
<th>  Context switches: 13k/300k/400k </th>
<th>  Latency:  120ms/135ms/145ms <br> cpu:  200/300/400  </th>
<th> Threads 8,16: Threads are more then desired. <br> Threads-2: close to desired number. <br> Best latency for 2 threads. </th>
</tr>
<tr>
<th> 3000 requests/sec </th>
<th> Threads: 1/4/8/16 </th>
<th>  Context switches: <br> 12k/157k/400k/400k </th>
<th>  Latency:  400ms/219ms/260ms/300ms <br> cpu:  -  </th>
<th> Threads-8,16: These threads are more then desired. <br> Threads -1: The threads are less then desired.<br> Threads-4: These threads are close to desired number.<br>  Best latency for 4 threads. </th>
</tr>
</table>

  - **Configured Threads are less then desired threads**: when the threads are less then desired, then thread will be congested and requests will be waiting in the queues, this will lead to increase in the latency. But the cpu consumption per request will be less or it will more cpu-effecient, and also context switches and futex will be less.
  - **Configured Threads are more then desired threads**: when threads are more then desired, latency will be more since there will more futex and context switches due to the thread going sleeping often. cpu consumption per request will be more  due to more context switch and futex call.
  - **To Configure Less thread then desired or More threads then desired?**: if threads are less then desired or more then desired, both lead to more latency and also lead more cpu cycles per request. but more threads then desired is better in latency then less threads then desired, **because the request in queue causes more latency increase then due to context switch/futex**. So it will best to have more threads then desired.
  - **How to get Best Cpu Effeciency or less cpu per request?.**: If  threads are configured less then desired leads to best cpu efficiency and it is well suitable for batch type of applications. The threads with best CPU effeciency may not be best in latency.
  - **How to get Best Latency?**: when thread are configured more then desired and each thread spins continously during idle time , then we will get best latency or minimum latency. but cpu consumption will be wasted due to cpu spinning or cpu effeciency will be less. here total number of threads should be less then total core in the case of spinning.
  - **Load is not constant**: Since the load or number of request per second is not constant, so when the load is less it will be as good as configuring threads more then desired.
  - **How to configure correct number of threads for real-time or non-batch application?.**: 
    - Configure some number of threads for each type to start with, run the test with the maximum number of requests per second for that particular hardware. measure the cpu consumption of each type of thread(like rest controller, camel-worker threads , hysterix,..etc). If cpu consumption is close 100% then increase the number of threads for that particular type. If the cpu consumption is less like less then 60% then decrease the number of threads. Repeat this few number of times till threads are closer to the desired.
    - Threads in SEDA (Stage Event Driven Architecture): The desired number of threads in each stage of SEDA will be different, means the number of threads for tomcat, camel-worker, hysterix,..etc will be different. because the amount of computation spend in each stage will be different. The sum of all these event-loop threads from different stages should be less then total number of cores so that some cores can be left to other threads. Some of the dynamic threads like camel-aggregation threads,Hysterix-Timer trhreads are not counted in total because they are may not be event loop, so the actual total of all threads can be much higher then then total cores. the number of threads for each stage should be in the ratio of computation of stages. example: suppose ratio of computation between tomcat:camel:Hysterix is 1:3:1 and total core are 32, then 25 cores with one thread each can be allocated to event-loop and are divided amount tomcat, camel and hysterix in the ratio of 1:3:1, means 5 threads for tomcat, 15 threads for camel-worker and 5 threads for hysterix. and rest of 7 cores for non-event loop threads. 
  - **How to configure the number of threads in kubernetes POD **: The amount of cpu reserved for pod is not like vm or bare-metal. In the amount of cpu cores participating in the computation varies  but in vm and bare-metal it is fixed in number. so the number of threads to be configured will have different impact in POD when compare to vm/bare-metal.  TODO: The details of functioning of threads in POD is yet to explore.
    

# 2) Different types of Async or Degree of Async:
 
 Async threads also encounter blocking, but the blocking is more on cpu compuatation not on Input/Output(IO), this is one of the key difference between sync and Async. But Async threads  encounters waiting to various degree. The following section describes different types of Async threads  and also difference among them.
 
 - **Sync** : **Blocking on IO and dedicate the thread for each request till end of request** : If the thread block on IO(Input/Ouput) then it is called Sync.  If thread does not block on IO it is called Async thread.
 - **Async-1** : **Blocking on another Async thread till partial computation of a request is completed**: These threads does not block on IO but block or wait for very short period of time for the completion to be completed on another Async threads. Async-1 threads are much efficient and faster when compared to Sync threads.
 - **Async-2** : **Block only if there is zero request in the queue**: These thread does not block on IO and also does not wait or block on another async thread but they go to sleep when there are no requests in the input queue. Usually Async-2 threads are communicated using SEDA architecture. Async-2 thread will enter into waiting state if there are no request present in the input queue, due to this it enter's in to the kernel using futex or epoll. Async-2 threads are faster then Async-1 thread and also more cpu efficient.
 - **Async-3** : **It never Block even if there are no requests**: Async-3 does not wait for other thread like Async-1 and also  it also does not wait inside the OS for new request like Async-2. They have a dedicated cpu core and continously spinning. so the futex and context switches will be zero.
 - **Difference in performance**: 
    -  Sync threads are bad in latency aswell as cpu consumption per request when compared to all, this due to blocking on IO and dedicated thread for each request.
    -  Async-1 is better then Sync by 5.4x from cpu computation point of view. All the Perf results shared are related to Async-1.
    -  Async-2 is better then Async-1 in terms of latency aswell as cpu computation. 
    -  Async-3 threads are best of all in terms of latency due to zero futex calls and context switches.
 
 
## 2.1)Async-1 :
In the case of Rest-camel applications, the communication between threads are as follows:
 - **Flow of Request** :**Rest-controller-thread -> Camel-worker-thread -> rx-client** : rx-client can be http, cassendra, redis ,..etc.
 - **Async-1 working**:  When a rest request comes to rest-controller thread, it takes the request and submits to the camel-worker threads, here the rest controller thread is waiting for the camel thread(async thread) to complete the cpu computation and return the mono object. when Rest controller thread waits for the Camel-worker thread the call stacks looks like the below. Here http-request write happens in the context of camel-worker thread, and  http-request read will happens in the context of rest-controller thread using the help of Mono object. Here Rest controller thread is using Camel blocking producer.  
 - **Why Sync is very slow when compare to Async-1**:  In the Sync case, the rest controller thread will be waiting till camel-worker thread completes the write and read. read operation is big blocking call. due to this camel-worker aswell as Rest controller can serve only one request at any time till the request completes, Due to this Async-1 is 5.4X times more cpu-efficient when compare to Sync thread.  

```
"reactor-http-epoll-3" #15 daemon prio=5 os_prio=0 tid=0x00007fc0290e9000 nid=0x556c waiting on condition [0x00007fc014690000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000000f3c0f608> (a java.util.concurrent.CountDownLatch$Sync)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.doAcquireSharedInterruptibly(AbstractQueuedSynchronizer.java:997)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(AbstractQueuedSynchronizer.java:1304)
        at java.util.concurrent.CountDownLatch.await(CountDownLatch.java:231)
        at org.apache.camel.processor.MulticastProcessor.doProcessParallel(MulticastProcessor.java:378)
        at org.apache.camel.processor.MulticastProcessor.process(MulticastProcessor.java:246)
        at org.apache.camel.processor.CamelInternalProcessor.process(CamelInternalProcessor.java:201)
        at org.apache.camel.processor.Pipeline.process(Pipeline.java:138)
        at org.apache.camel.processor.Pipeline.process(Pipeline.java:101)
        at org.apache.camel.processor.CamelInternalProcessor.process(CamelInternalProcessor.java:201)
        at org.apache.camel.component.direct.DirectBlockingProducer.process(DirectBlockingProducer.java:53) :::: BLOCKING CAMEL PRODUCER
        at org.apache.camel.processor.SharedCamelInternalProcessor.process(SharedCamelInternalProcessor.java:186)
        at org.apache.camel.processor.SharedCamelInternalProcessor.process(SharedCamelInternalProcessor.java:86)
        at org.apache.camel.impl.ProducerCache$1.doInProducer(ProducerCache.java:541)
        at org.apache.camel.impl.ProducerCache$1.doInProducer(ProducerCache.java:506)
        at org.apache.camel.impl.ProducerCache.doInProducer(ProducerCache.java:369)
        at org.apache.camel.impl.ProducerCache.sendExchange(ProducerCache.java:506)
        at org.apache.camel.impl.ProducerCache.send(ProducerCache.java:246)
        at org.apache.camel.impl.DefaultProducerTemplate.send(DefaultProducerTemplate.java:148)
        at org.apache.camel.impl.DefaultProducerTemplate.sendBody(DefaultProducerTemplate.java:156)
        at org.apache.camel.impl.DefaultProducerTemplate.sendBody(DefaultProducerTemplate.java:173)
        at org.apache.camel.impl.DefaultProducerTemplate.requestBody(DefaultProducerTemplate.java:305)
        at com.NettyServer.Netty_Example.getCamelMulticastlarge(Netty_Example.java:79)  :::: CAMEL-BEAN method from the application
        at sun.reflect.GeneratedMethodAccessor45.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.springframework.web.reactive.result.method.InvocableHandlerMethod.lambda$invoke$0(InvocableHandlerMethod.java:139)
        at org.springframework.web.reactive.result.method.InvocableHandlerMethod$$Lambda$509/772318757.apply(Unknown Source)
        at reactor.core.publisher.FluxFlatMap.trySubscribeScalarMap(FluxFlatMap.java:141)
        at reactor.core.publisher.MonoFlatMap.subscribe(MonoFlatMap.java:53)
        at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
        at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.drain(MonoIgnoreThen.java:153)
        at reactor.core.publisher.MonoIgnoreThen.subscribe(MonoIgnoreThen.java:56)
        at reactor.core.publisher.MonoPeekFuseable.subscribe(MonoPeekFuseable.java:74)
        at reactor.core.publisher.MonoPeekFuseable.subscribe(MonoPeekFuseable.java:74)
        at reactor.core.publisher.MonoOnErrorResume.subscribe(MonoOnErrorResume.java:44)
        at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:150)
        at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:67)
        at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:76)
        at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.innerNext(FluxConcatMap.java:275)
        at reactor.core.publisher.FluxConcatMap$ConcatMapInner.onNext(FluxConcatMap.java:849)
        at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:121)
        at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2070)
        at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:162)
        at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.set(Operators.java:1878)
        at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onSubscribe(Operators.java:1752)
        at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:90)
        at reactor.core.publisher.MonoJust.subscribe(MonoJust.java:54)
        at reactor.core.publisher.MonoMapFuseable.subscribe(MonoMapFuseable.java:59)
        at reactor.core.publisher.Mono.subscribe(Mono.java:3695)
        at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.drain(FluxConcatMap.java:442)
        at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.onSubscribe(FluxConcatMap.java:212)
        at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:139)
        at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:63)
        at reactor.core.publisher.FluxConcatMap.subscribe(FluxConcatMap.java:121)
        at reactor.core.publisher.MonoNext.subscribe(MonoNext.java:40)
        at reactor.core.publisher.MonoSwitchIfEmpty.subscribe(MonoSwitchIfEmpty.java:44)
        at reactor.core.publisher.MonoFlatMap.subscribe(MonoFlatMap.java:60)
        at reactor.core.publisher.MonoFlatMap.subscribe(MonoFlatMap.java:60)
        at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
        at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
        at reactor.core.publisher.MonoOnErrorResume.subscribe(MonoOnErrorResume.java:44)
        at reactor.core.publisher.MonoOnErrorResume.subscribe(MonoOnErrorResume.java:44)
        at reactor.core.publisher.MonoPeekTerminal.subscribe(MonoPeekTerminal.java:61)
        at reactor.core.publisher.MonoOnErrorResume.subscribe(MonoOnErrorResume.java:44)
        at reactor.core.publisher.Mono.subscribe(Mono.java:3695)
        at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.drain(MonoIgnoreThen.java:172)
        at reactor.core.publisher.MonoIgnoreThen.subscribe(MonoIgnoreThen.java:56)
        at reactor.core.publisher.MonoPeekFuseable.subscribe(MonoPeekFuseable.java:70)
        at reactor.core.publisher.MonoPeekTerminal.subscribe(MonoPeekTerminal.java:61)
        at reactor.netty.http.server.HttpServerHandle.onStateChange(HttpServerHandle.java:64)
        at reactor.netty.tcp.TcpServerBind$ChildObserver.onStateChange(TcpServerBind.java:226)
        at reactor.netty.http.server.HttpServerOperations.onInboundNext(HttpServerOperations.java:434)
        at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:141)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
        at reactor.netty.http.server.HttpTrafficHandler.channelRead(HttpTrafficHandler.java:160)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
        at io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:438)
        at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:323)
        at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:297)
        at io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:253)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
        at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1434)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
        at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:965)
        at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:799)
        at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:433)
        at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:330)
        at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:897)
        at java.lang.Thread.run(Thread.java:745)

```


## 2.2)Async-2 :
 In the case of Async-2, Rest controller thread will be using non blocking camel producer, it sends the request to the consumer (i.e camel-worker thread) with out expecting any return value, based on the consumer output the rest controller process at later time without blocking. Due to this the number of futex calls will be less and also it can process more number of requests.  so the Performance of Async-2 will be better then Async-1.
 
 **TODO: developing Async-2 sample code and getting perf numbers.**
 
## 2.3)Async-3 : 
 It is similar Async-2, but when threads wait for the events it does not goes in to OS for sleep instead it polls for the events by spinning continously. Due to continous cpu spinning it starts the new request very fast and overall latency will be best of all the cases. For Async-3, Camel and Netty need some changes.
   
# 3) Hysterix Function issue in Async: 
 Hysterix  is calculating the time till the camel bean method completes, but in Async case, the http-request does not complete inside the camel bean method instead it does in lazy way, means it just write the http request and read in the rest controller context as mentioned in Async-1 and Async-2, so Hysterix thread stops the timer when the camel-bean returns the mono object, means Hysterix thread is not checking the output of http. this need to fixed accordingly or Hysterix is not suitable in async environment.
 
 **TODO: Solution for this issue need to be captured. **
 
# 4) Tomcat-RX vs Netty Rest Controller
**TODO :  More detail need to be captured**.


  

