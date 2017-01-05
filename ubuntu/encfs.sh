#!/bin/sh
DIR=`dirname $0`/..
java -classpath $DIR/lib/bcprov-jdk15on-1.55.jar:$DIR/lib/JFileSync3.jar:$DIR/lib/slf4j-api-1.7.22.jar EncFSShell $*
