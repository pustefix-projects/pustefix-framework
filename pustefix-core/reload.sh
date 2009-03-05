#!/bin/sh

#################################################
# WebappAdminClient script for webapp reloading #
#################################################

PFX_USAGE="Usage: $0 <project>|<host> ..."

if [ `dirname $0` != "." ]
then
  echo "Script has to be started directly from within its owning directory!"
  exit 1
fi

if [ $# -lt 1 ] 
then
  echo "Missing arguments! You must specify at least one project or host!"
  echo "$PFX_USAGE"
  exit 1
fi

PFX_DIR=`pwd`
PFX_PID=`ps --format "pid cmd" -C java | grep "\-Dcatalina.home=$PFX_DIR/projects/servletconf" | sed "s/ *\([0-9]\+\) \+.*/\1/"`

if [ -z $PFX_PID ]
then
  echo "Can't find Java process using the ps command!"
  exit 2
fi

PFX_LIBS=`find $PFX_DIR lib -name "*pfixcore*.jar" -printf "%p:"`
java -cp build:$PFX_LIBS org.pustefixframework.admin.WebappAdminClient -p $PFX_PID -c reload $*
