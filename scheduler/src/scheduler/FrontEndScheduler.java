package scheduler;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;


public class FrontEndScheduler {

	public static void main(String[] args){
		
		int portNumber = Integer.parseInt(args[0]);
		
		String workerType = "lw";
		int poolSize = 2;
				
		try(ServerSocket serverSocket = new ServerSocket(portNumber)) {			
			/* listen for connections */
			while (true) {
				new ServerThread(serverSocket.accept(), workerType, poolSize).start();
				
			}
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		
		
	}
	
}
