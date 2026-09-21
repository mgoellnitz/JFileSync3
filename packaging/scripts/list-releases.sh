#!/bin/sh
#
# Copyright 2026 Martin Goellnitz
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
MYNAME=$(basename $0)

usage() {
   echo "Usage: $MYNAME [-h]" 1>&2
   echo "" 1>&2
   echo "  -h         this page" 1>&2
   echo "" 1>&2
}

while getopts "h" opt ; do
  case "${opt}" in
    h)
      usage
      exit
      ;;
    *)
      usage
      exit 1
      ;;
  esac
done
shift $((OPTIND-1))

cat packaging/xdg/JFileSync3.metainfo.xml|head -35
(git for-each-ref --sort='-creatordate' --format '%(refname:short)_%(creatordate)' refs/tags) | \
while IFS= read -r release ; do
  TAG=$(echo $release|cut -d '_' -f 1)
  DATE=$(echo $release|cut -d '_' -f 2|cut -d ' ' -f 1-5)
  D=$(date -d "$DATE" +%Y-%m-%d)
  echo '    <release version="'$TAG'" date="'$D'"/>'
done
cat packaging/xdg/JFileSync3.metainfo.xml|tail -25
