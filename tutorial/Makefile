LANG=C

PROJECT = pfixtutorial
SRCROOT = src
BUILDDIR  = build

################################################################
# normally, there's no need to change anything below this line #
################################################################

PFIXCORE_DATA=$(shell ls -1 lib/pfixcore-data-*.tar.gz)

export CLASSPATH = $(shell ./bin/setClassPath.sh `pwd`)
BUILDPATH        = ${CLASSPATH}:${PWD}/lib/pfixcore-0.2.4.jar:${JDK_HOME}/jre/lib/rt.jar


JAR = jar
#export JARAUTOTAG = AUTO_TAG_$(shell  date +%s)

TRFBUILD      = de.schlund.pfixcore.util.MultiTransform
TRFBUILDCLASS = de/schlund/pfixcore/util/MultiTransform.class
PFIXSREQCLASS = de/schlund/pfixxml/PfixServletRequest.class

SRCROOT     = src
JAVACR     = $(SRCROOT)/de/schlund
ALLDIRS = \
        $(JAVACR)/pfixtutorial \
        $(JAVACR)/pfixtutorial/iwrapper \


BUILD_LIB   = projects/core/build/iwrapper.xsl 
ALLWRAPPERS = $(shell find $(ALLDIRS) -maxdepth 1 -name "*.iwrp")
WRAPPERS    = $(ALLWRAPPERS:.iwrp=.java);

JAVA = java
JAVAC     = jikes -classpath $(BUILDPATH) -d ${PWD}/$(BUILDDIR) -sourcepath ${PWD}/$(SRCROOT) -g +D
ALLJAVA   = $(shell find $(ALLDIRS) -maxdepth 1 -name "*.java")

###################################################################

all: check_permissions compile compile_xml jar installjars_tc

#use this target if you do not have any java files to compile
xml: compile_xml check_permissions
	@(rm -f projects/servletconf/tomcat/lib/*.jar)
	@echo "link common jars to tomcat directory"
	@(cd projects/servletconf/tomcat/lib/; ln -s ../../../../lib/*.jar .)
	@echo Done!

compile: generate_src
	@echo
	@echo "*** Building all *.java-files..."
	@${JAVAC} $(ALLJAVA)
	@echo "*** ...Done!"

generate_src: projects/core
	@echo
	@echo "*** Generating wrapper java-files..."
	@echo "wrappers: $(ALLWRAPPERS)"
	@$(JAVA) $(TRFBUILD) -s $(SRCROOT)/ -a iwrp -x $(BUILD_LIB) $(ALLWRAPPERS)
	@echo "*** ...Done!"

compile_xml: projects/core
	cd projects && make -f Makefile  all

projects/core: $(PFIXCORE_DATA)
	@echo "*** Erasing the whole core subdirectory!"
	@rm -rf projects/core
	@mkdir projects/core
	@echo "*** Extracting core data from $(PFIXCORE_DATA)"
	(cd projects; tar xvzf ../$(PFIXCORE_DATA))

clean: 
	@echo "*** Removing all under '$(BUILDDIR)'..."
	@rm -rf $(BUILDDIR)/*
	@echo "*** Removing generated files..."
	@rm -f $(ALLWRAPPERS:.iwrp=.java)
	@echo "*** Removing all files matching: *~ ..."
	@find $(SUBDIRS) -type f -name "*~" | xargs rm -f
	@cd projects && make -f Makefile clean
	@rm -f projects/servletconf/tomcat/lib/*.jar
#	@rm -rf projects/core

clean-rebuild:
	make clean
	make all

jar:	compile
	@echo "Building $(PROJECT).jar in directory dist."
	@(rm -f dist/*.jar)
	@(if ! test -d dist; then mkdir dist; fi)
	@(if test -f dist/$(PROJECT).jar; then rm dist/$(PROJECT).jar; fi)
	@(cd $(BUILDDIR); ${JAR} cf ../dist/$(PROJECT).jar *)	
	-@(cd res; ${JAR} uf ../dist/$(PROJECT).jar `find . -type f | grep -v "CVS/"`)
	@echo Done!

installjars_tc: jar
	@(rm -f projects/servletconf/tomcat/lib/*.jar)
	@echo "copy jar to tomcat directory"
	@(cp dist/*.jar projects/servletconf/tomcat/lib/)
	@echo "link common jars to tomcat directory"
	@(cd projects/servletconf/tomcat/lib/; ln -s ../../../../lib/*.jar .)
	@echo Done!

check_permissions:
	@echo Checking permissions for files 
	@chmod -c 755 bin/setvars.sh 
	@chmod -c 755 projects/bin/setvars.sh 
	@chmod -c 755 bin/setClassPath.sh
	@chmod -c 755 startJServ.sh
	@chmod -c 755 startTomcat.sh
	@chmod -c 755 projects/servletconf/tomcat/bin/catalina.sh


