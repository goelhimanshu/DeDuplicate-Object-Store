package controllers;

import java.util.concurrent.ConcurrentHashMap;

import jobs.DataSyncJob;
import models.Content;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author himanshu
 * This class hold controllers to REST API for our store
 */
public class Store extends Controller {

	public static ConcurrentHashMap<Long, Content> database = new ConcurrentHashMap<>();									// for storing all ids and reference to there object for lookup
	public static Long currentDataSize = 0L;																				// holds size of data stored on memory at moment.
	private static Runnable dataSyncJob = new DataSyncJob();
	/**
	 * @return 200 Response with Json containing all(memory & disk) nodes of content object
	 */
	public static Result index(){
		return ok(Json.toJson(database));
	}
	
	/**
	 * Fetches out content data from request body and create a new object in memory which later might get moved to disk
	 * @return 200 response with id of created object
	 */
	public static Result update(){
		
		String data = request().body().asText();					// fetch out data from request Body
		
		Content obj = new Content(data);							// create object without check data duplication
		database.put(obj.getContentId(), obj);						// add reference of object in lookup map				
		currentDataSize += obj.getDataSize();						// update current memory allocated size
		
		Thread syncDataJob = new Thread(dataSyncJob);					// check for need to sync memory data with disk data
		syncDataJob.start();
		
		return ok(obj.getContentId().toString());					// return id of object created
	}
	
	/**
	 * @param id which is identifier of object stored 
	 * @return the data present on memory or on disk for referenced object
	 */
	public static Result read(long id){
		if(!database.containsKey(id)){
			return notFound();
		}
		return ok(database.get(id).getData());
	}
	
	/**
	 * @description it removes object reference to file and file in case it's the only object refering this file
	 * @param id which is identifier of object stored 
	 * @return 200 response on successful removal of data
	 */
	public static Result delete(long id){
		if(!database.containsKey(id)){
			return notFound();
		}
		
		Content obj = database.get(id);
		obj.removeAssociationWithFiles();
		database.remove(id);
		
		currentDataSize += obj.getDataSize();
		
		return ok("Deleted!!");
	}
		
}
