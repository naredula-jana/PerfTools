# Async vs Synchronous programming
Reactive programming is also called Event based or Asynchronese programming model. A reactive system is an architectural style that allows multiple individual requests to be processed by a single NIO thread for each cpu core.  Synchronous is the alternate  programming model, where each request is handled by a individual thread.  Reactive programming is having advantage if number concurrent requests to the server is large and processing of each request need lot of IO or sleeps/waits during the processing. Reactive programming model is relative new in Java world, But in the linux kernel the epoll and AsyncIO is been introduced or supported more then decade back. Reactive programming is implemented on the foundations of epoll and asyncIO to reduce the system calls overhead. system call are expensive and can impact latency as-well throughput. During system call, cpu executes less number of instruction per second,[ more detail are here ](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/Perf_IPC.pdf) 

In this Perf Test, Comparison between  Asynchronous Rest Server(based on Netty) vs  Synchronous Server(Tomcat) is compared.  Async uses few number of threads proportional to the number of cpu cores  to process large number of requests. On the Other hand Sync uses separate thread for each request. Suppose if there are 2000 concurrent requests on 32 core machine  then Async uses around 32 active threads vs Sync uses atleast 4000 active threads to processes the requests. Due to this if the number  of request are large and each request need lot of waits during processing of thread like waiting for database response then Async Performs well in terms of latency as well as throughput. Sync spends lot of cpu cycles in context switches and futex system calls. Async(Netty) has its own memory allocator for buffers, it doesn't waste memory bandwidth by filling buffers with zeros, Netty implements a jemalloc variant of memory allocation by bypassing jvm GC. 


![Architecture Sys Vs Async](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/arch.jpg)

#  Threading Model in Sync Vs Async
There are Two types of threads:
 - **Async thread pool**: The number of threads created are proportional to the cpu cores, the computation needed for the request SHOULD be async , means non-blocking computation should be used , If there is any blocking computation then thread will be delayed in processing the subsequent requests in the queue this causes starvation of threads and huge latency will be experienced.  
 - **Sync thread pool**: In this thread  the computation in the request are assumbed to be blocking in nature, Here the thread will be attached to the request till the end of the request. The number of threads created in Sync  are propotional to max concurrent request at any point of time.
 - Load = Number of Request(R) X number of endpoints per camel route(E)
    - R = number of Concurrent requests.
    - E = Number of endpoints in the camel route.
 - Following are different threads:
    - RestController: IO  threads: These threads are Async threads pool both in Async as well as latest Tomcat versions.
    - RestController: Exec threads: These threads are present only in Tomcat, In Async these threads are removed.
    - Camel-Worker threads: These threads does the task of endpoints in route, they are IO intensive if Hysterix is not enabled, otherwise IO is execute in Hysterix thread context.
    - Camel Aggregator threads:
    - Hysterix Threads:
    - Hysterix Timer threads:


<table border=1>
<tr>
<th>Name</th>
<th>Sync Model:  Type of Thread/Number of threads </th>
<th>Async Model:  Type of Thread/Number of threads</th>
<th> Description </th>
</tr>
<tr>
<th align=left> 1.1)Rest Controller-IO <br> Tomcat/Netty </th>
<th> <ul align=left><li> Async Threads</li><li> 6 threads</li></ul> </th>
<th><ul align=left><li> Async Threads</li><li> 6 threads</li></ul></th>
<th> These threads are per cpu and does socket io. These threads are more IO intensive. </th>
</tr>
<tr>
<th align=left>1.2)Rest Controller-Exec  <br>  Tomcat/Netty </th>
<th> <ul align=left><li> Sync Threads</li><li> R threads </li></ul></th>
<th> 0 threads </th>
<th> These threads executes business logic of the rest controller. </th>
</tr>
<tr> 
<th align=left>2.1) Camel-Worker <br> Camel/Camel-Rx/Camel-Netty </th>
<th>  <ul align=left><li> Sync Threads</li><li> (E*R) threads </li></ul>  </th>
<th><ul align=left><li> Async Threads</li><li> 2 threads</li></ul></th>
<th> These threads are more IO intensive when hysterix is not enabled. </th>
</tr>
<tr>
<th align=left>2.2) Camel-Aggregator threads  <br> Camel/Camel-Rx/Camel-Netty </li></ul></th>
<th> <ul align=left><li> Sync Threads</li><li> (0-R) threads</li></ul> </th>
<th> <ul align=left><li> (0-R) threads </li></ul> </th>
<th> These threads are Not IO intensive and more like a control threads. These thread manages each route. </th>
</tr>
<tr>
<th align=left>3.1) Hysterix threads</th>
<th>  <ul align=left><li> Sync Threads</li><li> (E*R) threads </li></ul>  </th>
<th> <ul align=left><li> Async Threads</li><li> 2 threads</li></ul> </th>
<th>  These threads are IO intensive. </th>
</tr>
<th align=left>3.2) Hysterix Timer threads</th>
<th> <ul align=left><li> (0-R) threads </li></ul> </th>
<th><ul align=left><li> (0-R) threads</li></ul> </th>
<th>  These threads are not  IO intensive. </th>
</tr>
<tr>
<th>Total threads needed without Hysterix  </th>
<th> (tomcat)+ E*R(camel-worker) +R(camel-aggregator) = (2+E)*R  </th>
<th> 8 to 8+(0-R)</th>
<th> Example Perf Case: Sync Vs Async threads: 2400 vs 68 threads  </th>
</tr>
<tr>
<th>Total threads needed with Hysterix  </th>
<th>    (3+2*E)*R </th>
<th> 10 to 10+2*(0-R)</th>
<th> Example Perf Case: Sync Vs Async threads: 3200 vs 68 threads  </th>
</tr>
</table>


