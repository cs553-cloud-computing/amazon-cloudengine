package scheduler;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

import commandline.CommandLineInterface;


public class FrontEndScheduler {

	public static void main(String[] args){
		String workerType = null;
		int poolSize = 0;
		
		CommandLineInterface cmd = new CommandLineInterface(args);
		
		int portNumber = Integer.parseInt(cmd.getOptionValue("s"));		
		String localWorkers = cmd.getOptionValue("lw");
		
		if(localWorkers != null){
			workerType = "lw";
			poolSize = Integer.parseInt(localWorkers);
		}else{
			workerType = "rw";
		}
				
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
