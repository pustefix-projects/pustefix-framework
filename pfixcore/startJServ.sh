#!/bin/sh

export CLASSPATH=`make -s echo-classpath-jserv`
LANG=C java org.apache.jserv.JServ ./example/servletconf/jserv/jserv.prop

