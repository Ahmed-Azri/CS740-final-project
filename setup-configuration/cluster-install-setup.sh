#!/bin/bash

# install necessary software update and package
sudo apt-get -y update
sudo apt-get -y install openjdk-7-jre openjdk-7-jdk curl maven

# install protobuf 2.5.0
cd /local/
sudo git clone https://github.com/ZihaoAllen/google-protobuf-2.5.0.git
cd google-protobuf-2.5.0
sudo ./configure --prefix=/usr
sudo make
sudo make install



# install hadoop from source
cd ..
sudo git clone https://github.com/ZihaoAllen/CS740-project.git
cd CS740-project
sudo mvn clean package -Pdist -DskipTests