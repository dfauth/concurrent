# cache
there are 3 implementation of cache here:
* ConcurrentHashMapCacheImpl - based on ConcurrentHashMap and used as a reference implementation for behaviour and benchmarking the speed
* SynchronizedCacheImpl - that simply used a HashMap and synchronizes access to it 
* BucketCacheImpl - the uses a strategy of pre-creating the map keys and allocating Bucket objects as values, these buckets are the mutex used to synchronise access. Doing it this way allows a degree of control over the contention by changing the number of buckets

The PerformanceTest class provides some degree of comparison of the speed for random keys:

`12:32:45.371 [main] INFO com.github.dfauth.concurrent.cache.PerformanceTest - ConcurrentHashMapCacheImpl completed in 3.818953 msec`

`12:32:45.377 [main] INFO com.github.dfauth.concurrent.cache.PerformanceTest - SynchronizedCacheImpl completed in 4.03513 msec`

`12:32:45.378 [main] INFO com.github.dfauth.concurrent.cache.PerformanceTest - BucketCacheImpl completed in 3.529672 msec`

BucketCacheImpl provides comparable performance to both using ConcurrentHashMap and simply synchronizing on a HashMap, although the results vary greatly

