package cloudworker;


import org.json.simple.JSONObject;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class WorkerThread implements Runnable {
	AmazonSQS sqs;
	String responseQName;
	String task_id;
	long sleepLength;
	
	WorkerThread(String task_id, String task,AmazonSQS sqs){
		this.task_id = task_id;
		this.responseQName = task_id.split(":")[0].replaceAll("[^0-9]", "-");
		this.sleepLength = Long.parseLong(task);
		this.sqs = sqs;
			
		System.out.println(responseQName);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {	
		//Get queue url	
        GetQueueUrlResult urlResult = sqs.getQueueUrl(responseQName);
        String QueueUrl = urlResult.getQueueUrl();
        JSONObject result = new JSONObject();
        
        try {
        	Thread.sleep(sleepLength);
        	
        	result.put("task_id", task_id);
        	result.put("result", "0");
        	
        	sqs.sendMessage(new SendMessageRequest(QueueUrl, result.toString()));
        	System.out.println(Thread.currentThread().getName()+" sleep done!");
        	
        } catch (Exception e) {
        	result.put("task_id", task_id);
        	result.put("result", "1");
        	sqs.sendMessage(new SendMessageRequest(QueueUrl, result.toString()));
        	
        }
	}

}
