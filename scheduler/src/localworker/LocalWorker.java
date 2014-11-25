package localworker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LocalWorker implements Runnable{
	BlockingQueue<String>  jobQ;
	
	public LocalWorker(BlockingQueue<String> jobQ){
		this.jobQ = jobQ;
	}
	
	@Override
	public void run() {	
        try {
        	//Waiting for non-empty jobQ
        	Thread.sleep(2000);
        	
        	while(!jobQ.isEmpty()){
	        	//waiting up to 100ms for an element to become available.
	        	String messageBody = jobQ.poll(100, TimeUnit.MILLISECONDS);
	        	
	        	if(messageBody == null){
	        		break;
	        	}
	        	
	        	JSONParser parser=new JSONParser();
	            JSONObject json = (JSONObject)parser.parse(messageBody);
	            
	            //String task_id = json.get("task_id").toString();
	            String task = json.get("task").toString();
	            
	        	Thread.sleep(Long.parseLong(task));
	        	
	        	System.out.println(Thread.currentThread().getName()+" sleep done!");
        	}
        	       	
        } catch (Exception e) {
             System.out.println(e);
        }
	}
	

}
