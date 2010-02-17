<?xml version="1.0" encoding="utf-8"?>
<!-- 
         This is the XSL HTML configuration file for the Spring
             Reference Documentation.
             -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                exclude-result-prefixes="d"      
                version="1.0">

    <xsl:import href="urn:docbkx:stylesheet"/>

  <xsl:template name="user.head.content">
    <xsl:param name="node" select="."/>
    <meta name="description">
      <xsl:attribute name="content"><xsl:value-of select="$node/d:info/d:title"/> - <xsl:value-of select="$node/d:info/d:subtitle"/></xsl:attribute>
    </meta>
  </xsl:template>


</xsl:stylesheet>
