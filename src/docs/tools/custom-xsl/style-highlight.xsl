<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xslthl="http://xslthl.sf.net"
  exclude-result-prefixes="xslthl"
  version="1.0">
  
  <xsl:template match='xslthl:keyword'>
    <span class="hl-keyword"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:string'>
    <span class="hl-string"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:comment'>
    <span class="hl-comment"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:tag'>
    <span class="hl-tag"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:attribute'>
    <span class="hl-attribute"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:value'>
    <span class="hl-value"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:html'>
    <span class="hl-html"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:xslt'>
    <span class="hl-xslt"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:section'>
    <span class="hl-section"><xsl:apply-templates/></span>
  </xsl:template>
  
</xsl:stylesheet>

