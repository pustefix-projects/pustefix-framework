<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" 
                xmlns:shop="http://shop" version="1.0">

    <xsl:param name="page"/>

    <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:template match="shop:link">
      <link to="{@page}"><ixsl:attribute name="class">myclass</ixsl:attribute><xsl:apply-templates/></link>
    </xsl:template>
    
   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
    

</xsl:stylesheet>