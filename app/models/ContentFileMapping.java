package models;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class holds Mapping data helps in looking up for sameFiles and their objects.
 * @author himanshu
 *
 */
public class ContentFileMapping {

	/**
	 * Storing mapping for all files having same dataSize to datasize
	 */
	public static ConcurrentHashMap<Integer, CopyOnWriteArrayList<Long>> dataSizeToFileIdMapping = new ConcurrentHashMap<>();
	
	/**
	 * Storing mapping for all objects referencing a file after syncUp
	 */
	public static ConcurrentHashMap<Long, CopyOnWriteArrayList<Long>> fileIdToContentIdMapping = new ConcurrentHashMap<>();
	
}
