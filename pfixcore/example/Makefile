#
# All changes should be made to the PRJFILE
#

LANG=C

PRJFILE = servletconf/projects.xml

ifndef MAKE_MODE
#MAKE_MODE = prod
MAKE_MODE = test
endif

# No user serviceable parts below. Do not open. Warranty is void if seal damaged
# <seal>

export CLASSPATH = $(shell ./bin/setvars.sh)
ifndef CONFIGS
CONFIGS   = $(shell find . -type f | grep 'depend.xml.in$$')
endif
ifndef PROPFILES
PROPFILES = $(shell find . -type f | grep '\.prop.in$$')
endif
DOCROOT   = $(shell pwd)
MACHINE   = $(shell hostname -s)
FQDN      = $(MACHINE).$(shell dnsdomainname)
UID       = $(USER)

#CORE_XSL = $(shell find . -type f | grep 'core/xsl/.*\.xsl.in$$')
CORE_XSL  = $(shell find . -type f | grep '.*\.xsl.in$$')

ifndef PROHIBIT_EDIT
PROHIBIT_EDIT = no
endif

# Override everything for production mode.
ifeq ($(MAKE_MODE), prod)
PROHIBIT_EDIT = yes
endif

# Debugging of TargetGenerator, Level 0: quiet
GENERATOR  = java -Dlog4jconfig=core/conf/generator_quiet.xml -mx100M de.schlund.pfixxml.targets.TargetGenerator
# Debugging of TargetGenerator, Level 1: just the tree
#GENERATOR = java -Dlog4jconfig=core/conf/generator_tree.xml  -mx100M de.schlund.pfixxml.targets.TargetGenerator
# Debugging of TargetGenerator, Level 2: full debugging
#GENERATOR = java -Dlog4jconfig=core/conf/generator_full.xml  -mx100M de.schlund.pfixxml.targets.TargetGenerator

SAXON      = java -mx120M -Djavax.xml.transform.TransformerFactory=com.icl.saxon.TransformerFactoryImpl \
              -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl com.icl.saxon.StyleSheet

TRFBUILD   = de.schlund.pfixcore.util.MultiTransform

all: bin/setvars.sh depend propfiles targetdefs  zoneprops stylesheets \
     common/conf/pfixlog.xml common/conf/products.prj \
     servletconf/tomcat/apache.conf servletconf/tomcat/jk.conf servletconf/tomcat/workers.prop servletconf/tomcat/conf/server.xml \
     common/dyntxt/statusmessages.xml

generate: all
	@echo "*** Generate Sequence running ...."
	$(GENERATOR) $(patsubst %.xml.in,%.xml,$(filter %.xml.in, $(CONFIGS)))

_generate:
	@echo "*** Generate Sequence running ...."
	$(GENERATOR) $(patsubst %.xml.in,%.xml,$(filter %.xml.in, $(CONFIGS)))


clean:
	@echo "*** Removing *~..."
	@find . -type f -name "*~" | xargs rm -f
	@echo "*** Removing generated stylesheets..."
	@rm -f $(CORE_XSL:.xsl.in=.xsl)
	@echo "*** Removing generated configs..."
	@rm -f $(CONFIGS:.xml.in=.xml)
	@echo "*** Removing generated .prop files..."
	@rm -f $(PROPFILES:.prop.in=.prop)
	@rm -f common/conf/products.prj common/conf/pfixlog.xml 
	@echo "*** Removing generated tomcat stuff..."
	@rm -f servletconf/tomcat/apache.conf servletconf/tomcat/jk.conf servletconf/tomcat/workers.prop servletconf/tomcat/conf/server.xml \
        @rm -rf servletconf/tomcat/webapps

bin/setvars.sh: core/src/setvars.sh.template
	$(error '$<' seems to be newer than '$@'. Copy '$<' to ./'$@' and edit it to match your site!)


depend:  targetdefs $(patsubst %.prj.in,%.prj,$(filter %.prj.in, $(CONFIGS)))


propfiles: 
	java $(TRFBUILD) -x core/build/create_propfiles.xsl -a std  \
			 -p docroot=$(DOCROOT) -p fqdn=$(FQDN) -p uid=$(UID) -p machine=$(MACHINE) -p mode=$(MAKE_MODE) \
			 $(PROPFILES)

targetdefs:
	java $(TRFBUILD) -x core/build/create_depend.xsl -a std \
			 -p prohibitEdit=$(PROHIBIT_EDIT) -p docroot=$(DOCROOT) \
			 $(filter %.xml.in, $(CONFIGS))

zoneprops:
	java $(TRFBUILD) -x dummy -a zoneprops \
			 -p docroot=$(DOCROOT) -p fqdn=$(FQDN) -p uid=$(UID) -p machine=$(MACHINE) -p mode=$(MAKE_MODE) \
			 -p outdir-tomcat=$(DOCROOT)/servletconf/tomcat \
			 -p xslfile-tomcat=core/build/create_tomcatweb.xsl \
			 $(PRJFILE)

stylesheets:
	java $(TRFBUILD) -x core/build/create_stylesheet.xsl -a std  \
			 -p docroot=$(DOCROOT) -p fqdn=$(FQDN) -p uid=$(UID) -p machine=$(MACHINE) -p mode=$(MAKE_MODE) \
			 $(CORE_XSL)

.PHONY: common/dyntxt/statusmessages.xml

common/dyntxt/statusmessages.xml:
	java -Dpropertyfile=common/conf/factory.prop de.schlund.pfixcore.util.MakeStatusMessageXML $@

common/conf/pfixlog.xml: common/conf/pfixlog.xml.in
	$(SAXON)  -o $@ $< core/build/create_log4j_config.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)" 

common/conf/products.prj: $(PRJFILE) core/build/create_products.xsl
	$(SAXON) -o $@  $< core/build/create_products.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)"

servletconf/tomcat/apache.conf: $(PRJFILE) core/build/create_apacheconf.xsl
	$(SAXON) -o $@ $(PRJFILE) core/build/create_apacheconf.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)" \
		container="tomcat" 

servletconf/tomcat/jk.conf: $(PRJFILE) core/build/create_jkconf.xsl
	$(SAXON) -o $@ $(PRJFILE)  core/build/create_jkconf.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)" \
		container="tomcat"

servletconf/tomcat/workers.prop: $(PRJFILE) core/build/create_workersprop.xsl
	$(SAXON) -o $@ $(PRJFILE) core/build/create_workersprop.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)" \
		java_home="$(JDK_HOME)" \
		container="tomcat" 

servletconf/tomcat/conf/server.xml: $(PRJFILE) core/build/create_serverxml.xsl
	$(SAXON) -o $@ $(PRJFILE) core/build/create_serverxml.xsl \
		docroot="$(DOCROOT)"  \
		uid="$(UID)" \
		machine="$(MACHINE)" \
		fqdn="$(FQDN)" \
		mode="$(MAKE_MODE)" \
		container="tomcat" 

# </seal>
