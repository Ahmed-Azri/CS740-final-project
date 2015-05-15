import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.Constants;
import utils.RemoteEntity;


public class Main {

	//call it by java Main read/write datanode1ip:port,datanode2ip:port client1ip:port client2ip:port
	public static void main(String args[]){
		boolean readPriority = args[0].equals(Constants.READ_OPT);
		boolean printStatCollectorLength = args[1].equals(Constants.LOG_STAT_COLLECTOR_LENGTH_OPTION);

		if(readPriority){
			System.out.println("we are prioritizing read over write");
		} else {
			System.out.println("we are prioritizing write over read");
		}
		Set<RemoteEntity> datanodeRemoteEntities = parseEntityFromStringCommand(args[2]);
		Set<RemoteEntity> clientRemoteEntities = parseEntityFromStringCommand(args[3]);
		Map<String, String> publicIpToPrivateIpMap = parsePublicIpToPrivateIpMap(args[4]);
		OpenflowHDFSServiceClient client = new OpenflowHDFSServiceClient(readPriority, datanodeRemoteEntities, clientRemoteEntities, publicIpToPrivateIpMap, printStatCollectorLength);
		try {
			client.start();
		} catch (IOException e) {
			System.err.println("cannot start the client successfully");
			System.exit(-1);
		}	
	}
	
	private static Map<String, String> parsePublicIpToPrivateIpMap(String argument){
		Map<String, String> result = new HashMap<String, String>();
		String[] eachEntryArr = argument.split(",");
		for(String eachEntry : eachEntryArr){
			String[] eachEntrySplit = eachEntry.split("=");
			result.put(eachEntrySplit[0], eachEntrySplit[1]);
		}
		return result;
	}

	private static Set<RemoteEntity> parseEntityFromStringCommand(String command){
		Set<RemoteEntity> remoteEntities = new HashSet<RemoteEntity>();
		String[] ipToPortMapArr = command.split(Constants.IP_PORT_PAIR_DELIMITER);
		for(String eachIpToPortString: ipToPortMapArr){
			String[] addressAndPort = eachIpToPortString.split(":");
			String ip = addressAndPort[0];
			int port = Integer.parseInt(addressAndPort[1]);
			RemoteEntity eachRemoteEntity = new RemoteEntity(ip, port);
			remoteEntities.add(eachRemoteEntity);
		}
		return remoteEntities;
	}
}
