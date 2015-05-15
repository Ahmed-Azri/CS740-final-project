import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


/**
 * HDFSBenchmark. Some part of it is borrowed from TestDFSIO.java in HDFS map reduce client code.
 * This essentially test HDFS read/write throughput by creating files that are read/write in proportion
 * to each other.
 * 
 * @author AllenLiu
 */

public class HDFSBenchmark implements Tool{

	private Configuration config;

	private static final long MEGA = ByteMultiple.MB.value();

	private static final String BASE_DIR_NAME = "hdfs_benchmark/";
	private static final String READ_DIR_NAME = "io_read";
	private static final String WRITE_DIR_NAME = "io_write";
	private static final String BASE_FILE_NAME = "test_hdfs_io_";
	private static final String USAGE =
			"Usage: " + HDFSBenchmark.class.getSimpleName() +
			" [-clean] | [" +
			" -readpercent A" +
			" -writepercent B" +
			" -nrfiles N" +
			" -filesize Size[B|KB|MB|GB|TB] ]";

	static{
		Configuration.addDefaultResource("hdfs-default.xml");
		Configuration.addDefaultResource("hdfs-site.xml");
	}

	static enum ByteMultiple {
		B(1L),
		KB(0x400L),
		MB(0x100000L),
		GB(0x40000000L),
		TB(0x10000000000L);

		private long multiplier;

		private ByteMultiple(long mult) {
			multiplier = mult;
		}

		long value() {
			return multiplier;
		}

		static ByteMultiple parseString(String sMultiple) {
			if(sMultiple == null || sMultiple.isEmpty()) // MB by default
				return MB;
			String sMU = sMultiple.toUpperCase();
			if(B.name().toUpperCase().endsWith(sMU))
				return B;
			if(KB.name().toUpperCase().endsWith(sMU))
				return KB;
			if(MB.name().toUpperCase().endsWith(sMU))
				return MB;
			if(GB.name().toUpperCase().endsWith(sMU))
				return GB;
			if(TB.name().toUpperCase().endsWith(sMU))
				return TB;
			throw new IllegalArgumentException("Unsupported ByteMultiple "+sMultiple);
		}
	}

	class HDFSWriteDaemon implements Runnable{

		private Configuration config;
		private FileSystem fs;
		private int numberOfFiles;
		private long bytes;
		private long writeExecutionTime;
		
		public HDFSWriteDaemon(FileSystem fs, Configuration config, int numberOfFiles, long bytes){
			this.numberOfFiles = numberOfFiles;
			this.bytes = bytes;
			this.writeExecutionTime = 0;
			this.fs = fs;
			this.config = config;
		}

		public long getWriteExecutionTime(){
			return this.writeExecutionTime;
		}
		
		@Override
		public void run() {
			for(int i = 1; i <= this.numberOfFiles; i++){
				String fileName = BASE_FILE_NAME + i;
				System.out.println(HDFSWriteDaemon.class.getSimpleName() + ": writing " + fileName);
				try {
					this.writeExecutionTime += writeToHDFSWithBytes(this.fs, new Path(getWriteDir(this.config), fileName), this.bytes);
				} catch (IOException e) {
					System.err.println(HDFSWriteDaemon.class.getSimpleName() + " write failed");
				}
			}
		}
	}

	class HDFSReadDaemon implements Runnable{
		
		private Configuration config;
		private FileSystem fs;
		private int numberOfFiles;
		private long readExecutionTime;
		
		public HDFSReadDaemon(FileSystem fs, Configuration config, int numberOfFiles){
			this.readExecutionTime = 0;
			this.config = config;
			this.fs = fs;
			this.numberOfFiles = numberOfFiles;
		}

		public long getReadExecutionTime(){
			return this.readExecutionTime;
		}
		