# Summary of Perf Tests :

 - **Test Setup**:
    - load generator -> Rest Server -> echo server. 
    - "ab" is used for load generator.
    -  Rest server can be Sync or Async server.
    - Camel and hysterix version used = 2.22
 - **Type of Perf Tests**: There are three type of Perf tests, first is just with Rest controller , second is Rest controller and Camel and third with Hysterix.
    1. **Only Rest Controller**:  Rest controller without camel directly calls web client to send the rest requests to the echo server.
    1. **Rest Controller + Camel**:
        -  **With Camel + Large Multicast**: [Large Multicast route for Async](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/Netty_eventBased/src/main/resources/application-context.xml) having 7 endpoints for Async and similar [Large Multicast route](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/Tomcat_syncBased/src/main/resources/application-context.xml) for Sync are used for comparing.  Here there will be 7 camel worker threads and  one aggregator thread per route in Sync platform.
        -  **With Camel + Small Multicast**: [Small Multicast route](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/Netty_eventBased/src/main/resources/application-context.xml) having 2 endpoints for Async and [Similar route is used for Sync](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/Tomcat_syncBased/src/main/resources/application-context.xml). Here there will be 2 camel worker threads and one aggregator thread per request in Sync platform. For Async there will only 2 camel worker for all the requests.
    1. **Rest Controller + Camel + Hysterix**:   [Hysterix route with the name multicastlargehysterix](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/Netty_eventBased/src/main/resources/application-context.xml) is used for Async and similar for Sync, In Async platform there will be 2 Hysterix threads serving all the requests, On Sync platform there will 7 Hysterix threads per request.


<table border=1>
<tr>
<th>Test-Description</th>
<th> parameters </th>
<th>Sync Platform</th>
<th>Async Platform</th>
<th>Async Improvement over Sync </th>
</tr>
<tr>
<th align=left width=auto>1)Without-Camel  </th>
<th width=auto> concurrency= 1800 </th>
<th width=auto>latency=191ms, cpu=840 </th>
<th width=auto>latency=157ms, cpu=480 </th>
<th width=auto>CPU: 1.75X  </th>
</tr>

<tr>
<th align=left width=auto>2)With-Camel + LargeMulticast  </th>
<th width=auto>   concurrency= 300 </th>
<th width=auto>cpu: 1400   user: 17 sys: 16</th>
<th width=auto>cpu: 360 user: 11% sys: 5%   latency: 196</th>
<th width=auto>CPU: 3.8X  </th>
</tr>

<tr>
<th align=left width=auto>3)With-Camel + LargeMulticast </th>
<th width=auto>   concurrency= 800  </th>
<th width=auto>cpu: 1900   user:21  sys:26 latency=750ms </th>
<th width=auto>cpu: 350 user:10.5% sys:4.5%   latency: 523</th>
<th width=auto> CPU: 5.4X </th>
</tr>

