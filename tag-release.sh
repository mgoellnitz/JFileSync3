#!/bin/bash
#
# Copyright 2020 Martin Goellnitz
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
function usage {
   echo "Usage: $MYNAME [-h] [-l name] [push]"
   echo ""
   echo "  -h         this page"
   echo "  -l name    latest release name"
   echo "     push    immediately push the result"
   echo ""
   exit
}

CURRENT=$(git tag -l|sort -k1.2n|tail -1)
if [ -z "$CURRENT" ] ; then
  CURRENT="3.0.1"
fi

PSTART=`echo $1|sed -e 's/^\(.\).*/\1/g'`
while [ "$PSTART" = "-" ] ; do
  if [ "$1" = "-h" ] ; then
    usage
    exit
  fi
  if [ "$1" = "-l" ] ; then
    shift
    CURRENT=$1
  fi
  shift
  PSTART=`echo $1|sed -e 's/^\(.\).*/\1/g'`
done

echo Current Release $CURRENT
COUNTER=$(echo $CURRENT|sed -e 's/^[0-9][0-9]*\.[0-9][0-9]*\.//g')
COUNTER=$[ $COUNTER + 1 ]
TAG="$(echo $CURRENT|sed -e 's/[0-9][0-9]$//g')$COUNTER"
echo Next Release $TAG
sed -i.back -e "s/3.0-SNAPSHOT/$TAG/g" build.gradle
sed -i.back -e "s/3.0-SNAPSHOT/$TAG/g" src/jfs/resources/conf/JFSConfig.properties
sed -i.back -e "s/3.0-SNAPSHOT/$TAG/g" xdg/JFileSync3.desktop
git commit -m "Release $TAG" build.gradle src/jfs/resources/conf/JFSConfig.properties xdg/JFileSync3.desktop
git tag -a -m "Release $TAG" $TAG
mv build.gradle.back build.gradle
mv xdg/JFileSync3.desktop.back xdg/JFileSync3.desktop
mv src/jfs/resources/conf/JFSConfig.properties.back src/jfs/resources/conf/JFSConfig.properties
git commit -m "Move to next Release" build.gradle src/jfs/resources/conf/JFSConfig.properties xdg/JFileSync3.desktop
