<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:prop="http://pustefix.sourceforge.net/properties200401" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
  
  <xsl:output method="xml" encoding="UTF-8"/>
  
  <!-- 
  <xsl:template match="prop:properties/prop:prop[@name = 'context.class']">
    <xxx><xsl:value-of select="."/></xxx>
  </xsl:template>
   -->
  
  <!-- 
  <xsl:template match="prop:properties/text()[self::text() != '']"><xsl:value-of select="translate(translate(., ' ', 'S'), '&#10;', 'N')"/></xsl:template>
   -->
  
  <xsl:template match="prop:properties/text()[self::text() != '']"><xsl:if test="normalize-space(.) != ''"><xsl:value-of select="name(following-sibling::prop:prop)"/><xsl:value-of select="following-sibling::prop:prop/@name"/></xsl:if> <xsl:value-of select="normalize-space(.)"/></xsl:template>
  
  <xsl:template match="text()"/>
  
</xsl:stylesheet>