# Async programming vs Synchronised 
Reactive programming is also called Event based or Asynchronise programming model. A reactive system is an architectural style that allows multiple individual requests to be processed by a single NIO thread for each cpu core.  Synchronised is the alternate  programming model, where each request is handled by a individual thread.  Reactive programming is having advantage if number concurrent requests to the server is large and processing of each request need lot of IO or sleeps/waits during the processing. Reactive programming model is relative new in Java world, But in the linux kernel the epoll and AsyncIO is been introduced or supported more then decade back. Reactive programming is implemented on the foundations of epoll and asyncIO to reduce the system calls overhead. system call are expensive and can impact latency as-well throughput. During system call, cpu executes less number of instruction,[ more detail are here ](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/Perf_IPC.pdf) 

In this Perf Test, Comparison between  Asynchronous Rest Server(based on Netty) vs  Synchronous Server(Tomcat) is compared.  Async uses few number of threads proportional to the number of cpu cores  to process large number of requests. On the Other hand Sync uses separate thread for each request. Suppose if there are 2000 concurrent requests on 32 core machine  then Async uses around 32 active threads vs Sync uses atleast 4000 active threads to processes the requests. Due to this if the number  of request are large and each request need lot of waits during processing of thread like waiting for database response then Async Performs well in terms of latency as well as throughput. Sync spends lot of cpu cycles in context switches and futex system calls. Async(Netty) has its own memory allocator for buffers, it doesn't waste memory bandwidth by filling buffers with zeros, Netty implements a jemalloc variant of memory allocation by bypassing jvm GC. 


![Architecture Sys Vs Async](https://github.com/naredula-jana/PerfTools/blob/master/Netty_Vs_Tomcat/arch.jpg)

# Summary of Tests :
Type of Tests:
- Test Setup: load generator -> Rest Server -> echo server.   "ab" is used for load generator, Rest server can Sync or Async server.
- Without Camel:  Rest controller directly calls web client to send the rest requests to the backend.
- With Camel + Large Multicast: Multicast route having 7 endpoints.  Here there will be 7 camel worker threads and  one aggregator thread per request.
- With Camel + Small Multicast: Multicast route having 2 endpoints. Here there will be 2 camel worker threads and one aggregator thread per request.
- With Camel + With Hysterix: TODO


<table border=1>
<tr>
<th>Test-Description</th>
<th>Sync: Tomcat</th>
<th>Async: Netty</th>
<th> Async Improvement over Sync </th>
</tr>
<tr>
<th>1)Without-Camel concurrency=1800 </th>
<th>latency=191ms, cpu=840 </th>
<th>latency=157ms, cpu=480 </th>
<th>CPU: 1.75X  </th>
</tr>

<tr>
<th>2)With-Camel LargeMulticast concurrency=300 </th>
<th>cpu: 1400   user: 17 sys: 16</th>
<th>cpu: 360 user: 11% sys: 5%   latency: 196</th>
<th>CPU: 3.8X  </th>
</tr>

<tr>
<th>3)With-Camel LargeMulticast concurrency=800 </th>
<th>cpu: 1900   user:21  sys:26 latency=750ms </th>
<th>cpu: 350 user:10.5% sys:4.5%   latency: 523</th>
<th> CPU: 5.4X </th>
</tr>

<tr>
<th>4)With-Camel LargeMulticast concurrency=1200 </th>
<th>Breakdown due to large number of threads </th>
<th>cpu: 310 user:10.5% sys:4.5%   latency: 600</th>
<th> CPU: >5.4X </th>
</tr>

<tr>
<th>5)With-Camel SmallMulticast concurrency=300 </th>
<th> cpu: 960 user: 15 sys:13.4 </th>
<th>cpu: 400 user: user:10% sys:6% latency: 85 </th>
<th>CPU:2.4X  </th>
</tr> 

<tr>
<th>6)With-Camel SmallMulticast concurrency=800 </th>
<th> cpu:1400  user:21  sys:19 latency:165 </th>
<th>cpu:450  user:11  sys:7% latency:220  </th>
<th> CPU: 3.1X </th>
</tr>
<tr>
<th>7)With-Camel LargeMulticas vs Hybrid on Sync concurrency=300 </th>
<th>Hybrid: cpu:500 LargeMultiCast:1500 concurrency=300  </th>
<th>  </th>
<th> CPU: 3.0X </th>
</tr>
</table>

#  Thread analysis in Sync Vs Async
- concurrent requests(R) = 300 or 800
- endpoints per large-multicast route(E)  = 7
- endpoints per small-multicast route(E)  = 2
- number of cores = 8 

