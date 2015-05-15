# De-Duplicating-Object-Store
This is a implementation of de-duplicate object store, which provides REST API to add, read and delete objects from this store.
It is implemented over reliable file system for storing data on disk. 

### About Objects
1. Objects stored in this store are immutable and can have large size. 
2. While creating new entries objects are not checked for duplication and are stored in memory, but later on reaching a threashold value of size the data is synced with disk while checking for duplicate data.
3. Some meta-info about these objects are kept in memory, which help us perform the required operations faster.

### Meta Info in memory
1. Objects Ids for lookup
2. Storing mapping of fileNames grouped by dataSize - it will minimise the file to be matched for duplicate content.
3. Storing mapping of Objects Reference grouped by fileName they are pointing - it will help in getting info of all objects referencing a particular file (Required in delete operation). 
4. CurrentMemorySizeUsed - it will help trigger syncData opertaion on crossing a threashold value of data size.

### Implementation
1. On adding new object to store the object is saved on memory and currentMemorySizeUsed is incremented by object size. While adding it a background thread is triggered which checks for threashold value as per load factor from configuration and if it is crossed take action to sync data with disk for all objects which are not synced with disk.
2. While Retreiving data for an id, first object reference is retrieved from lookup table. If object is not synced then data from memory is returned, else it reads data from a file which is pointed by this object.
3. While deleting object for an id. If Object is not synced with disk then object's reference from lookup table is removed and object is also removed, else check the file pointed by this object. If that file is pointed by only this object remove file and object reference from lookup table. If file is pointed by other objects, remove that mapping and remove object refernce from lookup table.

### Object Data model
![alt tag](http://url/to/img.png)

# Technology & Framework used
This is implemented in Play framework V2.1.4 over Java OpenJDK V1.7 

# How to run it?
Step 1. Play framework is required to build this project. Use command 
```
play dist
```
Step 2. Extract .zip file and you will find a start file inside the extracted folder. Make this start file executable and start the process
```sh
bash start
```
