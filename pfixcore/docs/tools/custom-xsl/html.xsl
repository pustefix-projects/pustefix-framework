<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
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
</xsl:stylesheet>