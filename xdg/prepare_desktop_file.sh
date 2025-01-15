#!/bin/sh
DIR=$(dirname $(readlink -f $0))
if [ -f $DIR/../lib/JFileSync3.jar ] ; then
  if [ -f ~/.config/user-dirs.dirs ] ; then
    DESKTOP_DIR=~/$(cat ~/.config/user-dirs.dirs |grep XDG_DESKTOP_DIR|sed -e 's/^.*HOME.\(.*\)./\1/g')
  else
    if [ -f ~/.config/user-dirs.dirs ] ; then
      DESKTOP_DIR=~/$(cat /etc/xdg/user-dirs.defaults |grep DESKTOP|sed -e 's/^.*=\(.*\)$/\1/g')
    else
      DESKTOP_DIR=~/Desktop
    fi
  fi
  PATTERN=$(echo $DIR|sed -e 's/\//\\\//g')\\/
  sed -i.bak -e "s/^Exec=.*/Exec=${PATTERN}JFileSync3/" $DIR/JFileSync3.desktop
  sed -i.bak -e "s/^Icon=.*/Icon=${PATTERN}JFileSync3.png/" $DIR/JFileSync3.desktop
  rm $DIR/JFileSync3.desktop.bak
fi
