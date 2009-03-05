#!/bin/sh

export CLASSPATH=`ls \`pwd\`/../lib/*.jar | xargs echo | sed -e 's/ /:/g'`:`pwd`/../build
export LANG=C

echo
echo -----------------------------------------------------------------------------------------------------------
echo Going to call:  java -mx500M de.schlund.pfixcore.util.ImportText `pwd` $@ 
echo ----------------------------------------------------------------------------------------------------------- 
echo
 
java -mx500M de.schlund.pfixcore.util.ImportText `pwd` $@ 


