INPUT = pages.xml
XSLT  = templates.xsl

SAXON = java com.icl.saxon.StyleSheet
SF    = pustefix.sourceforge.net
SFWEB = /home/groups/p/pu/pustefix/htdocs

USER  = $(shell cat CVS/Root  | cut -d ":" -f 3 | cut -d "@" -f 1)


export CLASSPATH  = $(shell ../../bin/setClassPath.sh `pwd`/../..)


all: transform install

clean:
	@rm -rf gen

transform:
	$(SAXON) $(INPUT) $(XSLT)

install: transform
	@echo "*** Installing pages on Sourceforge site ***"
	$(shell scp gen/*.html $(USER)@$(SF):$(SFWEB)/)
	@echo "*** Fixing permissions ***"
	$(shell ssh $(USER)@$(SF) chmod -R g+rw $(SFWEB)/*)
	@echo "*** Done. ***"
