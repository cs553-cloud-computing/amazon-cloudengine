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

package localworker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LocalWorker implements Runnable{
	BlockingQueue<String>  jobQ;
	BlockingQueue<String> respQ;
	
	public LocalWorker(BlockingQueue<String> jobQ, BlockingQueue<String> respQ){
		this.jobQ = jobQ;
		this.respQ = respQ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {	
		JSONParser parser=new JSONParser();
		JSONObject json;
		String task_id = null;
		String task;
		
		try {
        	
        	while(true){
	        	//waiting up to 100ms for an element to become available.
	        	String messageBody = jobQ.poll(100, TimeUnit.MILLISECONDS);
	        	
	        	if(messageBody != null){        	
		        	
		            json = (JSONObject)parser.parse(messageBody);
		            
		            task_id = json.get("task_id").toString();
		            task = json.get("task").toString();
		            
		        	Thread.sleep(Long.parseLong(task));
		        	
		        	JSONObject result = new JSONObject();
		        	result.put("task_id", task_id);
		        	result.put("result", "0");
		        	respQ.put(result.toString());
		        	
		        	//System.out.println(Thread.currentThread().getName()+" sleep done!");
	        	}
        	}
        	       	
        } catch (Exception e) {
        	JSONObject result = new JSONObject();
        	result.put("task_id", task_id);
        	result.put("result", "1");
        	try {
				respQ.put(result.toString());
				
			} catch (InterruptedException e1) {				
				e1.printStackTrace();
			}
        }
	}
	

}
