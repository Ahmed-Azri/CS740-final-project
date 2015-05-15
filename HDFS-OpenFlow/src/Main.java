import java.io.IOException;

public class Main {
	public static void main(String args[]){
		//which port this server runs on
		int port = Integer.parseInt(args[0]);
		
		//delays to be monitored
		HDFSTrafficDelay delays = new HDFSTrafficDelay();
		
		try {
			//create the server as thread and then start the thread
			HDFSOpenFlowServiceServer server = new HDFSOpenFlowServiceServer(port, delays);
			new Thread(server).start();
			
			//get the delay information
			while(true){
				System.out.println("HDFS received delay: " + delays);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println(Main.class.getSimpleName() + " cannot sleep for 1 sec because I am interrupted.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
