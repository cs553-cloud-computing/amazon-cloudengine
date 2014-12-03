/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

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
			
			System.out.println("Server start up!");
			
			/* listen for connections */
			while (true) {
				new ServerThread(serverSocket.accept(), workerType, poolSize).start();
				
			}
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		
		
	}
	
}
