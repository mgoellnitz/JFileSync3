#!/bin/sh
DIR=$(dirname $(dirname $(readlink -f $0)))
if [ -f $DIR/../lib/JFileSync3.jar ] ; then
  USER_DIRS="${XDG_CONFIG_HOME:-$HOME/.config}/user-dirs.dirs"
  if [ -f ${USER_DIRS} ] ; then
    DESKTOP_DIR=~/$(cat ${USER_DIRS} |grep XDG_DESKTOP_DIR|sed -e 's/^.*HOME.\(.*\)./\1/g')
  else
    if [ -f ~/.config/user-dirs.dirs ] ; then
      DESKTOP_DIR=~/$(cat /etc/xdg/user-dirs.defaults |grep DESKTOP|sed -e 's/^.*=\(.*\)$/\1/g')
    else
      DESKTOP_DIR=~/Desktop
    fi
  fi
  PATTERN=$(echo $DIR|sed -e 's/\//\\\//g')\\/
  sed -i.bak -e "s/^Exec=.*/Exec=${PATTERN}\\/bin\\/JFileSync3/" $DIR/share/applications/io.github.mgoellnitz.JFileSync3.desktop
  sed -i.bak -e "s/^Icon=.*/Icon=${PATTERN}\\/share\\/icons\\/hicolor\\/64x64\\/apps\\/io.github.mgoellnitz.JFileSync3.png/" $DIR/share/applications/io.github.mgoellnitz.JFileSync3.desktop
  rm $DIR/JFileSync3.desktop.bak
fi
