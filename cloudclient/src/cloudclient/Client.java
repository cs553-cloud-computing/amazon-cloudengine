package cloudclient;

import java.net.*;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import commandline.CommandLineInterface;


public class Client {
	
	public static void main(String[] args) throws Exception {
		
		//Command interpreter
		CommandLineInterface cmd = new CommandLineInterface(args);
		
		String socket = cmd.getOptionValue("s");
		String Host_Addr = socket.split(":")[0];
		int Port = Integer.parseInt(socket.split(":")[1]);
				
		String workload = cmd.getOptionValue("w");
		
		try {
			// make connection to server socket 
			Socket client = new Socket(Host_Addr, Port);
			InputStream inStream = client.getInputStream();
			OutputStream outStream = client.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);									
			BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
									
			//Batch sending tasks
			batchSendTask(out, workload);
					
			//Batch receive responses
			batchReceiveResp(inStream);
			
			// close the socket connection
			client.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
	
	@SuppressWarnings("unchecked")
	public static void batchSendTask(PrintWriter out, String workload) 
			throws FileNotFoundException, MalformedURLException {
		
		FileInputStream input = new FileInputStream(workload);
		BufferedReader bin = new BufferedReader(new InputStreamReader(input));
				
		String ip = getIP();
        
		System.out.println(ip);
		
		//Json object Array		
		JSONArray taskList = new JSONArray();  
		
        // Get task from workload file 
 		String line;
 		String sleepLength;
 		String id;
 		int count=0;
 		int batchSize = 10;
		try {
			while ( (line = bin.readLine()) != null){
				sleepLength = line.replaceAll("[^0-9]", "");
				System.out.println(sleepLength);
				id = ip + ":" + count;
				count++;
				
				JSONObject task = new JSONObject();
				task.put("task_id", id);
				task.put("task", sleepLength);
				
				taskList.add(task);
				
				if(taskList.size() == batchSize){
					out.println(taskList.toString());
					taskList.clear();
				}
			}
			System.out.println(taskList.toString());
			if(!taskList.isEmpty()){
				out.println(taskList.toString());
				taskList.clear();
			}
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public static void batchReceiveResp(InputStream inStream) throws IOException{
		String message;
		BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
		
		while((message = bin.readLine()) != null){
			System.out.println(message);
		}
		
	}
	
}
