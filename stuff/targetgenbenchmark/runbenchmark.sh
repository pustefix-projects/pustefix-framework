#!/bin/bash
cd `dirname $0`/../..
classpath="build:projects/servletconf/tomcat/common/lib/servlet-api.jar:"
for jar in lib/*.jar; do
classpath=$classpath:$jar
done

#jpda_opts="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
jpda_opts=""

java $jpda_opts -classpath $classpath de.schlund.pfixcore.testsuite.util.TargetGeneratorBenchmarkUtil $@
