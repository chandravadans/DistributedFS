#!/bin/bash

if [ $# -lt 3 ]
then
	echo "Usage: run.sh <ip_filename> <ip addr of server> <port of server>"
	exit
fi
javac *.java
java -Djava.security.policy=rmi.policy Client $1 $2 $3
