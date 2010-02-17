<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://docbook.org/ns/docbook" version="1.0" exclude-result-prefixes="d">
  <xsl:import href="../docbook-xsl-ns/html/docbook.xsl"/>
  <xsl:import href="style-highlight.xsl"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.autolabel.max.depth" select="2"/>
  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="html.cleanup" select="1"/>
  <xsl:param name="use.extensions" select="1"/>
  <xsl:param name="textinsert.extension" select="1"/>
  <xsl:param name="linenumbering.extension" select="1"/>
  <xsl:param name="graphicsize.extension" select="0"/>
  <xsl:param name="highlight.source" select="1"/>
  <xsl:param name="callout.graphics.path" select="'images/docbook/callouts/'"/>
  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.path" select="'images/docbook/'"/>
  <xsl:param name="html.stylesheet" select="'css/style.css'"/>
  <xsl:param name="toc.section.depth" select="2"/>

  <xsl:template name="user.head.content">
    <xsl:param name="node" select="."/>
    <meta name="description">
      <xsl:attribute name="content"><xsl:value-of select="$node/d:info/d:title"/> - <xsl:value-of select="$node/d:info/d:subtitle"/></xsl:attribute>
    </meta>
  </xsl:template>

</xsl:stylesheet>
