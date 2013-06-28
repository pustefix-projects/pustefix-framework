<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:func="http://exslt.org/functions"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback"
                exclude-result-prefixes="pfx func callback">
  
  <!-- XPath functions available on all transformation levels -->
  
  <!-- String functions -->

  <func:function name="pfx:ends-with">
    <xsl:param name="str"/>
    <xsl:param name="end"/>
    <func:result select="callback:endsWith($str, $end)"/>
  </func:function>
  
  <func:function name="pfx:lower-case">
    <xsl:param name="str"/>
    <func:result select="callback:lowerCase($str)"/>
  </func:function>
  
  <func:function name="pfx:upper-case">
    <xsl:param name="str"/>
    <func:result select="callback:upperCase($str)"/>
  </func:function>
  
  <!-- Development functions -->
  
  <func:function name="pfx:sleep">
    <xsl:param name="delay"/>
    <func:result select="callback:sleep($delay)"/>
  </func:function>  
  
</xsl:stylesheet>
