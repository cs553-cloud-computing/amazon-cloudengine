package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

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
            
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public void localWorker(PrintWriter out, BufferedReader in){
    	BlockingQueue<String> jobQ = new ArrayBlockingQueue<String>(1024*1024);
    	    	
    	//Create thread pool for localworker
		ExecutorService workerThreads = Executors.newFixedThreadPool(poolSize);
    		
		for(int i = 0; i < poolSize; i++){
			workerThreads.submit(new LocalWorker(jobQ));
		}
		
		String task;
    	try {
			while ((task = in.readLine()) != null) {   
				jobQ.put(task);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Shutdown threads poll
    	workerThreads.shutdown();
		
    
    }
    
    public void remoteWorker(PrintWriter out, BufferedReader in){
    	//Batch sending task to remote workers 
		List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
        String task;
        int batchSize = 10; 
        int counter = 0;

        try {
			while ((task = in.readLine()) != null) {     	
				entries.add(new SendMessageBatchRequestEntry()
					.withId(Integer.toString(counter))
					.withMessageBody(task));	
				
				counter++;
				
			  	if(counter == batchSize){
			  		sqs.batchSend(entries);
			    	entries.clear();
			    	counter = 0;
			    }
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
        
        if(!entries.isEmpty()){
        	sqs.batchSend(entries);
        }
        
    }
    
  
}
