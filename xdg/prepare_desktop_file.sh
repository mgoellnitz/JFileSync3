#!/bin/sh
#
# Copyright 2024-2026 Martin Goellnitz
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
DIR=$(dirname $(readlink -f $0))
if [ -f $DIR/../lib/JFileSync3.jar ] ; then
  LIBDIR=$(dirname $(readlink -f $DIR/../lib/JFileSync3.jar))
  PATTERN=$(echo $DIR|sed -e 's/\//\\\//g')\\/
  IMAGE_FILE=$(find $LIBDIR -name "JFileSync3.png"|sed -e 's/\//\\\//g')
  sed -i.bak -e "s/^Exec=.*/Exec=${PATTERN}JFileSync3/" $DIR/JFileSync3.desktop
  sed -i.bak -e "s/^Icon=.*/Icon=$IMAGE_FILE/" $DIR/JFileSync3.desktop
  rm -f $DIR/JFileSync3.desktop.bak
fi
