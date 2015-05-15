package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @description This class acts as a data model of Object for De-Duplicate store
 * @author himanshu
 */
public class Content implements Callable<Integer>{

	public static long lastCreatedObjectId = -1;
	
	private String data;
	private Long contentId;						// id of this object required for lookup
	private Integer dataSize;					// dataSize is length of data 
	private boolean isSynced;					// hold status whether data is synced to disk or still held in memory
	private Long fileId;						// id of file it is referencing after data sync to disk
		
	/**
	 * Parameterized constructor with default values
	 * @param data
	 */
	public Content(String data){
		this.data = data;
		this.contentId = ++lastCreatedObjectId;
		this.dataSize = data.length();
		this.isSynced = false;
		this.fileId = null;
	}
		
	public Boolean getIsSynced() {
		return this.isSynced;
	}

	public Long getFileId() {
		return this.fileId;
	}

	public Long getContentId() {
		return this.contentId;
	}
	
	/**
	 * @return data if sync not done else read file and return data
	 */
	public String getData() {
		if(this.data!=null){
			return this.data;
		}else{
			return getDataFromFile();
		}
	}
	
	public Integer getDataSize() {
		return this.dataSize;
	}

	
	/**
	 * @return string from file referenced by this object 
	 */
	private String getDataFromFile() {
		String data = "";
		
		try {
			Scanner br = new Scanner(new File("data/content_"+this.fileId+".txt"));
			data = br.nextLine();
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
				
		return data;
	}

	
	/**
	 * @description if any file with same dataSize is not present then create a new file and associate this object to that file.
	 * 				if some files has same dataSize then compare all that files with data of this object, till we get file content equals data of this object.
	 * 				if we don't get any file having content equals data of this object. 
	 * 				
	 * @return dataSize released from memory, after data sync for this object
	 */
	public Integer syncContent(){
		Integer freeDataSize = 0;
			
		if(!ContentFileMapping.dataSizeToFileIdMapping.containsKey(this.dataSize)){									//create new key in hashmap and store value if no data of same size present

			addFileToStorage();
			freeDataSize = this.dataSize;
			
			//define datasize and fileId mapping stored in memory for lookup
			CopyOnWriteArrayList<Long> filesList = new CopyOnWriteArrayList<>();
			filesList.add(this.fileId);
			ContentFileMapping.dataSizeToFileIdMapping.put(this.dataSize, filesList);
			
		}else{																									
			Iterator<Long> filesId = ContentFileMapping.dataSizeToFileIdMapping.get(this.dataSize).iterator();		// get list of files having same dataSize
			
			while(filesId.hasNext() && (this.fileId==null&&this.data!=null)){										// check for file until data matches with file content
				Long fileId = filesId.next();
				if(isSameDataInFile(fileId)){
					this.data = null;
					this.fileId = fileId;
					freeDataSize = this.dataSize;
							
					List<Long> fileAssociations = ContentFileMapping.fileIdToContentIdMapping.get(fileId);
					
					fileAssociations.add(this.contentId);
				}
			}
			
			if(this.data!=null && this.fileId==null){																// if no file matches data then create a new one 
				addFileToStorage();					
				freeDataSize = this.dataSize;
				ContentFileMapping.dataSizeToFileIdMapping.get(this.dataSize).add(this.fileId);						// add fileId to list containing fileIds of this size in dataSize mapping 
			}
		}
		this.isSynced = true;
		
		return freeDataSize;
	}

	
	/**
	 * @param destinationFileId is fileId with which data needs to be compared
	 * @return true if content of file is same as data of this object else false.
	 */
	private boolean isSameDataInFile(long destinationFileId){
		try {
			if(this.data==null)
				return false;
			
			BufferedReader destinationInputStream = new BufferedReader(new InputStreamReader(new FileInputStream("data/content_"+destinationFileId+".txt"),Charset.forName("UTF-8")));
			
			char[] dataChars = this.data.toCharArray();
			int charArrayIndex = 0;
			int charArrayLength = dataChars.length;
			
			int destinationStreamData = destinationInputStream.read();
			
			while(charArrayIndex<charArrayLength && destinationStreamData!=-1){
				if(dataChars[charArrayIndex]!=(char)destinationStreamData) {
					destinationInputStream.close();
					return false;
				}	
				charArrayIndex++;
				destinationStreamData = destinationInputStream.read();
			}
			
			destinationInputStream.close();
			
			if(charArrayIndex!=charArrayLength || destinationStreamData!=-1){
				return false;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @description it will create a new file on disk with contentId of this object and write data of this object to file
	 * it also maintain mapping of objects to this file.
	 */
	private void addFileToStorage(){
		FileOutputStream outFile = null;
		
		try {
			outFile = new FileOutputStream("data/content_"+this.contentId+".txt");
			
			outFile.write(this.data.getBytes());
			outFile.close();
			
			this.data=null;
			this.fileId = this.contentId;
			
			//define mapping of objects referring this file, stored in memory for lookup
			CopyOnWriteArrayList<Long> contentsList = new CopyOnWriteArrayList<>();
			contentsList.add(this.contentId);
			ContentFileMapping.fileIdToContentIdMapping.put(this.fileId, contentsList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @description this method remove all references of this object and make it available to garbage collector to sweep
	 * if data is not synced to disk then directly remove reference of this obect
	 * else remove file also in case only this object is referencing file.
	 */
	public void removeAssociationWithFiles(){
		if(this.data!=null && this.fileId==null){}
		else{
			//check it's file not associated with other content obj
			List<Long> contentNodes = ContentFileMapping.fileIdToContentIdMapping.get(this.fileId);
			
			contentNodes.remove(this.contentId);
			if(contentNodes.size()==1){
				File contentFile = new File("data/content_"+this.fileId+".txt");
				contentFile.delete();
			}
		}
	}

	@Override
	public Integer call() throws Exception {
		return syncContent();
	}
	
}
