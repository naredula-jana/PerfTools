

# Test with varying Threads in Async

Test Setup :  Async Rest controller with varying number of threads.

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


# How many Threads to configure:
  - **Configured Threads are less then desired threads**: when the threads are less then desired, then thread will be congested and requests will be waiting in the queues, this will lead to increase in the latency. But the cpu consumption per request will be less or it will more cpu-effecient, and also context switches and futex will be less.
  - **Configured Threads are more then desired threads**: when threads are more then desired, latency will be more since there will more futex and context switches due to the thread going sleeping often. cpu consumption per request will be more  due to more context switch and futex call.
  - **To Configure Less thread then desired or More threads then desired?**: if threads are less then desired or more then desired, both lead to more latency and also lead more cpu cycles per request. but more threads then desired is better in latency then less threads then desired, **because the request in queue causes more latency increase then due to context switch/futex**. So it will best to have more threads then desired.
  - **How to get Best Cpu Effecicency or less cpu per request?.**: If  threads are configured less then desired leads to best cpu efficiency and it is well suitable for batch type of applications. The threads with best CPU effeciency may not be best in latency.
  - **How to get Best Latency?**: when thread are configured more then desired and each thread spins continously during idle time , then we will get best latency or minimum latency. but cpu consumption will be wasted due to cpu spinning or cpu effeciency will be less. here total number of threads should be less then total core in the case of spinning.
  - **How to configure correct number of threads for real-time or non-batch application?.**: 
    - Configure some number of threads for each type to start with, run the test with the maximum number of requests per second for that particular hardware. measure the cpu consumption of each type of thread(like rest controller, camel-worker threads , hysterix,..etc). If cpu consumption is close 100% then increase the number of threads for that particular type. If the cpu consumption is less like less then 60% then decrease the number of threads. Repeat this few number of times till threads are closer to the desired.
    - Threads in SEDA (Stage Event Driven Architecture): The desired number of threads in each stage of SEDA will be different, means the number of threads for tomcat, camel-worker, hysterix,..etc will be different. because the amount of computation spend in each stage will be different. The sum of all these event-loop threads from different stages should be less then total number of cores so that some cores can be left to other threads. Some of the dynamic threads like camel-aggregation threads,Hysterix-Timer trhreads are not counted in total because they are may not be event loop, so the actual total of all threads can be much higher then then total cores. the number of threads for each stage should be in the ratio of computation of stages. example: suppose ratio of computation between tomcat:camel:Hysterix is 1:3:1 and total core are 32, then 25 cores with one thread each can be allocated to event-loop and are divided amount tomcat, camel and hysterix in the ratio of 1:3:1, means 5 threads for tomcat, 15 threads for camel-worker and 5 threads for hysterix. and rest of 7 cores for non-event loop threads. 
   
  

