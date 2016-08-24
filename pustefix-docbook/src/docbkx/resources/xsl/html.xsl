<?xml version="1.0" encoding="utf-8"?>
<!-- 
         This is the XSL HTML configuration file for the Spring
             Reference Documentation.
             -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xslthl="http://xslthl.sf.net"    
                exclude-result-prefixes="d xslthl"      
                version="1.0">

    <xsl:import href="urn:docbkx:stylesheet"/>
    <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>
<!--
  <xsl:template name="user.head.content">
    <xsl:param name="node" select="."/>
    <meta name="description">
      <xsl:attribute name="content"><xsl:value-of select="$node/d:info/d:title"/> - <xsl:value-of select="$node/d:info/d:subtitle"/></xsl:attribute>
    </meta>
  </xsl:template>

  <xsl:template name="user.header.content">
    <xsl:param name="node" select="."/>
    <div id="header">
      <h1><a href="http://pustefix-framework.org/">Pustefix-Framework</a></h1>
    </div>
    <div id="navi">
      <li><a href="faq.html"><xsl:if test="$node/@id='faq'"><xsl:attribute name="class">active</xsl:attribute></xsl:if>FAQ</a></li>
      <li><a href="reference.html"><xsl:if test="$node/@id='reference'"><xsl:attribute name="class">active</xsl:attribute></xsl:if>Reference</a></li>
      <li><a href="tutorial.html"><xsl:if test="$node/@id='tutorial'"><xsl:attribute name="class">active</xsl:attribute></xsl:if>Tutorials</a></li>
      <li><a href="http://pustefix-framework.org/docs.html">Other versions</a></li>
    </div>
  </xsl:template>
-->

  <xsl:template name="credits.div"/>

  <xsl:template match="xslthl:keyword" mode="xslthl">
    <span class="hl-keyword">
      <xsl:apply-templates mode="xslthl"/>
    </span>
  </xsl:template>

  <xsl:template match="xslthl:string" mode="xslthl">
    <span class="hl-string">
      <xsl:apply-templates mode="xslthl"/>
    </span>
  </xsl:template>

  <xsl:template match="xslthl:tag" mode="xslthl">
    <span class="hl-tag">
      <xsl:apply-templates mode="xslthl"/>
    </span>
  </xsl:template>

  <xsl:template match="xslthl:attribute" mode="xslthl">
    <span class="hl-attribute">
      <xsl:apply-templates mode="xslthl"/>
    </span>
  </xsl:template>

  <xsl:template match="xslthl:value" mode="xslthl">
    <span class="hl-value">
      <xsl:apply-templates mode="xslthl"/>
    </span>
  </xsl:template>

</xsl:stylesheet>
