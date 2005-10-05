<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="ISO-8859-1" 
    doctype-public="-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
  	doctype-system="http://java.sun.com/dtd/web-app_2_3.dtd"/>

  <xsl:template match="/">
  	<xsl:apply-templates/>
  </xsl:template>
  
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="init-param[param-name='listings']">
    <init-param>
		  <param-name>listings</param-name>
		  <param-value>false</param-value>
    </init-param>		
	</xsl:template>

</xsl:stylesheet>