		@Override
		public void run() {
			for(int i = 1; i <= this.numberOfFiles; i++){
				String fileName = BASE_FILE_NAME + i;
				System.out.println(HDFSReadDaemon.class.getSimpleName() + ": reading " + fileName);
				Path filePath = new Path(getReadDir(this.config), fileName);
				BufferedReader br = null;
                try {
                	long startTime = System.currentTimeMillis();
					br = new BufferedReader(new InputStreamReader(fs.open(filePath)));
					String line = null;
                    line = br.readLine();
                    while (line != null){
                            line=br.readLine();
                    }
                    long endTime = System.currentTimeMillis();
                    this.readExecutionTime += endTime - startTime;
				} catch (IOException e) {
					System.err.println(HDFSReadDaemon.class.getSimpleName() + " read failed");
				} finally {
					if (br != null){
						try {
							br.close();
						} catch (IOException e) {
							System.err.println(HDFSReadDaemon.class.getSimpleName() + " can't even close buffer");
						}
					}
				}

			}
		}

	}

	public HDFSBenchmark(){
		this.config = new Configuration();
	}

	public static void main(String args[]){
		HDFSBenchmark bench = new HDFSBenchmark();
		int res = -1;
		try {
			res = ToolRunner.run(bench, args);
		} catch(Exception e) {
			System.err.print(StringUtils.stringifyException(e));
			res = -2;
		}
		if(res == -1)
			System.err.println(USAGE);
		System.exit(res);
	}

	@Override
	public Configuration getConf() {
		return this.config;
	}

	@Override
	public void setConf(Configuration conf) {
		this.config = conf;
	}

	@Override
	public int run(String[] args) throws Exception {
		boolean clean = false;
		long nrBytes = 1*MEGA;
		int nrFiles = 1;
		int readPercent = 0;
		int writePercent = 0;

		if (args.length == 0) {
			System.err.println("Missing arguments.");
			return -1;
		}

		for (int i = 0; i < args.length; i++) { // parse command line
			if (args[i].toLowerCase().startsWith("-readpercent")) {
				readPercent = Integer.parseInt(args[++i]);
			} else if (args[i].equalsIgnoreCase("-writepercent")) {
				writePercent = Integer.parseInt(args[++i]);
			} else if (args[i].equalsIgnoreCase("-clean")) {
				clean = true;
			} else if (args[i].equalsIgnoreCase("-nrfiles")) {
				nrFiles = Integer.parseInt(args[++i]);
			} else if (args[i].equalsIgnoreCase("-filesize")) {
				nrBytes = parseSize(args[++i]);
			} else {
				System.err.println("Illegal argument: " + args[i]);
				return -1;
			}
		}

		FileSystem fs = FileSystem.get(config);

		if (clean) {
			cleanup(fs);
			return 0;
		}

		if(writePercent + readPercent != 100){
			System.err.println("Illegal argument: read + write percentage must equal to 100");
			return -1;
		}
		int numReadFiles = (int)Math.ceil((0.01 * readPercent * nrFiles));
		int numWriteFiles = (int)Math.ceil((0.01 * writePercent * nrFiles));
		System.out.println("clean = " + clean);
		System.out.println("readPercent = " + readPercent);
		System.out.println("writePercent = " + writePercent);
		System.out.println("nrFiles = " + nrFiles);
		System.out.println("numReadFiles = " + numReadFiles);
		System.out.println("numWriteFiles = " + numWriteFiles);
		System.out.println("nrBytes (MB) = " + toMB(nrBytes));
		System.out.println("baseDir = " + getBaseDir(config));

		prepareExperiment(fs, numReadFiles, nrBytes);
		long[] results = startExperiment(fs, numReadFiles, numWriteFiles, nrBytes);		//results[0] read total time in sec, results[1] write total time
		analyzeResult(numReadFiles, numWriteFiles, nrBytes, results);
		cleanup(fs);
		return 0;
	}

	private void analyzeResult(int numReadFiles, int numWriteFiles, long nrBytes, long[] readWriteExecutionTime){
		double readThroughput = toMB(numReadFiles * nrBytes / (readWriteExecutionTime[0] / 1000.0));
		double writeThroughput = toMB(numWriteFiles * nrBytes / (readWriteExecutionTime[1] / 1000.0));

		System.out.println("Experiment result: ");
		System.out.println("total read execution time in sec: " + (readWriteExecutionTime[0]/1000.0));
		System.out.println("total write execution time in sec: " + (readWriteExecutionTime[1]/1000.0));
		System.out.println("Read throughput (Mb/sec): " + readThroughput);
		System.out.println("Write throughput (Mb/sec): " + writeThroughput);
	}
	
