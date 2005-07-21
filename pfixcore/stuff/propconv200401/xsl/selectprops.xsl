<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
  xmlns:cus="http://www.schlund.de/pustefix/customize"
  >

  <!--<xsl:template match="cus:docroot"></xsl:template>-->
  <xsl:output me met="text"/>
  
  <!-- de.schlund.pfixxml.DirectOutputServlet -->
  <!-- de.schlund.pfixxml.ContextXMLServer -->
  <xsl:param name="class" select="'de.schlund.pfixxml.ContextXMLServer'"/>
  
  <xsl:template match="text()"></xsl:template> 
  <xsl:template match="servlet[class = $class]">
    <xsl:value-of select="propfile"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  
</xsl:stylesheet>
