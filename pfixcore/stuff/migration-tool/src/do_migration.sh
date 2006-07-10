#!/bin/sh
basepath=`dirname $0`
saxonpath=`echo $basepath/saxon*.jar`
classpath=$saxonpath:$basepath/migration-tool.jar
java -classpath $classpath de.schlund.util.configmigration.MigrationTool
