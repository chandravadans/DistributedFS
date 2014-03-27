#!/bin/bash

#Comments out a set of lines in C/C++ style
echo "Usage example: comments.sh 1,10 *.java"

for i in $(ls $2)
do
	sed -i '$1 s/^/\/\//' $i
done
