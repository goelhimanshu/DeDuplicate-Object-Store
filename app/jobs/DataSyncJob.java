package jobs;

import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Content;
import play.Play;
import controllers.Store;

public class DataSyncJob implements Runnable{

	/**
	 * @description it is internal method called by class methods to trigger syncData functionality for objects hilding data on memory
	 * This function triggers syncData on crossing load threshold value which is configurable
	 * It sync memory data to disk and also chekc for duplicate content and in case of duplicate object refres to same file.
	 */
	private static void checkLoad(){
		
		Long MAX_DATA_SIZE_IN_MEMORY = Play.application().configuration().getLong("maxDataSizeInMemory");		// take out memory size from configuration
		Float loadFactor = Float.parseFloat(Play.application().configuration().getString("loadFactor"));		// take load factor value from configuration
		Long thresholdDataSize = (long) (MAX_DATA_SIZE_IN_MEMORY*loadFactor);
		
		ExecutorService threadpool = Executors.newFixedThreadPool(10); 
		
		if(Store.currentDataSize >= thresholdDataSize){																// if current memory size crosses threshold trigger syncData method
			for(Entry<Long, Content> entry : Store.database.entrySet()){
				
				if(!entry.getValue().getIsSynced()){
					try {
						Content contentObject = entry.getValue();
						
						synchronized (contentObject) {
							Integer freeDataSize = threadpool.submit(contentObject).get();
							synchronized (Store.currentDataSize) {
								Store.currentDataSize = Store.currentDataSize - freeDataSize;									// update current memory allocated size
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	
	@Override
	public void run() {
		checkLoad();
	}
	
}
