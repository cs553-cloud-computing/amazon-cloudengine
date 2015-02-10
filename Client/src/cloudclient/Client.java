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

package cloudclient;

import java.net.*;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import commandline.CommandLineInterface;


public class Client {
		
	public static void main(String[] args) throws Exception {		
		//Command interpreter
		CommandLineInterface cmd = new CommandLineInterface(args);
		
		String socket = cmd.getOptionValue("s");
		String Host_IP = socket.split(":")[0];
		int Port = Integer.parseInt(socket.split(":")[1]);				
		String workload = cmd.getOptionValue("w");
		
		try {
			// make connection to server socket 
			Socket client = new Socket(Host_IP, Port);
			
			InputStream inStream = client.getInputStream();
			OutputStream outStream = client.getOutputStream();
			PrintWriter out = new PrintWriter(outStream, true);									
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			
			System.out.println("Send tasks to server...");
			//Start clock
			long startTime = System.currentTimeMillis();
						
			//Batch sending tasks
			batchSendTask(out, workload);
			client.shutdownOutput();
			
			//Batch receive responses
			batchReceiveResp(in);
			
			//End clock
			long endTime = System.currentTimeMillis();
			double totalTime = (endTime - startTime)/1e3;
			
			System.out.println("\nDone!");
			System.out.println("Time to execution = "+totalTime+" sec.");
			
			// close the socket connection
			client.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
	
	public static String getIP() throws MalformedURLException{		
		//Get external ip of the client				     
    	String ip = null;
    	
    	URL whatismyip = new URL("http://checkip.amazonaws.com");
		try {
			BufferedReader in_ip = new BufferedReader(new InputStreamReader(
			        whatismyip.openStream()));
			ip = in_ip.readLine();
			
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
		
		return ip;
		
	}
	
	@SuppressWarnings("unchecked")
	public static void batchSendTask(PrintWriter out, String workload) 
			throws FileNotFoundException, MalformedURLException {
		
		FileInputStream input = new FileInputStream(workload);
		BufferedReader bin = new BufferedReader(new InputStreamReader(input));
						
        // Get task from workload file 
 		String line;
// 		String sleepLength;
 		String id;
 		int count=0;
 		final int batchSize = 10;
 		
		try {
			//Get client public IP
			String ip = getIP();        
			//System.out.println(ip);
			
			//JSON object Array		
			JSONArray taskList = new JSONArray();  
			
			while ( (line = bin.readLine()) != null){
				//sleepLength = line.replaceAll("[^0-9]", "");
				//System.out.println(sleepLength);
				count++;
				id = ip + ":" + count;
				
				JSONObject task = new JSONObject();
				task.put("task_id", id);
				task.put("task", line);
				
				taskList.add(task);
				
				if(taskList.size() == batchSize){
					out.println(taskList.toString());
					taskList.clear();
				}
			}
			
			//System.out.println(taskList.toString());			
			if(!taskList.isEmpty()){
				out.println(taskList.toString());
				taskList.clear();
			}
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void batchReceiveResp(BufferedReader in) throws IOException, ParseException{
		BufferedWriter bw = new BufferedWriter(new FileWriter("result.txt"));
		
		JSONParser parser=new JSONParser();
		
		String message;
		while((message = in.readLine()) != null){
			//System.out.println(message);
			JSONArray responseList = (JSONArray)parser.parse(message);
			
			for(int i=0; i< responseList.size(); i++){
				JSONObject response = (JSONObject)responseList.get(i);
				bw.write(response.get("URL").toString());
				bw.newLine();
			}		
		}
		
		bw.close();
		
	}
	
}
