package cloudworker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;


public class RemoteWorker {
	
	static AmazonSQS sqs;
	static DynamoDBService dynamoDB;
	
	private static void init() throws Exception {
		/*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
	     AWSCredentials credentials = null;
	        try {
	            credentials = new ProfileCredentialsProvider().getCredentials();
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (~/.aws/credentials), and is in valid format.",
	                    e);
	        }

	        sqs = new AmazonSQSClient(credentials);
	        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
			sqs.setRegion(usEast1);
			
			dynamoDB = new DynamoDBService(credentials);
			
	}
	
	public static void main(String[] args) throws Exception {
		init();
		
		final int poolSize = 4;
	    		
		//Create thread pool
		ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
		
        //Get queue url
        GetQueueUrlResult urlResult = sqs.getQueueUrl("JobQueue");
        String jobQueueUrl = urlResult.getQueueUrl();
              
		// Receive messages
        System.out.println("Receiving messages from JobQueue.\n");
        while(getQueueSize(sqs, jobQueueUrl) > 0){
	        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(jobQueueUrl);
	        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	        for (Message message : messages) {
	            System.out.println("  Message");
	            System.out.println("    MessageId:     " + message.getMessageId());
	            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
	            System.out.println("    Body:          " + message.getBody());
	          
	            //Get task
	            String task = message.getBody();
	            try{
		            dynamoDB.addTask(task,task);
		            String sleepLength = task.replaceAll("[^0-9]", "");
		            //System.out.println(Long.parseLong(sleepLength));
		            
		            //Execute task
		            threadPool.submit(new WorkerThread(Long.parseLong(sleepLength)));
		            	            
		            // Delete the message
		            String messageRecieptHandle = message.getReceiptHandle();
		            sqs.deleteMessage(new DeleteMessageRequest(jobQueueUrl, messageRecieptHandle));
	            }catch(ConditionalCheckFailedException ccf){
	        	   //DO something...
	            }
	            
	        }	
        }
        System.out.println();
        
        threadPool.shutdown();
     // Wait until all threads are finished
        while(!threadPool.isTerminated()){
        	
        }
        
	}
	
	public static int getQueueSize(AmazonSQS sqs, String queueUrl){
		HashMap<String, String> attributes;
		
		Collection<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessages");
		
		GetQueueAttributesRequest getAttributesRequest = new GetQueueAttributesRequest(queueUrl)
		.withAttributeNames(attributeNames);
		attributes = (HashMap<String, String>) sqs.getQueueAttributes(getAttributesRequest).getAttributes();
		
		return Integer.valueOf(attributes.get("ApproximateNumberOfMessages"));
	}
}
