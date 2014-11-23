package scheduler;

import java.net.*;
import java.io.*;
 
public class ServerThread extends Thread {
    private Socket socket = null;
 
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
     
    public void run() {
 
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    		InputStream in = socket.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        
            String inputLine;
            while ((inputLine = bin.readLine()) != null) {
                out.println(inputLine);
                
            }
            
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
