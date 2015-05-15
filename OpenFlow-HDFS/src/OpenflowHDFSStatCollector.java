import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Constants;
import utils.OpenflowMatchEntry;
import utils.TrafficAnalyzer;

public class OpenflowHDFSStatCollector implements Runnable {

	private double readBandwidth;
	private double writeBandwidth;
	private double replicationBandwidth;

	private Map<String, String> typeOfTraffic; 	

	private TrafficAnalyzer analyzer;

	public OpenflowHDFSStatCollector(List<String> datanodeIps, List<String> clientIps){
		this.readBandwidth = 0.0;
		this.writeBandwidth = 0.0;
		this.replicationBandwidth = 0.0;

		this.typeOfTraffic = new HashMap<String, String>();

		for(String clientIp : clientIps){
			for(String datanodeIp: datanodeIps){
				this.typeOfTraffic.put(clientIp + "-" + datanodeIp, Constants.FLOW_STAT_WRITE_KEY);
			}
		}

		for(String clientIp : clientIps){
			for(String datanodeIp: datanodeIps){
				this.typeOfTraffic.put(datanodeIp + "-" + clientIp, Constants.FLOW_STAT_READ_KEY);
			}
		}

		for(String datanodeIpFrom : datanodeIps){
			for(String datanodeTo: datanodeIps){
				if(!datanodeIpFrom.equals(datanodeTo)){
					this.typeOfTraffic.put(datanodeIpFrom + "-" + datanodeTo, Constants.FLOW_STAT_REPLICATION_KEY);
				}
			}
		}
		
		this.analyzer = new TrafficAnalyzer(this.typeOfTraffic);
	}

	/* Bandwidth measurements are all in bytes/milliseconds */
	public double getReadBandwidth(){
		return this.readBandwidth;
	}

	public double getWriteBandwidth(){
		return this.writeBandwidth;
	}

	public double getReplicationBandwidth(){
		return this.replicationBandwidth;
	}

	public int getStatCollectorSize(){
		return this.analyzer.getNumberOfEntries();
	}
	
