#!/bin/sh

echo ---------------------------

if [ "$1" == "-d" ] ; then 
   echo Remote debug, will wait for connect to localhost:5000
   debug="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=5000"
else
    echo No remote debug, enable it with $0 "-d"
    debug=""
fi

export JAVA_OPTS="-server -mx200M $debug"
export JSSE_HOME="`pwd`/lib/jsse"

echo "starting tomcat with java-opts '$JAVA_OPTS'"

echo ---------------------------

cd ./example/servletconf/tomcat/
LANG=C ./bin/catalina.sh run


