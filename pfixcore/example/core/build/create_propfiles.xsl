<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<!--  <xsl:param name="docroot"/>
  <xsl:param name="uid"/>
  <xsl:param name="machine"/>
  <xsl:param name="fqdn"/>
  <xsl:param name="mode"/>-->

  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  <xsl:include href="create_lib.xsl"/>
    
  <!--- match the root node -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- the property template -->  
  <xsl:template match="prop">
    <xsl:param name="key"><xsl:value-of select="./@name"/></xsl:param>
    <xsl:value-of select="$key"/><xsl:text>=</xsl:text>
    <xsl:apply-templates  select="./* | ./text()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <!-- match text nodes but just if it contains a property value -->
  <xsl:template match="text()">
    <xsl:param name="doit"/>
    <xsl:if test="$doit"> 
      <xsl:value-of select="translate(normalize-space(.), '&#xa;&#xd;', '  ')"/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
