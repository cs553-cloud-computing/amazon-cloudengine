package cloudworker;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class WorkerThread implements Runnable {
	AmazonSQS sqs;
	String responseQName;
	long sleepLength;
	
	WorkerThread(String task_id, String task,AmazonSQS sqs){
		this.responseQName = task_id.split(":")[0].replaceAll("[^0-9]", "-");
		this.sleepLength = Long.parseLong(task);
		this.sqs = sqs;
			
		System.out.println(responseQName);
	}
	
	@Override
	public void run() {	
		//Get queue url
        GetQueueUrlResult urlResult = sqs.getQueueUrl(responseQName);
        String QueueUrl = urlResult.getQueueUrl();
		
        try {
        	Thread.sleep(sleepLength);
        	sqs.sendMessage(new SendMessageRequest(QueueUrl, "0"));
        	System.out.println(Thread.currentThread().getName()+" sleep done!");
        	
        } catch (Exception e) {
        	sqs.sendMessage(new SendMessageRequest(QueueUrl, "1"));
        	
        }
	}

}
