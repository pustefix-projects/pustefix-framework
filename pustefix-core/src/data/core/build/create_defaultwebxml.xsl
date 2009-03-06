<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:j2ee="http://java.sun.com/xml/ns/j2ee" xmlns="http://java.sun.com/xml/ns/j2ee">

  <xsl:output method="xml" encoding="UTF-8" />

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <!-- web-app_2_3.dtd version -->
  <xsl:template match="init-param[param-name='listings']">
    <xsl:element name="init-param" namespace="">
      <xsl:element name="param-name" namespace="">listings</xsl:element>
      <xsl:element name="param-value" namespace="">false</xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- web-app_2_4.xsd version -->	
  <xsl:template match="j2ee:init-param[j2ee:param-name='listings']">
    <xsl:element name="init-param">
      <xsl:element name="param-name">listings</xsl:element>
      <xsl:element name="param-value">false</xsl:element>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
