## Perf Tools for Java and C applications
Perf tool Measures the following :
1. **IPC (Instruction Per Cycle)** : more details ipc in this [paper](../master/docs/Perf_IPC.pdf).
2. **lock contention ** : lock contention from system or OS view. contention is measured using fadvise system call.
3. **Cpu cache  Usage ** : L1 cache misses.
4. **context switches  ** : context switches for each thread in the app.
5. ** pagefaults, TLB misses,.. etc ** :  All these values for each thread.


## Papers:
 -   [Page cache optimizations for Hadoop/HDFS, published and presented in open cirrus-2011 summit](../master/docs/PageCache-Open-Cirrus.pdf) .
 -   [User space Memory optimization techniques](../master/docs/malloc_paper_techpulse_submit_final.pdf).
 -   [Golang apps in ring-0](../master/docs/GolangAppInRing0.pdf).
 -   [Perf tool to measure the speed of vm/app](../master/docs/Perf_IPC.pdf).

## Related Projects:
 -[Vmstate](https://github.com/naredula-jana/vmstate): Virtualmachine state capture and analysis.
 -[JinyKernel](https://github.com/naredula-jana/Jiny-Kernel): Jiny-Kernel.