<tr>
<th align=left width=auto>4)With-Camel + LargeMulticast </th>
<th width=auto>   concurrency= 1200 </th>
<th width=auto>Breakdown due to large number of threads </th>
<th width=auto>cpu: 310 user:10.5% sys:4.5%   latency: 600</th>
<th width=auto> CPU: >5.4X </th>
</tr>

<tr>
<th align=left width=auto>5)With-Camel + SmallMulticast</th>
<th width=auto>    concurrency= 300 </th>
<th width=auto> cpu: 960 user: 15 sys:13.4 </th>
<th width=auto>cpu: 400 user: user:10% sys:6% latency: 85 </th>
<th width=auto>CPU:2.4X  </th>
</tr> 

<tr>
<th align=left width=auto>6)With-Camel + SmallMulticast  </th>
<th width=auto>   concurrency= 800 </th>
<th width=auto> cpu:1400  user:21  sys:19 latency:165 </th>
<th width=auto>cpu:450  user:11  sys:7% latency:220  </th>
<th width=auto> CPU: 3.1X </th>
</tr>

<tr>
<th align=left width=auto>7)With-Camel + LargeMulticast +Hysterix  </th>
<th width=auto>   concurrency= 100, netty worker=4, camel worker=2 </th>
<th width=auto>without hysterix:cpu:800 (threads:2088) latency:61 withhysetrix:   </th>
<th width=auto> </th>
<th width=auto>  CPU: >5.4X  </th>
</tr>

<tr>
<th align=left width=auto>8)With-Camel + LargeMulticast +Hysterix  </th>
<th width=auto>   concurrency= 300, netty worker=4, camel worker=2 </th>
<th width=auto>without hysterix: cpu:1200 withhysetrix: BREAKDOWN (unable to create threads  > 3400)  </th>
<th width=auto> without hysterix:cpu=330 , with hysterix: cpu=400  </th>
<th width=auto>  CPU: >6.0X  </th>
</tr>

<tr>
<th align=left width=auto>9)With-Camel + LargeMulticast +Hysterix  </th>
<th width=auto>   concurrency=800, netty worker=4, camel worker=2 </th>
<th width=auto>BREAKDOWN  </th>
<th width=auto> without hysterix:cpu=330, latency=381(connect=5ms) , with hysterix:cpu=400, latency=621(connect=200ms), hysetrix threads=40</th>
<th width=auto>   CPU: >6.0X  </th>
</tr>

<tr>
<th align=left width=auto>10)With-Camel + Pipeline +Hysterix  </th>
<th width=auto>   concurrency=800 </th>
<th width=auto> TODO </th>
<th width=auto>TODO  </th>
<th width=auto>  </th>
</tr>

<tr>
<th align=left width=auto>11)With-Camel + LargeMulticas + rx-camel on Sync-tomcat </th>
<th width=auto>  concurrency=300 </th>
<th width=auto>Hybrid: cpu:500 LargeMultiCast:1500 concurrency=300  </th>
<th width=auto>  </th>
<th width=auto> CPU: 3.0X </th>
</tr>
</table>

# Load  vs Performance Gap
As load increases, the Gap between Async and Sync increases. Sync Collapses at load of 1200/sec for large multicast. Here load refers to number of concurrent request and number of endpoints in the camel multicast route. As the load increases the following thing happens for Async and Sync servers:

**Async model**:  Since total threads stays constant irrespective of load. Firstly , the amount of idle time decreases per thread as load increases, so context switches decreases. secondly as consumer threads especially camel thread will be serving more requests before going to sleep, means the number of the producer(camel aggregator) wake-up's become less, means number of futex calls decreases, or average cpu cycles spend for IPC decreases. 
  - **Context switches**: As load increases the context switch will decrease since less amount of idle time.
  - **IPC**: As load increases, the producer may not required wake up the consumer since consumer is processing the preveous request. 

**Sync model**:  As the load increases the total number of active threads increase, context switches increases proportionally. As the request passing from producer(camel-aggregator) to consumer(camel-threads) increases so the fuxex call increases. This is exactly opposite to Async.
  - **Context switches**: As active threads increases the context switches increases proportionally in Sync.
  - **IPC**: irrespective of load, the producer need to pick free thread from the free pool to assign the request, means it need to wake up the free thread before assignment. so as load increases the futex calls increases proportionally.


In summary, as load increases the efficiency of IPC and context switches decreases in Sync model, on other hand for Async the efficiency of context switches and IPC increases, this makes the gap widen as load increases.  

