import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Constants;
import utils.RemoteEntity;

/**
 * Daemon for sending delays to each ip:port pair
 * @author AllenLiu
 */
class ClientSendDaemon implements Runnable{

	private String ip;
	private int port;
	private long[] delays;

	public ClientSendDaemon(String ip, int port, long[] delays) {
		this.ip = ip;
		this.port = port;
		this.delays = delays;
	}

	@Override
	public void run() {
		Socket kkSocket = null;
		PrintWriter out = null;
		try {
			kkSocket = new Socket(this.ip, this.port);
			out = new PrintWriter(kkSocket.getOutputStream(), true);			
			out.println(delays[0] + "," + delays[1] + "," + delays[2]);
		} catch (IOException e) {
			System.err.println("Error when creating the socket at client side");
		} finally {
			if(out != null){
				out.close();
			}
			try {
				if(kkSocket != null){
					kkSocket.close();
				}
			} catch (IOException e) {
				System.err.println("Error when closing the socket at client side");
			}
		}
		
	}
}

public class OpenflowHDFSServiceClient {

	private boolean readPriority;
	private boolean printStatCollectorLength;

	private OpenflowHDFSStatCollector statCollector;
	private Set<RemoteEntity> datanodeIpAndPort;
	private Set<RemoteEntity> clientIpAndPort;

	public OpenflowHDFSServiceClient(boolean readPriority, Set<RemoteEntity> datanodeIpAndPort, Set<RemoteEntity> clientIpAndPort, Map<String, String> publicIpToPrivateIpMap, boolean printStatCollectorLength){
		System.out.println(OpenflowHDFSServiceClient.class.getSimpleName() + " initialize the client service.");
		this.readPriority = readPriority;
		this.printStatCollectorLength = printStatCollectorLength;
		this.datanodeIpAndPort = datanodeIpAndPort;
		this.clientIpAndPort = clientIpAndPort;
		List<String> datanodeIps = new ArrayList<String>();
		List<String> clientIps = new ArrayList<String>();

		for(RemoteEntity eachDatanodeIpAndPort : datanodeIpAndPort){
			datanodeIps.add(publicIpToPrivateIpMap.get(eachDatanodeIpAndPort.getIp()));
		}
		for(RemoteEntity eachClientIpAndPort : clientIpAndPort){
			clientIps.add(publicIpToPrivateIpMap.get(eachClientIpAndPort.getIp()));
		}
		
		this.statCollector = new OpenflowHDFSStatCollector(datanodeIps, clientIps);
		new Thread(this.statCollector).start();											//start the stat collector thread
	}

	public void start() throws UnknownHostException, IOException{
		long sleepInEachTurn = -1;
		while(true){
			long[] delays = calculateDelaysFromBandwidth();
			//send it via client send daemon
			List<Thread> threadPools = new ArrayList<Thread>();
			for(RemoteEntity eachRemote : this.datanodeIpAndPort){
				Thread sendToDatanodeThread = new Thread(new ClientSendDaemon(eachRemote.getIp(), eachRemote.getPort(), delays));
				sendToDatanodeThread.start();
				threadPools.add(sendToDatanodeThread);
			}
			for(RemoteEntity eachRemote : this.clientIpAndPort){
				Thread sendToClientThread = new Thread(new ClientSendDaemon(eachRemote.getIp(), eachRemote.getPort(), delays));
				sendToClientThread.start();
				threadPools.add(sendToClientThread);
			}
			
			//make sure all thread finish execution in this iteration.
			for(Thread eachThreadInPool : threadPools){
				try {
					eachThreadInPool.join();
				} catch (InterruptedException e) {
					System.err.println(OpenflowHDFSServiceClient.class.getSimpleName() + " some thread can't be joined.");
				}
			}
			
			sleepInEachTurn = Math.max(delays[0], delays[1]);
			sleepInEachTurn = Math.max(sleepInEachTurn, delays[2]) * 2;
			sleepInEachTurn = (sleepInEachTurn == 0)? Constants.DEFAULT_SENDING_RATE_DELAY_IN_MILLI_SEC : sleepInEachTurn;
			try {
				Thread.sleep(sleepInEachTurn);
			} catch (InterruptedException e) {
				System.err.println("Cannot sleep for the delay");
			}
		}
	}

	//bandwidth is bytes/milli sec, delay is milliseconds
	//first is read, then write, then replication
	private long[] calculateDelaysFromBandwidth(){
		double readBandwidth = this.statCollector.getReadBandwidth();
		double writeBandwidth = this.statCollector.getWriteBandwidth();
		double replicationBandwidth = this.statCollector.getReplicationBandwidth();
		if(!this.printStatCollectorLength){
			System.out.println(OpenflowHDFSServiceClient.class.getSimpleName() + " bandwidth information: read->" + readBandwidth + " write->" + writeBandwidth + " replication->" + replicationBandwidth);
		} else {
			System.out.println(OpenflowHDFSServiceClient.class.getSimpleName() + " stat collector length: " + this.statCollector.getStatCollectorSize() + " time: " + System.currentTimeMillis());
		}
		long[] result = new long[3];

		if(this.readPriority){
			if(readBandwidth > Constants.THRESHOLD_FOR_CHANGING_DELAY_IN_MILLI_SEC){
				result[1] = (long) ((writeBandwidth/ readBandwidth) * Constants.UNIT_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC);
				if(result[1] > Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC){
					result[1] = Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC;
				}
			}
		} else {	
			if(writeBandwidth > Constants.THRESHOLD_FOR_CHANGING_DELAY_IN_MILLI_SEC){
				result[0] = (long) ((readBandwidth/ writeBandwidth) * Constants.UNIT_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC);
				if(result[0] > Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC){
					result[0] = Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC;
				}
			}
		}
		 if(readBandwidth + writeBandwidth > Constants.THRESHOLD_FOR_CHANGING_DELAY_IN_MILLI_SEC){
			result[2] = (long) ((replicationBandwidth / (readBandwidth + writeBandwidth)) * Constants.UNIT_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC);
			if(result[2] > Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC){
				result[2] = Constants.MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC;
			}
		}
		return result;
	}

}
