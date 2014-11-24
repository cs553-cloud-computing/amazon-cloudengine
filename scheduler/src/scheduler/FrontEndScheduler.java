package scheduler;

import java.net.*;
import java.io.*;


public class FrontEndScheduler {

	public static void main(String[] args){
		
		int PORT = Integer.parseInt(args[0]);
		
		try(ServerSocket serverSocket = new ServerSocket(PORT)) {			
			/* listen for connections */
			while (true) {
				new ServerThread(serverSocket.accept()).start();
				
			}
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		
		
	}
}
