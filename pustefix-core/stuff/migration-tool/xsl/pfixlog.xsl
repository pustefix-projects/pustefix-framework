<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
>
  
  <xsl:import href="defaultcopy.xsl"/>
  <xsl:import href="customization.xsl"/>
  
  <xsl:param name="targetNamespace">http://www.schlund.de/pustefix/customize</xsl:param>
  <xsl:param name="targetPrefix">cus</xsl:param>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="cus:choose">
    <cus:choose>
      <xsl:call-template name="choose"/>
    </cus:choose>
  </xsl:template>
  
</xsl:stylesheet>