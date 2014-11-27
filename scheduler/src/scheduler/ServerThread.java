package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import localworker.LocalWorker;

import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;


public class ServerThread extends Thread {
    private Socket socket = null;
    private SQSService sqs;
    String workerType;
	int poolSize;
 
    public ServerThread(Socket socket, String workerType, int poolSize ) {
        this.socket = socket;          
        this.workerType = workerType;
        this.poolSize = poolSize;
        
        if(workerType.equals("rw")){
        	 sqs = new SQSService();
        }
        
    }
     
    public void run() {
 
        try{        
            InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        			
			if(workerType.equals("rw")){
				//Remote worker
				remoteWorker(out, in);				
			}else{			
				//Local worker
				localWorker(out, in);
			}
			            
            socket.close();
            
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } 
    }
    
    public void localWorker(PrintWriter out, BufferedReader in) throws ParseException{
    	BlockingQueue<String> jobQ = new ArrayBlockingQueue<String>(1024*1024);
    	    	
    	//Create thread pool for localworker
		ExecutorService workerThreads = Executors.newFixedThreadPool(poolSize);
    		
		for(int i = 0; i < poolSize; i++){
			workerThreads.submit(new LocalWorker(jobQ));
		}
		
		String message;
    	try {
    		JSONParser parser=new JSONParser();
    		
			while ((message = in.readLine()) != null) {
				
				JSONArray taskList = (JSONArray)parser.parse(message);
				
				for(int i=0; i< taskList.size(); i++){
					JSONObject task = (JSONObject)taskList.get(i);
					jobQ.put(task.toString());
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Shutdown threads poll
    	workerThreads.shutdown();
		
    
    }
    
    public void remoteWorker(PrintWriter out, BufferedReader in) throws ParseException{
    	//Batch sending task to remote workers 
		List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
        String message;
        int batchSize = 10; 
        int counter = 0;

        try {
        	JSONParser parser=new JSONParser();
        	
			while ((message = in.readLine()) != null) {
				JSONArray taskList = (JSONArray)parser.parse(message);
				
				for(int i=0; i< taskList.size(); i++){
					JSONObject task = (JSONObject)taskList.get(i);
					
					entries.add(new SendMessageBatchRequestEntry()
					.withId(Integer.toString(counter))
					.withMessageBody(task.toString()));	
					
					counter++;
				}
																
			  	if(entries.size() == batchSize){
			  		sqs.batchSend(entries);
			    	entries.clear();		    	
			    }
			  	
			  	
			}
			
		} catch (IOException e) {			
			e.printStackTrace();
		}
        
        if(!entries.isEmpty()){
        	sqs.batchSend(entries);
        	entries.clear();
        }
        
    }
     
}
