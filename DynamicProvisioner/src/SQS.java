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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;


public class SQS {
	
	static AmazonSQS sqs;
	static String queueURL;
	
	public SQS(AWSCredentials credentials, Region region, String queueName) {
        sqs = new AmazonSQSClient(credentials);
        sqs.setRegion(region);
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        queueURL = sqs.createQueue(createQueueRequest).getQueueUrl();
	}
	
	public int getApproximateQueueSize() {
		Collection<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessages");
		GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest(queueURL).withAttributeNames(attributeNames);
		Map<String, String> attributes = sqs.getQueueAttributes(queueAttributesRequest).getAttributes();
		return Integer.valueOf(attributes.get("ApproximateNumberOfMessages"));
	}
	
	public int getApproximateNotVisibleMessageNum() {
		Collection<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessagesNotVisible");
		GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest(queueURL).withAttributeNames(attributeNames);
		Map<String, String> attributes = sqs.getQueueAttributes(queueAttributesRequest).getAttributes();
		return Integer.valueOf(attributes.get("ApproximateNumberOfMessagesNotVisible"));
	}
}
