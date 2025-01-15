#!/bin/bash
#
# Copyright 2020-2025 Martin Goellnitz
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
usage() {
   echo "Usage: $MYNAME [-h] [-l name] [push]" 1>&2
   echo "" 1>&2
   echo "  -h         this page" 1>&2
   echo "  -l name    latest release name" 1>&2
   echo "     push    immediately push the result" 1>&2
   echo "" 1>&2
   exit 1
}

CURRENT=$(git tag -l|sort -k1.2n|tail -1)
if [ -z "$CURRENT" ] ; then
  CURRENT="3.0.1"
fi

while getopts "hl:" opt ; do
  case "${opt}" in
    h)
      usage
      ;;
    l)
      CURRENT=$OPTARG
      ;;
    *)
      usage
      ;;
  esac
done
shift $((OPTIND-1))

echo Current Release $CURRENT
COUNTER=$(echo $CURRENT|sed -e 's/^[0-9][0-9]*\.[0-9][0-9]*\.//g')
COUNTER=$[ $COUNTER + 1 ]
TAG="$(echo $CURRENT|sed -e 's/[0-9][0-9]$//g')$COUNTER"
echo Next Release $TAG
git tag -a -m "Release $TAG" $TAG
git commit -m "Move to next Release" build.gradle src/jfs/resources/conf/JFSConfig.properties xdg/JFileSync3.desktop
