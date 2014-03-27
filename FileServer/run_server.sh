#!/bin/bash

if [ $# -lt 2 ]
then
	echo "Usage: run.sh <ip addr> <port>"
	echo "Note: Get the IP Address of the server using something like ipconfig, if the system has to function correctly"
	exit
fi
javac *.java
rmic ReadWriteInterfaceImpl
echo "Starting rniregistry.."
rmiregistry -J-Djava.security.policy=rmi.policy $2 &
echo "Starting Server.."
java -Djava.security.policy=rmi.policy FileServer $1 $2

