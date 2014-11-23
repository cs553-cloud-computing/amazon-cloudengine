package cloudclient;

import java.net.*;
import java.io.*;


public class Client {
	
	public static void main(String[] args) throws Exception {
		
		String IP_ADDR = args[0];
		int PORT = Integer.parseInt(args[1]);
		
		try {
			/* make connection to server socket */
			Socket clientSocket = new Socket(IP_ADDR, PORT);
			
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
			
			InputStream in = clientSocket.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			
			out.println("hello world!");//test!
			/* read the date from the socket */
			String line;
			while ( (line = bin.readLine()) != null)
				System.out.println(line);
			
			/* close the socket connection*/
			clientSocket.close();
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
}
