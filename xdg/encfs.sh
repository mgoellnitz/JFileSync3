#!/bin/sh
DIR=`dirname $0`/..
java -classpath $DIR/lib/bcprov-jdk15on-1.69.jar:$DIR/lib/JFileSync3.jar:$DIR/lib/slf4j-api-1.7.32.jar EncFSShell $*
