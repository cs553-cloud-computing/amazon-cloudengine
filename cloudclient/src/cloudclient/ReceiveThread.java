package cloudclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReceiveThread extends Thread{

	String message;
	BufferedReader bin;
	InputStream inStream;
	
	ReceiveThread(InputStream inStream){
		this.inStream = inStream;
		bin = new BufferedReader(new InputStreamReader(inStream));
	}
	
	public void run(){
		
			try {
				while(inStream.available()!=0){
					message = bin.readLine();
					System.out.println(message);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				while(inStream.available()!=0){
					message = bin.readLine();
					System.out.println(message);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
}