<table border=1>
<tr>
<th>Description</th>
<th>Sync: Tomcat</th>
<th>Async: Netty</th>
<th> Description </th>
</tr>
<tr>
<th align=left>1) Rest Controller</th>
<th> R threads </th>
<th>6</th>
<th> These threads are more IO intensive. </th>
</tr>
<tr>
<th align=left>2) Camel_worker</th>
<th> (E*R) threads  </th>
<th>2</th>
<th> These threads are more IO intensive. </th>
</tr>
<tr>
<th align=left>3) Camel Aggregator threads</th>
<th> (R) </th>
<th> 0 to R </th>
<th> These threads are less IO intensive and more control threads. </th>
</tr>
<tr>
<th align=left>4) Hysterix threads</th>
<th> R </th>
<th> o to R </th>
<th>  These threads are less IO intensive and more control threads. </th>
</tr>
<tr>
<th>Total threads needed with camel and without hysterix </th>
<th> R(tomcat)+ E*R(camel-worker) +R(camel-aggregator) = (2+E)*R  </th>
<th> 8 to 8+R</th>
<th> </th>
</tr>
</table>

# Load  vs Performance Gap
As load increases, the Gap between Async and Sync increases. Sync Collapses at load of 1200/sec for large multicast. Here load refers to number of concurrent request and number of endpoints in the camel multicast route. As the load increases the following thing happens for Async and Sync servers:
- Async:  Since total threads stays constant irrespective of load. Firstly , the amount of idle time decreases per thread as load increases, so context switches decreases. secondly as consumer threads especially camel thread will be serving more requests before going to sleep, means the number of the producer(camel aggregator) wake-up's become less, means number of futex calls decreases, or average cpu cycles spend for IPC decreases. 
- Sync:  As the load increases the total number of active threads increase, context switches increases proportionally. As the request passing from producer(camel-aggregator) to consumer(camel-threads) increases so the fuxex call increases. This is exactly opposite to Async.

Context swicthes and IPC:
- Context switches: As active threads increases the context switches increases proportionally.
- IPC: suppose there are 1 request per sec in case-1 and 1000 request per second in case-2. in  case-1 the camel-worker thread will be sleeping for every request, so the camel aggregator thread need to wake up  the camel-worker thread for every request this contributes   , whereas in case-2, by the time camel-worker process one request, other request will be in the queue this avoids the futex call. on average instead of 1 futex call per request it will be something like 0.01 futex calls per request.

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

 Analysis of Test Results

- Large Multicast as more performance gap between Async and Sync because of threads gap between them is more.
- Small Multicast as less performance gap because threads gaps is small.


# Summary :

 - Root Cause and Impact: In synchronisation programming, there will be large number of concurrent threads proportional to number of requests and each thread handles one request before going to sleep. Due to this reason there will be lot of futex system calls and context switches leading to lot of cpu cycles consumption and increase in latency.
 - The performance gap between Sync and Async:  As load increases the gap between sync and async increases, the threads in Async becomes more efficient as load increases, on the other side for sync the threads becomes less efficient till it collapse. 
 
# Advanced Optimizations:

- pinning the cpu cores for camel worker threads and rest controller threads. these are highly io intensive, so by pinning the threads it improves the cpu efficiency further.
- camel worker threads will be sleeping if there is no request, instead of sleeping it can spin continuously in the user space, by doing this it improves the latency and throughput. on other hand it waste the cpu cycles by spinning, this optimisation is well suited for high end machines with the dedicated cores.
- Improving the efficiency of Hysterix and Camel Aggregator threads. Currently these threads are dynamic and created when there are requests.
- making zero context switches and zero futex calls:  by pinning thread to the cpu and spinning the thread during idle will achieve it, for this variable thread pool like camel-aggregator and hysterix need to make constant number of threads,  this improves the latency  and throughput to the maximum. This is suitable for highend servers.


## Papers related to syscall impact on performance:
 -   [syscall impact in high end NoSQL database](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/HighThroughputDatabaseForBigData.pdf) .
 -   [Minimising syscall : Golang apps in ring-0](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/GolangAppInRing0.pdf).
  -  [Netty uses Jemalloc variant for memory allocation](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/malloc_paper_techpulse_submit_final.pdf).
  -  [Async-Netty Vs sync-Tomcat performance results](https://www.slideshare.net/brendangregg/rxnetty-vs-tomcat-performance-results).
  -  [Java Perf Tool Used to measure the metrics](https://github.com/naredula-jana/PerfTools/tree/master/java_perf)
  -  [Why systemcall make cpu slower](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/Perf_IPC.pdf)

 
