<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon" version="1.0">

  <xsl:output method="xml"/>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="xmlcode">
    <div class="xmlcode">
      <xsl:apply-templates mode="static_disp" select="node()"/>
    </div>
  </xsl:template>

  <xsl:template match="xmlnote" mode="static_disp">
    <br/><br/>
    <div class="xmlnote">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="xmlcodeNS" mode="static_disp"/>
  
  <xsl:template match="*" mode="static_disp">
    <xsl:param name="ind">&#160;&#160;</xsl:param>
    <xsl:param name="break">true</xsl:param>
    <xsl:param name="col">
      <xsl:choose>
        <xsl:when test="starts-with(name(),'xsl:')">tagxsl</xsl:when>
        <xsl:when test="starts-with(name(),'cus:')">tagcus</xsl:when>
        <xsl:when test="starts-with(name(),'ixsl:')">tagixsl</xsl:when>
        <xsl:when test="starts-with(name(),'pfx:')">tagpfx</xsl:when>
        <xsl:otherwise>tagother</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:if test="$break='false'">
      <br/>
    </xsl:if>
    <xsl:if test="(name() = 'xsl:template') or (name() = 'xsl:template')">
      <br/></xsl:if>
    <xsl:value-of select="$ind"/>
    <span class="ltgt">&lt;</span>
    <span><xsl:attribute name="class"><xsl:value-of select="$col"/>
      </xsl:attribute><xsl:value-of select="name()"/></span>
    <xsl:for-each select="./xmlcodeNS">&#160;<span class="attrkey">xmlns:<xsl:value-of select="@ns"/></span>
      <xsl:text>="</xsl:text><span class="attrval"><xsl:value-of select="./text()"/></span><xsl:text>"</xsl:text></xsl:for-each>
    <xsl:for-each select="@*">&#160;<span class="attrkey">
        <xsl:value-of select="name()"/></span><xsl:text>="</xsl:text><span class="attrval">
        <xsl:value-of select="."/></span><xsl:text>"</xsl:text></xsl:for-each><span class="ltgt">
      <xsl:if test="count(./node()) = 0">/</xsl:if>&gt;</span>
    <xsl:apply-templates mode="static_disp">
      <xsl:with-param name="ind">
        <xsl:value-of select="$ind"/>&#160;&#160;&#160;&#160;</xsl:with-param>
      <xsl:with-param name="break">false</xsl:with-param>
    </xsl:apply-templates>
    <xsl:if test="not(count(./node()) = 0)">
      <xsl:if test="count(./*) > 0">
        <br/>
        <xsl:value-of select="$ind"/>
      </xsl:if>
      <span class="ltgt">&lt;/</span>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="$col"/></xsl:attribute>
        <xsl:value-of select="name()"/></span>
      <span class="ltgt">&gt;</span>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="static_disp">
    <xsl:value-of select="normalize-space(current())"/>
  </xsl:template>

  <xsl:template match="comment()" mode="static_disp">
    <br/> <font color="#999999">&lt;!--<xsl:value-of select="."/>--&gt;</font>
  </xsl:template>
  
</xsl:stylesheet>
