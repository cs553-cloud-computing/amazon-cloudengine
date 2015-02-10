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
			
		//System.out.println(responseQName);
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
        	//System.out.println(Thread.currentThread().getName()+" sleep done!");
        	
        } catch (Exception e) {
        	result.put("task_id", task_id);
        	result.put("result", "1");
        	sqs.sendMessage(new SendMessageRequest(QueueUrl, result.toString()));
        	
        }
	}

}
