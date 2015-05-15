# CS740-final-project

All the software you need to run the COLAB is here. We tested most of the work on CloudLab APT cluster. So some configuration in the software
is targeted toward APT type of machine specifically.
<br />
Also, you need the software in this repo to run the experiment as we adjust and hacked some of them to ensure that it works on APT cluster
and COLAB. 
<br />
HDFS-OpenFlow: This is where the server Daemon resides. It is attached inside HDFS source code as well so it's in
hadoop-common. But it might be worthwhile to have a separate modules for testing.
<br />
HDFSBenchmark: This is where we designed the benchmark to test if the COLAB can improve the throughput given that we have equal amount of 
read and write traffic.
<br />
Openflow-HDFS: This is the client side daemon attached to the floodlight controller. It does the main job in COLAB, namely 
collecting stats, calculating slow down, and send it over to server daemon in HDFS.
<br />
dataparser: a dataparser we wrote to extract data from openvswitch flow table to do traffic analysis.
<br />
floodlight: open source SDN controller we used. To compile, run ./compile.sh. To run, run ./run.sh
<br />
google-protobuf-2.5.0: this is needed to compile the Hadoop cluster.
<br />
hadoop-common: this is the hadoop cluster. It also contains our server daemon to accept the slow downs
and then sleep for that amount of time.
<br />
setup-configuration: contains information about how to set up Hadoop, floodlight, and openvswitch 
in CloudLab APT cluster.