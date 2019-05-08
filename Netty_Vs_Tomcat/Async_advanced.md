

# Test with varying Threads in Async

Test Setup :  Async Rest controller with Netty with varying number of threads.

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
<th> Threads 8,16: Threads are more then desired. <br> Threads-2: close to desired number. <br> Best performance for 2 threads. </th>
</tr>
<tr>
<th> 3000 requests/sec </th>
<th> Threads: 1/4/8/16 </th>
<th>  Context switches: <br> 12k/157k/400k/400k </th>
<th>  Latency:  400ms/219ms/260ms/300ms <br> cpu:  -  </th>
<th> Threads-8,16: The threads are more then desired. <br> Threads -1: The threads are less then desired.<br> Threads-4: very close the optimial number of threads.<br>  Best performance for 4 threads. </th>
</tr>
</table>


# How many Threads to configure:
  - **Configured Threads are less then desired threads**: when the threads are less then desired, then thread will be congested and requests will be waiting in the queues, this will lead to increase in the latency. But the cpu consumption per request will be less or it will more cpu-effecient, context switches and futex will be less.
  - **Configured Threads are more then desired threads**: when threads are more then desired, latency will be more since there will more futex and context switches due to the thread going sleeping often. cpu consumption per request will be more  due to more context switch and futex call.
  - **Less thread or More threads**: if threads are less then desired or more then desired, both lead to more latency and also lead more cpu cycles per request. but more threads then desired is better then less threads then desired.
  - **How to get Best Latency?**: when thread are configured more then desired and each thread spins continously during idle time , then we will get best latency or minimum latency. but cpu consumption will be wasted due to cpu spinning. here total number of threads should be less then core in this special configuration.
  - **How to configure correct number of threads?.**: 
    - configure some number of threads for each type, run the test with the maximum number of requests per second for that particular hardware. measure the cpu consumption of each type of thread(like rest controller, camel-worker threads , hysterix,..etc). If cpu consumption is close 100% then increase the number of threads for that particular type. If the cpu consumption is less like less then 60% then decrease the number of threads. Repeat this few number of times till threads are closer to the desired.
  

