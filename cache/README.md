# cache
there are 3 implementation of cache here:
* ConcurrentHashMapCacheImpl - based on ConcurrentHashMap and used as a reference implementation for behaviour and benchmarking the speed
* SynchronizedCaacheImpl - that simply used a HashMap and synchronizes access to it 
* BucketCacheImpl - the uses a strategy of pre-creating the map keys and allocating Bucket objects as values, these buckets will be synchronized on in an attempt to minimise contention

