<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:conf="http://pustefix.sourceforge.net/wsconfig200401"
                xmlns="http://pustefix.sourceforge.net/wsconfig200401"
>
  
  <xsl:import href="defaultcopy.xsl"/>
  <xsl:import href="customization.xsl"/>
  
  <xsl:param name="targetNamespace">http://pustefix.sourceforge.net/wsconfig200401</xsl:param>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="conf:choose">
    <choose>
      <xsl:call-template name="choose"/>
    </choose>
  </xsl:template>

</xsl:stylesheet>