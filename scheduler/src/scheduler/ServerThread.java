package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;


public class ServerThread extends Thread {
    private Socket socket = null;
    private SQSService sqs;
 
    public ServerThread(Socket socket) {
        this.socket = socket;
        
    }
     
    public void run() {
 
        try{        
            InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);
			BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
        
			//Batch task sending 
			List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
            String task;
            int batchSize = 10; 
            int counter = 0;
            while ((task = bin.readLine()) != null) {     	
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
            
            if(!entries.isEmpty()){
            	sqs.batchSend(entries);
            }
            
            //out.println(inputLine);
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    
    
    
    
}
