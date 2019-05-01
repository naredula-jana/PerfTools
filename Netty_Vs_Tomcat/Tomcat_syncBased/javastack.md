Hysterix thread executing blocking method from camel mean:

 ```
"hystrix-CamelHystrix-5" #393 daemon prio=5 os_prio=0 tid=0x00007fced0107800 nid=0x52b5 waiting on condition [0x00007fceb2ced000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000000f810a428> (a java.util.concurrent.CountDownLatch$Sync)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.doAcquireSharedInterruptibly(AbstractQueuedSynchronizer.java:997)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(AbstractQueuedSynchronizer.java:1304)
        at java.util.concurrent.CountDownLatch.await(CountDownLatch.java:231)
        at reactor.core.publisher.BlockingSingleSubscriber.blockingGet(BlockingSingleSubscriber.java:81)
        at reactor.core.publisher.Mono.block(Mono.java:1500)
        at CamelBean.appendSomeText1(CamelBean.java:24)
        at sun.reflect.GeneratedMethodAccessor86.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.camel.component.bean.MethodInfo.invoke(MethodInfo.java:481)
        at org.apache.camel.component.bean.MethodInfo$1.doProceed(MethodInfo.java:300)
        at org.apache.camel.component.bean.MethodInfo$1.proceed(MethodInfo.java:273)
        at org.apache.camel.component.bean.AbstractBeanProcessor.process(AbstractBeanProcessor.java:187)
        at org.apache.camel.component.bean.BeanProcessor.process(BeanProcessor.java:53)
        at org.apache.camel.component.bean.BeanProducer.process(BeanProducer.java:41)
        at org.apache.camel.processor.SendProcessor.process(SendProcessor.java:148)
        at org.apache.camel.processor.CamelInternalProcessor.process(CamelInternalProcessor.java:201)
        at org.apache.camel.processor.DelegateAsyncProcessor.process(DelegateAsyncProcessor.java:97)
        at org.apache.camel.component.hystrix.processor.HystrixProcessorCommand.run(HystrixProcessorCommand.java:114)
        at org.apache.camel.component.hystrix.processor.HystrixProcessorCommand.run(HystrixProcessorCommand.java:33)
        
```
