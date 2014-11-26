package cloudclient;

import java.net.*;
import java.io.*;

import org.json.simple.JSONObject;

import commandline.CommandLineInterface;


public class Client {
	
	public static void main(String[] args) throws Exception {
		
		//Command interpreter
		CommandLineInterface command = new CommandLineInterface(args);
		
		String socket = command.getOptionValue("s");
		String Host_Addr = socket.split(":")[0];
		int Port = Integer.parseInt(socket.split(":")[1]);
		
		System.out.println(Port);
		String workload = command.getOptionValue("w");
		
		try {
			// make connection to server socket 
			Socket clientSocket = new Socket(Host_Addr, Port);
			InputStream inStream = clientSocket.getInputStream();
			OutputStream outStream = clientSocket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);									
			BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
						
			//System.out.println(clientSocket.getLocalSocketAddress());
					
			// json test!
			JSONObject json_1 = new JSONObject();

		    json_1.put("task_id","553");
		    json_1.put("task",new Integer(5000));
		    
		    JSONObject json_2 = new JSONObject();

		    json_2.put("task_id","554");
		    json_2.put("task",new Integer(1000));
			      
		    out.println(json_1.toString());
		    out.println(json_2.toString());
						
		    // read the date from the socket 
 			/*String line;
 			while ( (line = bin.readLine()) != null)
 				System.out.println(line);*/
		    
		    
			// close the socket connection
			clientSocket.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
	
}
