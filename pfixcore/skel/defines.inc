PROJECT  = pfixcore_skel
SRCROOT  = src
BUILDDIR = build

export CLASSPATH  = $(shell ./bin/setClassPath.sh ${PWD}):${PWD}/projects/servletconf/tomcat/common/lib/servlet.jar
export JIKESPATH  = ${CLASSPATH}:${JDK_HOME}/jre/lib/rt.jar
export JAVAVM     = java
export JAVAC      = jikes -d ${PWD}/$(BUILDDIR) -sourcepath ${PWD}/$(SRCROOT) -g +D
export JAR        = jar

# add all directories containing source code of your projects here
# e.g. ALLDIRS=$(SRCROOT)/org/my/projectFoo \
#              $(SRCROOT)/org/my/projectBar

ALLDIRS = $(SRCROOT)

