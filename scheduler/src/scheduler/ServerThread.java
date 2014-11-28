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

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;


public class ServerThread extends Thread {
    private Socket socket = null;
    private SQSService jobQ;
    private SQSService responseQ;
    String workerType;
	int poolSize;
 
    public ServerThread(Socket socket, String workerType, int poolSize ) {
        this.socket = socket;          
        this.workerType = workerType;
        this.poolSize = poolSize;
        
    }
     
    public void run() {
 
        try{        
            InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        			
			if(workerType.equals("rw")){
				jobQ = new SQSService("JobQueue");
	        	 //Set client ip as response queue name
	        	 String resQName = socket.getInetAddress().toString().substring(1).replaceAll("[^0-9]", "-");
	        	 responseQ = new SQSService(resQName);
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
    
    public void remoteWorker(PrintWriter out, BufferedReader in){
    	try {
    		//Send tasks
			remoteBatchSend(in);
			
			//Get results
			remoteBatchReceive(out);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public void remoteBatchSend(BufferedReader in) throws ParseException{
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
			  		jobQ.batchSend(entries);
			    	entries.clear();		    	
			    }
			  	
			  	
			}
			
		} catch (IOException e) {			
			e.printStackTrace();
		}
        
        if(!entries.isEmpty()){
        	jobQ.batchSend(entries);
        	entries.clear();
        }
    }
    
    public void remoteBatchReceive(PrintWriter out){
    	while(true){ 
	    	while(responseQ.getQueueSize() > 0){	 
	    		 List<Message> messages = responseQ.batchReceive();
			     //out.println(messages);  
			     
		        for (Message message : messages) {
		            System.out.println("  Message");
	//		            System.out.println("    MessageId:     " + message.getMessageId());
	//		            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	//		            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
		            System.out.println("    Body:          " + message.getBody());
		          
		            //Get task
		            String messageBody = message.getBody();
		        }
	    	 }
    	}
    	 
    }
     
}
