#!/bin/sh

echo ---------------------------

if [ "$1" == "-d" ] ; then 
   echo Remote debug, will wait for connect to localhost:5000
   debug="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=5000"
else
    echo No remote debug, enable it with $0 "-d"
    debug=""
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:`pwd`/lib/jni/build
export JAVA_OPTS="-mx200M $debug"
export JSSE_HOME="`pwd`/lib/jsse"

echo "starting tomcat with java-opts '$JAVA_OPTS'"

echo ---------------------------

cd ./projects/servletconf/tomcat/
LANG=C ./bin/catalina.sh run


