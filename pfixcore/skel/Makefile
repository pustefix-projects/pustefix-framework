#################################################################
# Rules...
#################################################################
LANG=C
PROJECT=pfixcore_skel
BUILDDIR=build
JAR=jar
PFIXCORE_DATA=$(shell ls -1 lib/pfixcore-data-*.tar.gz)

all:  projects/core check_permissions compile installjars_tc compile_xml 

projects/core: $(PFIXCORE_DATA)
	@echo "*** Erasing the whole core subdirectory!"
	@rm -rf projects/core
	@mkdir projects/core
	@echo "*** Extracting core data from $(PFIXCORE_DATA)"
	(cd projects; tar xvzf ../$(PFIXCORE_DATA))


check_permissions:
	@echo Checking permissions for files 
	@chmod -c 755 bin/setvars.sh 
	@chmod -c 755 projects/bin/setvars.sh 
	@chmod -c 755 bin/setClassPath.sh
	@chmod -c 755 startTomcat.sh
	@chmod -c 755 apploader.sh
	@chmod -c 755 projects/servletconf/tomcat/bin/catalina.sh

inc: generate_src
	@echo compiling classes incrementally...
	make -f Makefile.classes inc

compile: generate_src
	@echo 
	@echo compiling classes...
	make -f Makefile.classes compile

compile_xml:
	cd projects && make -f Makefile all 

generate_src:
	@echo 
	@echo ROOT: generating src. 
	make -f Makefile.iwrp generate_src

generate: all
	@echo Making Targets:
	cd projects && make -f Makefile generate

clean:
	@echo 
	@echo cleaning up!
	make -f Makefile.iwrp clean 
	make -f Makefile.generated clean
	make -f Makefile.classes clean
	cd projects && make -f Makefile clean 
	@(rm -rf dist)

installjars_tc: compile
	@echo "Setting link to $(BUILDDIR)/* in projects/servletconf/tomcat/classes."
	@(cd projects/servletconf/tomcat/; ln -s ../../../$(BUILDDIR) classes)
	@(rm -f projects/servletconf/tomcat/lib/*.jar)
	@echo "create resource jar in tomcat directory"
	@(cd res; ${JAR} cf ../projects/servletconf/tomcat/lib/$(PROJECT)-res.jar `find . -type f | grep -v "CVS/"`)
	@echo "link common jars to tomcat directory"
	@(cd projects/servletconf/tomcat/lib/; ln -s ../../../../lib/*.jar .)
	@echo Done!


doc:	all
	make -f Makefile.javadoc doc

#################################################################

echo-classpath-tomcat:
	@make -f Makefile.classes echo-classpath-tomcat
#################################################################
