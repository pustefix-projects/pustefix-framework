#!/bin/sh

export CLASSPATH=`cd ..; make -s echo-classpath`
export LANG=C

find . -type f | grep "/txt/" | grep "\.xml$"  > .ALLINCFILES
find . -type f | grep "/img/" | grep "\.\(gif\|jpg\|jpeg\|JPG\|JPEG\)$"  > .ALLIMAGES
find . -type f | grep "depend.xml$"  > .ALLPROJECTS

java de.schlund.pfixcore.util.CheckIncludes CHECKOUTPUT.xml .ALLPROJECTS .ALLINCFILES .ALLIMAGES
