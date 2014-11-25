package cloudclient;

import java.net.*;
import java.io.*;

import org.json.simple.JSONObject;


public class Client {
	
	public static void main(String[] args) throws Exception {
		
		String IP_ADDR = args[0];
		int PORT = Integer.parseInt(args[1]);
		
		try {
			/* make connection to server socket */
			Socket clientSocket = new Socket(IP_ADDR, PORT);
			InputStream inStream = clientSocket.getInputStream();
			OutputStream outStream = clientSocket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);									
			BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
						
			System.out.println(clientSocket.getLocalSocketAddress());
			/* read the date from the socket */
			/*String line;
			while ( (line = bin.readLine()) != null)
				System.out.println(line);*/
			
			// json test!
			JSONObject json_1 = new JSONObject();

		    json_1.put("task_id","553");
		    json_1.put("task",new Integer(5000));
		    
		    JSONObject json_2 = new JSONObject();

		    json_2.put("task_id","554");
		    json_2.put("task",new Integer(1000));
			      
		    out.println(json_1.toString());
		    out.println(json_2.toString());
						
			/* close the socket connection*/
			clientSocket.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
	
}