	private long[] startExperiment(FileSystem fs, int numReadFiles, int numWriteFiles, long bytesOfFiles) throws InterruptedException{
		long[] result = new long[2]; 			//first read, then write, unit in second
		HDFSWriteDaemon writeDaemon = new HDFSWriteDaemon(fs, this.config,numWriteFiles, bytesOfFiles);
		HDFSReadDaemon readDaemon = new HDFSReadDaemon(fs, this.config, numReadFiles);
		Thread writeThread = new Thread(writeDaemon);
		Thread readThread = new Thread(readDaemon);
		writeThread.start();
		readThread.start();
		writeThread.join();
		readThread.join();
		long writeTime = writeDaemon.getWriteExecutionTime();
		long readTime = readDaemon.getReadExecutionTime();
		result[0] = readTime;
		result[1] = writeTime;
		return result;
	}

	private void prepareExperiment(FileSystem fs, int numReadFiles, long bytesOfFiles) throws FileNotFoundException, IOException{
		//make the read write directory
		if(fs.mkdirs(new Path(getBaseDir(this.config)))){
			System.out.println("success making base dir: " + getBaseDir(this.config));
		}
		if(fs.mkdirs(getReadDir(this.config))){
			System.out.println("success making read dir " + getReadDir(this.config));
		}
		if(fs.mkdirs(getWriteDir(this.config))){
			System.out.println("success making write dir " + getWriteDir(this.config));
		}
	
		//first create necessary read files
		int numberOfReadFilesInHDFS =0; 
		RemoteIterator<LocatedFileStatus> itRead = fs.listFiles(getReadDir(this.config), true);
		while(itRead.hasNext()){
			numberOfReadFilesInHDFS++;
			itRead.next();
		}
		System.out.println(HDFSBenchmark.class.getSimpleName() + " need to create " + numberOfReadFilesInHDFS + " files for read.");
		if(numReadFiles > numberOfReadFilesInHDFS){
			for(int i = numberOfReadFilesInHDFS + 1; i <= numReadFiles; i++){
				String currReadFileName = BASE_FILE_NAME + i;
				writeToHDFSWithBytes(fs, new Path(getReadDir(this.config), currReadFileName), bytesOfFiles);
			}
		}
		
		//then remove all the files in the write folder
		RemoteIterator<LocatedFileStatus> itWrite = fs.listFiles(getWriteDir(this.config), true);
		while(itWrite.hasNext()){
			LocatedFileStatus eachWriteFile = itWrite.next();
			Path writeFilePath = eachWriteFile.getPath();
			fs.delete(writeFilePath, true);
		}
	}

	private long writeToHDFSWithBytes(FileSystem fs, Path filePath, long bytesOfFiles) throws IOException{
		byte[] bytesInFile = new byte[(int)bytesOfFiles];
		new Random().nextBytes(bytesInFile);
		long startTime = System.currentTimeMillis();
		FSDataOutputStream writeStream = fs.create(filePath);
		writeStream.write(bytesInFile);
		writeStream.close();
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}

	private void cleanup(FileSystem fs)
			throws IOException {
		System.out.println("Cleaning up test files");
		fs.delete(new Path(getBaseDir(config)), true);
	}

	/**
	 * Returns size in bytes.
	 * 
	 * @param arg = {d}[B|KB|MB|GB|TB]
	 * @return
	 */
	private static long parseSize(String arg) {
		String[] args = arg.split("\\D", 2);  // get digits
		assert args.length <= 2;
		long nrBytes = Long.parseLong(args[0]);
		String bytesMult = arg.substring(args[0].length()); // get byte multiple
		return nrBytes * ByteMultiple.parseString(bytesMult).value();
	}

	private static double toMB(double bytes) {
		return bytes/MEGA;
	}

	private static String getBaseDir(Configuration conf) {
		return conf.get("test.build.data", BASE_DIR_NAME + "TestHDFSProportionIO");
	}

	private static Path getWriteDir(Configuration conf) {
		return new Path(getBaseDir(conf), WRITE_DIR_NAME);
	}

	private static Path getReadDir(Configuration conf) {
		return new Path(getBaseDir(conf), READ_DIR_NAME);
	}
}