<table border=1>
<tr>
<th>Description</th>
<th>Sync: Tomcat</th>
<th>Async: Netty</th>
<th> Threads Ratio Vs Performance </th>
</tr>
<tr>
<th>Total threads for LargeMulticast with 300 request without hysterix </th>
<th> (2+E)*R = 2700  </th>
<th> 6+2+60 = 68 </th>
<th> 2700 / 68 vs 3.0X </th>
</tr>
<tr>
<th>Total threads for SmallMulticast with 800 request without hysterix </th>
<th> (2+E)*R =(2+2)*800= 3200  </th>
<th> 6+2+60 = 68 </th>
<th>  3200 / 68 vs 3.1X </th>
</tr>
<tr>
<th>Total threads for LargeMulticast with 800 request </th>
<th> (2+E)*R= (2+7)*800 = 7200  </th>
<th> 6+2+(30-60) = 38-68</th>
<th> 7200 / 68 vs 5.4X  </th>
</tr>
<tr>
<th>Total threads for SmallMulticast with 300 request without hysterix </th>
<th> (2+E)*R = 1200  </th>
<th> 6+2+60 = 68 </th>
<th>  1200 / 68 vs 2.4X </th>
</tr>
</table>

 **Analysis of Test Results**

- Large Multicast as more performance gap between Async and Sync because of threads gap between them is more.
- Small Multicast as less performance gap because threads gaps is small.
- Number of threads directly proportional to performance Gap.


 
# Advanced Optimizations in Async:

1. **Allocating Number of Async Threads**: Async threads should be created based on the load and computation need per each request. Throughput means number of requests can be served per unit of cpu core.  Throughput and latency are directly propotional, means as load increased the through and latency increases. As latency crosses particular limit the number threads should be increased or requests rate should be decreased, otherwise the latency may be high. This can be acheived by the load balancer like HaProxy. HaProxy can send periodic ping request to check the latency, accordingly it can route the requests. 
1. **Pinning Thread to cpu**: pinning the cpu cores for camel worker threads and rest controller threads. these are highly io intensive, so by pinning the threads it improves the cpu efficiency further.
1. **Making zero context switches and zero futex calls by Spinning and pinning**: camel worker threads will be sleeping during the time of no request. Zero context switches and zero futex calls inside tomcat/camel can be acheived by the following means: a) Tomcat and  camel threads need to spin continously instead of sleeping inside the kernel. b) Using dedicated and pinned cores only for camel-worker and tomcat threads. remaining threads are assigned to left over cores.   by doing this it improves the latency and throughput. on other hand it waste the cpu cycles by spinning and may increase the power consumption, this optimization is well suited for high end machines with the dedicated cores.
1. **Improving the efficiency of Hysterix Timer threads and Camel Aggregator threads**: Need to investigate further to decrease the number of threads.


# Summary :

 - **Root Cause and Solution**:
    - **Root Cause**:  In synchronisation programming, there will be large number of concurrent threads proportional to number of requests and each thread handles one request before going to sleep. Due to this reason there will be lot of futex system calls and context switches leading to lot of cpu cycles consumption and increase in latency.
    - **Solution**: By making architecture Async using Non blocking IO(NIO) threads and creating threads based on number cores and not based requests will solve the above problems.
 - **The performance gap between Sync and Async**:  As load increases the gap between sync and async increases, the threads in Async becomes more efficient as load increases, on the other side for sync the threads becomes less efficient as load increases till it collapses.
 - **Advanced optimizations**: Advanced optimization improve further by tunning and changing some part of opensource  librarires.

## Papers related to Async performance:
 -   [syscall impact in high end NoSQL database](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/HighThroughputDatabaseForBigData.pdf) .
 -   [Minimising syscall : Golang apps in ring-0](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/GolangAppInRing0.pdf).
  -  [Netty uses Jemalloc variant for memory allocation](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/malloc_paper_techpulse_submit_final.pdf).
  -  [Async-Netty Vs sync-Tomcat performance results](https://www.slideshare.net/brendangregg/rxnetty-vs-tomcat-performance-results).
  -  [Java Perf Tool Used to measure the metrics](https://github.com/naredula-jana/PerfTools/tree/master/java_perf)
  -  [Why systemcall make cpu slower](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/Perf_IPC.pdf)

 
