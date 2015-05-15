package utils;

public class Constants {
	//Main
	public static final String READ_OPT = "read";
	public static final String LOG_STAT_COLLECTOR_LENGTH_OPTION = "stat_collector_length";

	public static final String IP_PORT_PAIR_DELIMITER = ",";
	
	//for OpenflowHDFSServiceClient
	public static final int UNIT_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC = 30;
	public static final int MAX_BANDWIDTH_SLOW_DOWN_DELAY_IN_MILLI_SEC = 2000;
	public static final int DEFAULT_SENDING_RATE_DELAY_IN_MILLI_SEC = 1000;
	public static final int THRESHOLD_FOR_CHANGING_DELAY_IN_MILLI_SEC = 150;

	
	
	//for stat collector
	public static final int DELAY_IN_COLLECTING_STATS = 500;
	public static final String SWITCH_ID = "00:00:4e:56:17:ee:e5:43";
	public static final String FLOODLIGHT_REST_URL_FOR_FLOW_STAT = "http://localhost:8080/wm/core/switch/all/flow/json";
	
	public static final String REST_RESPONSE_FLOWS_KEY = "flows";
	public static final String REST_RESPONSE_IPV4_SRC_KEY = "ipv4_src";
	public static final String REST_RESPONSE_IPV4_DST_KEY = "ipv4_dst";
	public static final String REST_RESPONSE_TCP_SRC_PORT_KEY = "tcp_src";
	public static final String REST_RESPONSE_TCP_DST_PORT_KEY = "tcp_dst";
	public static final String REST_RESPONSE_IN_PORT_KEY = "in_port";
	public static final String REST_RESPONSE_ETH_SRC_KEY = "eth_src";
	public static final String REST_RESPONSE_ETH_DST_KEY = "eth_dst";
	public static final String REST_RESPONSE_ETH_TYPE_KEY = "eth_type";
	public static final String REST_RESPONSE_IP_PROTOCOL_KEY = "ip_proto";

	
	public static final String REST_RESPONSE_FLOW_MATCH_KEY = "match";
	public static final String REST_RESPONSE_FLOW_BYTE_COUNT = "byteCount";
	public static final String FLOW_STAT_READ_KEY = "read";
	public static final String FLOW_STAT_WRITE_KEY = "write";
	public static final String FLOW_STAT_REPLICATION_KEY = "replication";


	//for traffic analyzer
	public static final long TRAFFIC_ANALYZER_FLOW_ENTRY_TIMEOUT_MILLI_SEC = 10000;
	public static final long TRAFFIC_ANALYZER_SLEEP_TIMEOUT_MILLI_SEC = 1000;
}
