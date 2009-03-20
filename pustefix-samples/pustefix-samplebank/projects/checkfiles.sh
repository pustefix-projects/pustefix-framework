#!/bin/sh

export CLASSPATH=`ls \`pwd\`/../lib/*.jar | xargs echo | sed -e 's/ /:/g'`:`pwd`/../build
export LANG=C

find . -type f | grep "/txt/" | grep "\.xml$"  > .ALLINCFILES
find . -type f | grep "/img/" | grep "\.\(gif\|jpg\|jpeg\|JPG\|JPEG\)$"  > .ALLIMAGES
find . -type f | grep "depend.xml$"  > .ALLPROJECTS

java -mx500M de.schlund.pfixcore.util.CheckIncludes CHECKOUTPUT.xml .ALLPROJECTS .ALLINCFILES .ALLIMAGES
java -mx500M com.icl.saxon.StyleSheet  CHECKOUTPUT.xml core/build/unused.xsl  > UnusedInfo.txt
java -mx500M com.icl.saxon.StyleSheet  CHECKOUTPUT.xml core/build/cleanup.xsl > Cleanup.xml

