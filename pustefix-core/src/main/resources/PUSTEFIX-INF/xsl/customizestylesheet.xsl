<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                exclude-result-prefixes="xsl cus" 
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:param name="lang"/>
  <xsl:param name="__target_gen"/>
  
  <!-- No customizeable parts below -->
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="cus:navigation">
    <xsl:value-of select="$__target_gen"/>
  </xsl:template>

  <xsl:template match="cus:lang">
    <xsl:value-of select="$lang"/>
  </xsl:template>

</xsl:stylesheet>
