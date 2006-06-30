<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes" doctype-system="http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd"/>
  <xsl:include href="create_lib.xsl"/>

  <xsl:template match="param">
    <param>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(@value)">
        <xsl:attribute name="value"><xsl:apply-templates select="./node()"/></xsl:attribute>
      </xsl:if>
    </param>
  </xsl:template>
  
</xsl:stylesheet>
