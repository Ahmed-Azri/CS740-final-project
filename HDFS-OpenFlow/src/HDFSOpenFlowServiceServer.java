import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * HDFS side server daemon that mainly does the job of accepting the delay messages.
 * 
 * @author AllenLiu
 */

public class HDFSOpenFlowServiceServer implements Runnable {
	private HDFSTrafficDelay delays;
	private ServerSocket sSocket;
	
	public HDFSOpenFlowServiceServer(int port, HDFSTrafficDelay delays) throws IOException {
		this.delays = delays;						//all delays are 0 initially
		this.sSocket = new ServerSocket(port);		//initialize the server socket
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Socket clientSocket = this.sSocket.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				//controller sends delays for read, write, replicate as a,b,c in milliseconds
				String[] controllerCommand = in.readLine().split(",");		
				
				//set the delays correspondingly, we need three of them to be synchronizeds
				synchronized(this.delays){
					delays.setReadDelay(Long.parseLong(controllerCommand[0]));
					delays.setWriteDelay(Long.parseLong(controllerCommand[1]));
					delays.setReplicationDelay(Long.parseLong(controllerCommand[2]));
				}
				
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("Can't open a socket for client at server");
			}
			
		}
	}
}