	@Override
	public void run() {
		int currReadTrafficCount = -1, currWriteTrafficCount = -1, currReplicationTrafficCount = -1;
		int prevReadTrafficCount = -1, prevWriteTrafficCount = -1, prevReplicationTrafficCount = -1;
		long prevStatCollectTime = -1, currStatCollectTime = -1;		
		
		while(true){
			Map<String, Integer> trafficCountMap = new HashMap<String, Integer>();

			trafficCountMap.put(Constants.FLOW_STAT_READ_KEY, 0);
			trafficCountMap.put(Constants.FLOW_STAT_WRITE_KEY, 0);
			trafficCountMap.put(Constants.FLOW_STAT_REPLICATION_KEY, 0);
			
			try {
				URL url = new URL(Constants.FLOODLIGHT_REST_URL_FOR_FLOW_STAT);
				URLConnection urlc = url.openConnection();
				BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				String jsonResponseAsString = br.readLine();
				
				currStatCollectTime = System.currentTimeMillis();			//get the current stat collection time
								
				JSONObject jsonResponse = new JSONObject(jsonResponseAsString);
				JSONObject switchAsJson = jsonResponse.getJSONObject(Constants.SWITCH_ID);
				JSONArray flows = (JSONArray) switchAsJson.get(Constants.REST_RESPONSE_FLOWS_KEY);
				for(int i = 0; i < flows.length(); i++){
					JSONObject eachFlow = flows.getJSONObject(i);
					long eachFlowByteCount = eachFlow.getLong((Constants.REST_RESPONSE_FLOW_BYTE_COUNT));
					if(eachFlow.has(Constants.REST_RESPONSE_FLOW_MATCH_KEY)){
						JSONObject matchForEachFlow = eachFlow.getJSONObject(Constants.REST_RESPONSE_FLOW_MATCH_KEY);
						if(matchForEachFlow.has(Constants.REST_RESPONSE_IPV4_SRC_KEY) && matchForEachFlow.has(Constants.REST_RESPONSE_IPV4_DST_KEY)
								&& matchForEachFlow.has(Constants.REST_RESPONSE_ETH_SRC_KEY) && matchForEachFlow.has(Constants.REST_RESPONSE_ETH_DST_KEY)  
								&& matchForEachFlow.has(Constants.REST_RESPONSE_ETH_TYPE_KEY) && matchForEachFlow.has(Constants.REST_RESPONSE_IP_PROTOCOL_KEY)
								&& matchForEachFlow.has(Constants.REST_RESPONSE_IN_PORT_KEY) && matchForEachFlow.has(Constants.REST_RESPONSE_TCP_DST_PORT_KEY)
								&& matchForEachFlow.has(Constants.REST_RESPONSE_TCP_SRC_PORT_KEY)){
							String srcIp = matchForEachFlow.getString(Constants.REST_RESPONSE_IPV4_SRC_KEY);
							String dstIp = matchForEachFlow.getString(Constants.REST_RESPONSE_IPV4_DST_KEY);
							int tcpSrc = Integer.parseInt(matchForEachFlow.getString(Constants.REST_RESPONSE_TCP_SRC_PORT_KEY));
							int tcpDst = Integer.parseInt(matchForEachFlow.getString(Constants.REST_RESPONSE_TCP_DST_PORT_KEY));
							String ethSrc = matchForEachFlow.getString(Constants.REST_RESPONSE_ETH_SRC_KEY);
							String ethDst = matchForEachFlow.getString(Constants.REST_RESPONSE_ETH_DST_KEY);
							String ethType = matchForEachFlow.getString(Constants.REST_RESPONSE_ETH_TYPE_KEY);
							String ipProtocol = matchForEachFlow.getString(Constants.REST_RESPONSE_IP_PROTOCOL_KEY);
							int inPort = Integer.parseInt(matchForEachFlow.getString(Constants.REST_RESPONSE_IN_PORT_KEY));
							
							OpenflowMatchEntry entry = new OpenflowMatchEntry();
							entry
								.setIpv4Src(srcIp)
								.setIpv4Dst(dstIp)
								.setIpProtocol(ipProtocol)
								.setEthSrc(ethSrc)
								.setEthDst(ethDst)
								.setEthProtocol(ethType)
								.setTcpSrcPort(tcpSrc)
								.setTcpDstPort(tcpDst)
								.setInPort(inPort);

							this.analyzer.addEntry(entry, eachFlowByteCount);
						}
					}
				}
				
				currReadTrafficCount = this.analyzer.getNumReadBytes();
				currWriteTrafficCount = this.analyzer.getNumWriteBytes();
				currReplicationTrafficCount = this.analyzer.getNumReplicationBytes();

				if(prevReadTrafficCount != -1 && prevWriteTrafficCount != -1 && prevReplicationTrafficCount != -1 && prevStatCollectTime != -1){
					long statCollectDurationInMilli = currStatCollectTime - prevStatCollectTime;
			
					//the bandwidth estimation need to be done together.
					synchronized(this){
						this.readBandwidth = (1.0 * (currReadTrafficCount - prevReadTrafficCount)) /  statCollectDurationInMilli; 
						this.writeBandwidth = (1.0 * (currWriteTrafficCount - prevWriteTrafficCount)) / statCollectDurationInMilli; 
						this.replicationBandwidth = (1.0 * (currReplicationTrafficCount - prevReplicationTrafficCount)) / statCollectDurationInMilli;
					}					
				}
				
				br.close();
			} catch (MalformedURLException e1) {
				System.err.println("malformed URL at client");
			} catch (IOException e) {
				System.err.println("cannot open connection to rest at client");
			}
			
			//sleep for some time between each measurement
			try {
				Thread.sleep(Constants.DELAY_IN_COLLECTING_STATS);
			} catch (InterruptedException e) {
				System.err.println("cannot sleep at client");
			}
			
			prevStatCollectTime = currStatCollectTime;
			prevReadTrafficCount = currReadTrafficCount;
			prevWriteTrafficCount = currWriteTrafficCount;
			prevReplicationTrafficCount = currReplicationTrafficCount;
		}
	}
}
