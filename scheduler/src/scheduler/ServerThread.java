package scheduler;

import java.net.*;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

 
public class ServerThread extends Thread {
    private Socket socket = null;
 
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
     
    public void run() {
 
        try{        
            InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			
			PrintWriter out = new PrintWriter(outStream, true);
			BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));
        
			JSONParser parser=new JSONParser();
			
            String inputLine;
            while ((inputLine = bin.readLine()) != null) {     	
                Object obj = parser.parse(inputLine);
                JSONObject message = (JSONObject)obj;
                System.out.println(message.get("name").toString());
            }
            
          //out.println(inputLine);
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}
    }
    
}
