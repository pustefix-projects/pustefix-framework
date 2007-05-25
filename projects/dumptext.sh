#!/bin/sh

export CLASSPATH=`ls \`pwd\`/../lib/*.jar | xargs echo | sed -e 's/ /:/g'`:`pwd`/../build
export LANG=C

echo
echo -----------------------------------------------------------------------------------------------------------
echo Going to call:  java -mx500M de.schlund.pfixcore.util.DumpText `pwd` $@ 
echo ----------------------------------------------------------------------------------------------------------- 
echo
echo CAUTION:
echo 'CAUTION:        MAKE SURE THAT THE PROJECT YOU ARE ABOUT TO DUMP HAS BEEN COMPLETELY GENERATED!'
echo 'CAUTION:                  (call "ant generate" in the top level directory if unsure)'
echo CAUTION:
echo
 
java -mx500M de.schlund.pfixcore.util.DumpText `pwd` $@ 


