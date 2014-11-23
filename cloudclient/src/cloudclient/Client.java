package cloudclient;

import java.net.*;
import java.io.*;


public class Client {
	
	public static void main(String[] args) throws Exception {
		
		String IP_ADDR = args[0];
		int PORT = Integer.parseInt(args[1]);
		
		try {
			/* make connection to server socket */
			Socket socket = new Socket(IP_ADDR, PORT);
			
			PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			
			InputStream in = socket.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			/* read the date from the socket */
			String line;
			while ( (line = bin.readLine()) != null)
				System.out.println(line);
			
			/* close the socket connection*/
			socket.close();
		}

		catch (IOException ioe) {
			System.err.println(ioe);
		}
            
	}
}
