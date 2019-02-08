# Reactive programming vs synchronised 
Reactive programming is also called Event based or asynchronise programming model. A reactive system is an architectural style that allows multiple individual requests to be processed by a single thread for each cpu core.  Synchronised is the alternate  programming model, where each request is handled by a individual thread.  Reactive programming is having advantage if number concurrent requests to the server is large and processing of each request need lot of IO or sleeps/waits during the processing. Reactive programming model is relative new in Java world, But in the linux kernel the epoll and AsyncIO is been introduced or supported more then decade back. Reactive programming is implemented on the foundations of epoll and asyncIO to reduce the system calls overhead. system call are expensive and can impact latency as-well throughput. 

In this Perf Test, Comparison between a Netty server a Asynchronous model vs Tomacat  a Synchronous model is compared.  Netty uses few number of threads proportional to the number of cpu cores  to process large number of requests. On the Other hand Tomcat uses separate thread for each request. Suppose if there are 2000 concurrent requests on 32 core machine  then Netty uses around 32 active threads vs Tomcat uses 2000 active threads to processes the requests. Due to this if the number  of request are large and each request need lot of waits during processing of thread like waiting for database response then Netty Performs well in terms of latency as well as throughput. Tomcat spends lot of cpu cycles in context switches. Netty has its own memory allocator for buffers, it doesn't waste memory bandwidth by filling buffers with zeros, Netty implements a jemalloc variant of memory allocation by bypassing jvm GC. 




## Papers related to syscall impact on performance:
 -   [syscall impact in high end NoSQL database](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/HighThroughputDatabaseForBigData.pdf) .
 -   [Minimising syscall : Golang apps in ring-0](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/GolangAppInRing0.pdf).
  -   [Netty uses Jemalloc varient for memory allocation](https://github.com/naredula-jana/Jiny-Kernel/blob/master/doc/malloc_paper_techpulse_submit_final.pdf).
 
