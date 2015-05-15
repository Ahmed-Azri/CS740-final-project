package utils;

public class OpenflowMatchEntry {
	private int inPort;
	private String ethSrc;
	private String ethDst;
	private String ethProtocol;
	private String ipv4Src;
	private String ipv4Dst;
	private String ipProtocol;
	private int tcpSrcPort;
	private int tcpDstPort;
	
	private long lastTimeModiy;
	
	public OpenflowMatchEntry setLastTimeModify(long time){
		this.lastTimeModiy = time;
		return this;
	}
	
	public OpenflowMatchEntry setInPort(int inPort){
		this.inPort = inPort;
		return this;
	}
	
	public OpenflowMatchEntry setEthSrc(String ethSrc){
		this.ethSrc = ethSrc;
		return this;
	}
	
	public OpenflowMatchEntry setEthDst(String ethDst){
		this.ethDst = ethDst;
		return this;
	}
	public OpenflowMatchEntry setEthProtocol(String ethProtocol){
		this.ethProtocol = ethProtocol;
		return this;
	}
	public OpenflowMatchEntry setIpv4Src(String ipv4Src){
		this.ipv4Src = ipv4Src;
		return this;
	}
	public OpenflowMatchEntry setIpv4Dst(String ipv4Dst){
		this.ipv4Dst = ipv4Dst;
		return this;
	}
	public OpenflowMatchEntry setIpProtocol(String ipProtocol){
		this.ipProtocol = ipProtocol;
		return this;
	}
	public OpenflowMatchEntry setTcpSrcPort(int tcpSrcPort){
		this.tcpSrcPort = tcpSrcPort;
		return this;
	}
	public OpenflowMatchEntry setTcpDstPort(int tcpDstPort){
		this.tcpDstPort = tcpDstPort;
		return this;
	}
	
	public int getInPort(){
		return this.inPort;
	}
	
	public String getEthSrc(){
		return this.ethSrc;
	}
	
	public String getEthDst(){
		return this.ethDst;
	}
	public String getEthProtocol(){
		return this.ethProtocol;
	}
	public String getIpv4Src(){
		return this.ipv4Src;
	}
	public String getIpv4Dst(){
		return this.ipv4Dst;
	}
	public String getIpProtocol(){
		return this.ipProtocol;
	}
	public int getTcpSrcPort(){
		return this.tcpSrcPort;
	}
	public int getTcpDstPort(){
		return this.tcpDstPort;
	}
	public long getLastTimeModify(){
		return this.lastTimeModiy;
	}
	
	public String toString(){
		return this.ipv4Src + "-" + this.ipv4Dst;
	}
	
	public boolean equals(OpenflowMatchEntry other){
		return this.ethDst.equals(other.ethDst) && this.ethSrc.equals(other.ethSrc) && this.ethProtocol.equals(other.ethProtocol)
				&& this.inPort == other.inPort && this.ipProtocol.equals(other.ipProtocol) && this.ipv4Dst.equals(other.ipv4Dst)
				&& this.ipv4Src.equals(other.ipv4Src) && this.tcpDstPort == other.tcpDstPort && this.tcpSrcPort == other.tcpSrcPort;
	}
	
	public int hashCode(){
		String combined = this.ethDst + "-" + this.ethSrc + "-" + this.ethProtocol + "-" + this.inPort + "-" 
				+ this.ipv4Dst + "-" + this.ipv4Src + "-" + this.ipProtocol + "-" + this.tcpDstPort + "-" + this.tcpSrcPort;
		return combined.hashCode();
	}
}
