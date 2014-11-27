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
				
		if(cmd.hasOption("lw")){
			workerType = "lw";
			poolSize = Integer.parseInt(cmd.getOptionValue("lw"));
			
		} else if(cmd.hasOption("rw")){
			workerType = "rw";
			
		} else{
			System.out.println("Please specify worker type!");
			System.exit(0);
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
