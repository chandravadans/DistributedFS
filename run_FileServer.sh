#!/bin/bash

if [ $# -lt 2 ]
then
	echo "Usage: run.sh <ip addr> <port>"
	echo "Note: Get the IP Address of the server using something like ipconfig, if the system has to function correctly"
	exit
fi
cd FileServer
javac *.java -cp ../:../utils:../Registry
rmic ReadWriteInterfaceImpl
echo "Starting rmiregistry.."
rmiregistry -J-Djava.security.policy=rmi.policy $2 &
echo "Starting Server.."
java -Djava.security.policy=rmi.policy Server $1 $2

