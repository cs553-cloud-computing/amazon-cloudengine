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
						
			/* read the date from the socket */
			/*String line;
			while ( (line = bin.readLine()) != null)
				System.out.println(line);*/
			
			// json test!
			JSONObject json = new JSONObject();

		    json.put("name","foo");
		    json.put("num",new Integer(100));
			      
		    out.println(json.toString());
						
			/* close the socket connection*/
			clientSocket.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
}
