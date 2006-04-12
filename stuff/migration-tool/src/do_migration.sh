#!/bin/sh
basepath=`dirname $0`
classpath=$basepath/saxon*.jar:$basepath/migration-tool.jar
java -classpath $classpath de.schlund.util.configmigration.MigrationTool
