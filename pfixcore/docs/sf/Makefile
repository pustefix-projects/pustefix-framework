INPUT = pages.xml
XSLT  = templates.xsl

SAXON = java com.icl.saxon.StyleSheet
SF    = pustefix.sourceforge.net
SFWEB = /home/groups/p/pu/pustefix/htdocs

USER  = $(shell cat CVS/Root  | cut -d ":" -f 3 | cut -d "@" -f 1)


export CLASSPATH  = $(shell ../../bin/setClassPath.sh `pwd`/../..)


all: transform 

transform:
	$(SAXON) $(INPUT) $(XSLT)

install: transform
	@echo "*** Installing pages on Sourceforge site ***"
	-$(shell ssh $(USER)@$(SF) mkdir $(SFWEB)/img/)
	$(shell scp gen/*.html $(USER)@$(SF):$(SFWEB)/)
	$(shell scp gen/img/*.gif $(USER)@$(SF):$(SFWEB)/img/)
	$(shell scp gen/img/*.jpg $(USER)@$(SF):$(SFWEB)/img/)
	@echo "*** Fixing permissions ***"
	-$(shell ssh $(USER)@$(SF) chmod -R g+rw $(SFWEB)/*)
	-$(shell ssh $(USER)@$(SF) chmod -R g+rw $(SFWEB)/img/*)
	@echo "*** Done. ***"
