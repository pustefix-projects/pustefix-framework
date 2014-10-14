<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:func="http://exslt.org/functions"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback"
                xmlns:xslt2="java:de.schlund.pfixxml.util.xsltimpl.Xslt2BackPortFunctions"
                exclude-result-prefixes="pfx func callback xslt2">
  
  <!-- XPath functions available on all transformation levels -->
  
  <!-- String functions -->

  <func:function name="pfx:ends-with">
    <xsl:param name="str"/>
    <xsl:param name="end"/>
    <func:result select="xslt2:endsWith($str, $end)"/>
  </func:function>
  
  <func:function name="pfx:lower-case">
    <xsl:param name="str"/>
    <func:result select="xslt2:lowerCase($str)"/>
  </func:function>
  
  <func:function name="pfx:upper-case">
    <xsl:param name="str"/>
    <func:result select="xslt2:upperCase($str)"/>
  </func:function>
  
  <func:function name="pfx:tokenize">
    <xsl:param name="str"/>
    <xsl:param name="pattern"/>
    <func:result select="xslt2:tokenize($str, $pattern)"/>
  </func:function>
  
  <func:function name="pfx:format-date">
    <xsl:param name="dateTime"/>
    <xsl:param name="datePattern"/>
    <func:result select="xslt2:formatDate($str, $pattern)"/>
  </func:function>
  
  
  <!-- Development functions -->
  
  <func:function name="pfx:sleep">
    <xsl:param name="delay"/>
    <func:result select="callback:sleep($delay)"/>
  </func:function>  
  
</xsl:stylesheet>
