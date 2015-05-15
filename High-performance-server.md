There are few areas in which i would think for building a high performance server of our De-Duplicate object store. Our server should be:
Highly available
Highly concurrent
Highly efficient/ minimum processing time

To make it highly available, we need to create multiple nodes of our server and create a cluster of it, there should be a load balancer over it to distribute load to every node equally, while creating this cluster we should make nodes in various availability zones to make it more faster to respond via nearest node, plus also prevent it from hazards.
For high concurrency there are lot of things that can be done, some of them are as follow:
All API calls must be non-blocking, which means thread should not get blocked for delayed tasks. For e.g.: writing content to file and I/O. Use future and promise for achieving it.
Logic should be completely stateless and must not depend on some other API calls, if we need to maintain some state for a particular resource that should be done in a separate layer.
Have asynchronous behaviour wherever possible, like checking for duplicate data can be done asynchronously for every object.
Event driven model - If some task is triggered on fulfilment of some condition, avoid polling after specific interval to check for condition and then trigger task, implement event driven logic which triggers task on occurrence of event and uses call back to inform the completion or error in task.

For minimising processing time 
we can cache the data which is required very frequently. This can be done on separate layer by using some persistent in-memory database like reds.
Use better data-structures to reduce the time complexity for lookups and finding out relationships
Using efficient algorithm for matching content in memory with a file, by diving content in chunks and then process in parallel to find similarity.
Preprocess data as much as possible to reduce the time required in matching content. For e.g.: in current implementation i have grouped my files according to data size. but if we implement hashing over object content that can save a lot time complexity. 
Using relational-database, currently i have kept all meta-data in memory. It will be better if we create a relational data model for it. that will save a lot data-redundency and makes it persistent also.
