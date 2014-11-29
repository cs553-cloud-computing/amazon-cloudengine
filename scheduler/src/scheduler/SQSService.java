
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

public class SQSService {
	private AmazonSQS sqs;
	private String queueUrl;
	
	public SQSService(String queueName){
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
		
		// Create a queue or returns the URL of an existing one
        System.out.println("Creating a new SQS queue called " + queueName);
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        queueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
                
	}
	
	public void batchSend(List<SendMessageBatchRequestEntry> entries){
 
        try {        
        	// Send batch messages
            System.out.println("\nSending a message to jobQueue.\n");
            
            SendMessageBatchRequest batchRequest = new SendMessageBatchRequest().withQueueUrl(queueUrl);		  	
		  	batchRequest.setEntries(entries);
		
		  	SendMessageBatchResult batchResult = sqs.sendMessageBatch(batchRequest);
		  		
		  	// sendMessageBatch can return successfully, and yet individual batch
		  	// items fail. So, make sure to retry the failed ones.
		  	if (!batchResult.getFailed().isEmpty()) {
		    	System.out.println("Retrying sending messages...");		       
		        	
		    	sqs.sendMessageBatch(batchRequest);
		  	}
            
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
	
	public List<Message> batchReceive(){
		//Batch retrieving messages
    	ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
    		.withQueueUrl(queueUrl)
    		.withMaxNumberOfMessages(10);
    	
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        
        return messages;
        
	}
	
	public int getQueueSize(){
		HashMap<String, String> attributes;
		
		Collection<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessages");
		
		GetQueueAttributesRequest getAttributesRequest = new GetQueueAttributesRequest(queueUrl)
			.withAttributeNames(attributeNames);
		attributes = (HashMap<String, String>) sqs.getQueueAttributes(getAttributesRequest).getAttributes();
		
		return Integer.valueOf(attributes.get("ApproximateNumberOfMessages"));
		
	}

	public void deleteMessage(String messageRecieptHandle){
		 sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));

	}
	
}
