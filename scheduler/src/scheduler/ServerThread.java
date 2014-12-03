/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;

import localworker.LocalWorker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;


public class ServerThread extends Thread {
    private Socket socket = null;
    private SQSService jobQ;
    private SQSService responseQ;
    private String workerType;
    private int poolSize;
    private int msg_cnt = 0;
    BlockingQueue<String> localJobQ;
    BlockingQueue<String> localRespQ;

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
	        	
	        	//Send tasks
				remoteBatchSend(in);
				
				//Get results
				remoteBatchReceive(out);
										
			}else{			
				localJobQ = new ArrayBlockingQueue<String>(1024*1024);
				localRespQ = new ArrayBlockingQueue<String>(1024*1024);
				//Local worker
				localSend(in);
				localReceive(out);
			}
			            
            socket.close();
            
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {			
			e.printStackTrace();
		} 
    }
    
    public void localSend(BufferedReader in) throws ParseException{
    	//Create thread pool for localworkers
		ExecutorService workerThreads = Executors.newFixedThreadPool(poolSize);
    		
		for(int i = 0; i < poolSize; i++){
			workerThreads.submit(new LocalWorker(localJobQ, localRespQ));
		}
		
		String message;
    	try {
    		JSONParser parser=new JSONParser();
    		
			while ((message = in.readLine()) != null) {
				
				JSONArray taskList = (JSONArray)parser.parse(message);
				
				for(int i=0; i< taskList.size(); i++){
					JSONObject task = (JSONObject)taskList.get(i);
					localJobQ.put(task.toString());
					msg_cnt++;
				}
			}
		} catch (IOException | InterruptedException e) {			
			e.printStackTrace();
		}
    	
    	//Shutdown threads poll
//    	workerThreads.shutdown();
		  
    }
    
    @SuppressWarnings("unchecked")
	public void localReceive(PrintWriter out) throws InterruptedException, ParseException{
    	JSONArray responseList = new JSONArray();
    	JSONParser parser=new JSONParser();
    	int batchSize = 10;
    	
    	while(msg_cnt > 0){ 
	    	while(!localRespQ.isEmpty()){	    		
	    		//waiting up to 100ms for an element to become available.
	        	String messageBody = localRespQ.poll(100, TimeUnit.MILLISECONDS);
           
	            JSONObject resp = (JSONObject)parser.parse(messageBody);
	            responseList.add(resp);
	            		            	            
	            msg_cnt--;
	            
	            if(responseList.size() == batchSize){
	            	out.println(responseList.toString());
			    	responseList.clear();
	            }
	            
	    	}
	    	
		    if(!responseList.isEmpty()){
		    	 out.println(responseList.toString());
		    	 responseList.clear();
		    }
		    
	    }
    	   	
    }
    
    public void remoteBatchSend(BufferedReader in) throws ParseException{
    	//Batch sending task to remote workers 
		List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
        String message;
        int batchSize = 10; 
        

        try {
        	JSONParser parser=new JSONParser();
        	
			while ((message = in.readLine()) != null) {
				
				JSONArray taskList = (JSONArray)parser.parse(message);
				
				for(int i=0; i< taskList.size(); i++){
					JSONObject task = (JSONObject)taskList.get(i);
					msg_cnt++;

					entries.add(new SendMessageBatchRequestEntry()
					.withId(Integer.toString(msg_cnt))
					.withMessageBody(task.toString()));	
						
				}
																
			  	if(entries.size() == batchSize){
			  		jobQ.batchSend(entries);
			    	entries.clear();		    	
			    }
			  	
			  	
			}
			
			 if(!entries.isEmpty()){
		        	jobQ.batchSend(entries);
		        	entries.clear();
		     }
			
		} catch (IOException e) {			
			e.printStackTrace();
		}
        
       
    }
    
    @SuppressWarnings("unchecked")
	public void remoteBatchReceive(PrintWriter out) throws ParseException{
    	JSONArray responseList = new JSONArray();
    	JSONParser parser=new JSONParser();
    	    	
    	while(msg_cnt > 0){ 
	    	while(responseQ.getQueueSize() > 0){
	    		//Get up to 10 messages
	    		 List<Message> messages = responseQ.batchReceive();
			      		     
			     for (Message message : messages) {
		            //System.out.println("  Message");
	//		            System.out.println("    MessageId:     " + message.getMessageId());
	//		            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	//		            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
		            //System.out.println("    Body:          " + message.getBody());
		          
		            //Get task
		            String messageBody = message.getBody();		           
		            JSONObject resp = (JSONObject)parser.parse(messageBody);
		            responseList.add(resp);
		            		            	            
		            msg_cnt--;
		            // Delete the message
                    String messageRecieptHandle = message.getReceiptHandle();
                    responseQ.deleteMessage(messageRecieptHandle);

		        }
			     if(!responseList.isEmpty()){
			    	 out.println(responseList.toString());
			    	 responseList.clear();
			     }
	    	}
    	}
    	 
    }
     
}
