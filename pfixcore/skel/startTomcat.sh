#!/bin/sh

if [ "x$1" = "x-d" ] ; then 
   cmd="jpda run"
else
   cmd="run"
fi

export JAVA_OPTS="-Dcom.sun.management.jmxremote -mx300M -XX:MaxPermSize=128m"
echo "---------------------------"
echo "catalina.sh $cmd"
echo "Using JAVA_OPTS:       $JAVA_OPTS"
echo "Using JPDA_TRANSPORT:  $JPDA_TRANSPORT"
echo "Using JPDA_ADDRESS:    $JPDA_ADDRESS"
echo "Using JPDA_OPTS:       $JPDA_OPTS"
echo "---------------------------"

if [ -e startup.properties ]; then
  java -cp build:`find lib -name "*pfixcore*.jar" -printf "%p:"` org.pustefixframework.tools.TomcatStartupConfigurator startup.properties projects/servletconf/tomcat/conf/server.xml projects/servletconf/tomcat/conf/server-runtime.xml
fi
if [ -e projects/servletconf/tomcat/conf/server-runtime.xml ]; then
  echo "Use configuration created at startup from 'projects/servletconf/tomcat/conf/server-runtime.xml'"
  cmd="$cmd -config conf/server-runtime.xml"
fi
cd ./projects/servletconf/tomcat/
LANG=C ./bin/catalina.sh $cmd 
