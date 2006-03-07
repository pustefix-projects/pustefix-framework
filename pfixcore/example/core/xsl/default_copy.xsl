<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<!--  this stylesheet defines the default action: copy through unchanged. -->
<!--  The funny variable defined here (__env) is a very dirty hack to -->
<!--  introduce a dynamically scoped variable into XSLT. Of course this -->
<!--  works only when the default rule actually is called... -->

  <xsl:template match="*|/">
    <xsl:param name="__env"/>
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates>
        <xsl:with-param name="__env" select="$__env"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
