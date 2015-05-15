package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrafficAnalyzer {

	private int numReadBytes;
	private int numWriteBytes;
	private int numReplicationBytes;

	private Map<String, String> typeOfTrafficMap;
	private Map<OpenflowMatchEntry, Long> flowEntryToByteCountMap;

	class TrafficAnalyzerEntryCleaner implements Runnable{
		@Override
		public void run() {
			while(true){
				Set<OpenflowMatchEntry> flowEntrySets = null;
				synchronized(flowEntryToByteCountMap){
					flowEntrySets = new HashSet<OpenflowMatchEntry>(flowEntryToByteCountMap.keySet());
				}
				for(OpenflowMatchEntry eachEntry : flowEntrySets){
					long timeFromLastModify = System.currentTimeMillis() - eachEntry.getLastTimeModify();
					if(timeFromLastModify > Constants.TRAFFIC_ANALYZER_FLOW_ENTRY_TIMEOUT_MILLI_SEC){
						synchronized(flowEntryToByteCountMap){
							flowEntryToByteCountMap.remove(eachEntry);
						}
					}
				}

				try {
					Thread.sleep(Constants.TRAFFIC_ANALYZER_SLEEP_TIMEOUT_MILLI_SEC);
				} catch (InterruptedException e) {
					System.err.println(TrafficAnalyzerEntryCleaner.class.getSimpleName() + " cannot sleep during clean up");
				}
			}
		}
	}

	public TrafficAnalyzer(Map<String, String> typeOfTrafficMap){
		this.numReadBytes = 0;
		this.numWriteBytes = 0;
		this.numReplicationBytes = 0;

		this.flowEntryToByteCountMap = new HashMap<OpenflowMatchEntry, Long>();
		this.typeOfTrafficMap = new HashMap<String, String>(typeOfTrafficMap);
		
		new Thread(new TrafficAnalyzerEntryCleaner()).start();
	}
	
	public void addEntry(OpenflowMatchEntry eachMatchEntry, long byteCount){
		long numBytesToAdd = 0;
		if(!this.flowEntryToByteCountMap.containsKey(eachMatchEntry)){
			eachMatchEntry.setLastTimeModify(System.currentTimeMillis());
			synchronized(flowEntryToByteCountMap){
				this.flowEntryToByteCountMap.put(eachMatchEntry, byteCount);
			}
			numBytesToAdd = byteCount;
		} else {
			long numBytesBefore = flowEntryToByteCountMap.get(eachMatchEntry);
			eachMatchEntry.setLastTimeModify(System.currentTimeMillis());
			synchronized(this.flowEntryToByteCountMap){
				this.flowEntryToByteCountMap.put(eachMatchEntry, byteCount);
			}
			numBytesToAdd = byteCount - numBytesBefore;
		}
		String typeOfFlowTraffic = getTypeOfTraffic(eachMatchEntry);
		if(typeOfFlowTraffic != null){
			if(typeOfFlowTraffic.equals(Constants.FLOW_STAT_READ_KEY)){
				this.numReadBytes += numBytesToAdd;
			} else if(typeOfFlowTraffic.equals(Constants.FLOW_STAT_WRITE_KEY)){
				this.numWriteBytes += numBytesToAdd;
			} else if(typeOfFlowTraffic.equals(Constants.FLOW_STAT_REPLICATION_KEY)) {
				this.numReplicationBytes += numBytesToAdd;
			}
		}
	}

	public int getNumberOfEntries(){
		return this.flowEntryToByteCountMap.size();
	}
	
	public int getNumReadBytes(){
		return this.numReadBytes;
	}

	public int getNumWriteBytes(){
		return this.numWriteBytes;
	}

	public int getNumReplicationBytes(){
		return this.numReplicationBytes;
	}

	private String getTypeOfTraffic(OpenflowMatchEntry eachMatchEntry){
		return this.typeOfTrafficMap.get(eachMatchEntry.getIpv4Src() + "-" + eachMatchEntry.getIpv4Dst());
	}
}
