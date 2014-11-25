package scheduler;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;

public class SQSService {
	private AmazonSQS sqs;
	
	SQSService(){
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
	}
	
	public void batchSend(List<SendMessageBatchRequestEntry> entries){
	    
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
           
        
        try {
            // Create a queue
            System.out.println("Creating a new SQS queue called JobQueue.\n");
            CreateQueueRequest createQueueRequest = new CreateQueueRequest("JobQueue");
            String jobQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

            // List queues
            /*System.out.println("Listing all queues in your account.\n");
            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
                System.out.println("  QueueUrl: " + queueUrl);
            }
            System.out.println();*/
            
        	// Send batch messages
            System.out.println("Sending a message to jobQueue.\n");
            
            SendMessageBatchRequest batchRequest = new SendMessageBatchRequest().withQueueUrl(jobQueueUrl);		  	
		  	batchRequest.setEntries(entries);
		
		  	SendMessageBatchResult batchResult = sqs.sendMessageBatch(batchRequest);
		  		
		  	// sendMessageBatch can return successfully, and yet individual batch
		  	// items fail. So, make sure to retry the failed ones.
		  	if (!batchResult.getFailed().isEmpty()) {
		    	System.out.println("Retrying sending messages...");		       
		        	
		    	sqs.sendMessageBatch(batchRequest);
		  	}
          
//            sqs.sendMessage(new SendMessageRequest(jobQueueUrl, task_1));
//            sqs.sendMessage(new SendMessageRequest(jobQueueUrl, task_2));
//            sqs.sendMessage(new SendMessageRequest(jobQueueUrl, task_1));
//            sqs.sendMessage(new SendMessageRequest(jobQueueUrl, task_2));
            
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
            
    }
	
}
