#!/bin/sh
DIR=$(dirname $(readlink -f $0))
if [ -f $DIR/../lib/JFileSync3.jar ] ; then
  PATTERN=$(echo $DIR|sed -e 's/\//\\\//g')\\/
  IMAGE_FILE=$(find $DIR -name "JFileSync3.png"|sed -e 's/\//\\\//g')
  sed -i.bak -e "s/^Exec=.*/Exec=${PATTERN}JFileSync3/" $DIR/JFileSync3.desktop
  sed -i.bak -e "s/^Icon=.*/Icon=${IMAGE_FILE}/" $DIR/JFileSync3.desktop
  rm $DIR/JFileSync3.desktop.bak
fi
