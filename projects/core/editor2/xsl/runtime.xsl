<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:edit="http://pustefix.sourceforge.net/pfixcore/editor"
                version="1.0">
  
  <xsl:template match="*" mode="static_disp">
    <xsl:param name="ind">&#160;&#160;</xsl:param>
    <xsl:param name="break">true</xsl:param>
    <xsl:param name="col">
      <xsl:choose>
        <xsl:when test="starts-with(name(),'xsl:')">#dd0000</xsl:when>
        <xsl:when test="starts-with(name(),'ixsl:')">#cc44aa</xsl:when>
        <xsl:when test="starts-with(name(),'pfx:')">#0000aa</xsl:when>
        <xsl:otherwise>#ffaa44</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:if test="$break='false'">
      <br/>
    </xsl:if>
    <xsl:if test="(name() = 'xsl:template') or (name() = 'ixsl:template')">
      <br/></xsl:if>
    <xsl:value-of select="$ind"/>
    <font color="#ff0000">&lt;</font>
    <font><xsl:attribute name="color"><xsl:value-of select="$col"/>
      </xsl:attribute><b><xsl:value-of select="name()"/></b></font>
    <xsl:for-each select="@*">&#160;<font color="#0000ff">
        <xsl:value-of select="name()"/></font><xsl:text>="</xsl:text><font color="#22aa00">
        <xsl:value-of select="."/></font><xsl:text>"</xsl:text></xsl:for-each><font color="#ff0000">
      <xsl:if test="count(./node()) = 0">/</xsl:if>&gt;</font>
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
      <font color="#ff0000">&lt;/</font>
      <font>
        <xsl:attribute name="color"><xsl:value-of select="$col"/></xsl:attribute>
        <b><xsl:value-of select="name()"/></b></font>
      <font color="#ff0000">&gt;</font>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="static_disp">
    <xsl:value-of select="normalize-space(current())"/>
  </xsl:template>

  <xsl:template match="xmlcode">
    <div>
      <xsl:attribute name="style">
        <xsl:if test="@width">width:<xsl:value-of select="@width"/>px; </xsl:if>
        <xsl:text>background: #ffffff; border: 1px solid #AEAEAE; padding: 4px 4px 4px 4px; overflow:auto</xsl:text>
      </xsl:attribute>
      <xsl:apply-templates mode="static_disp" select="node()"/>
    </div>
  </xsl:template>
  
  <xsl:template match="comment()" mode="static_disp">
    <br/> <span style="color:#999999">&lt;!--<xsl:value-of select="."/>--&gt;</span>
  </xsl:template>
  
  <func:function name="edit:jsEscape">
    <xsl:param name="thestring"/>
    <func:result select="edit:jsEscapeQuotes(edit:jsEscapeBackslashes($thestring))"/>
  </func:function>
  
  <func:function name="edit:jsEscapeBackslashes">
    <xsl:param name="thestring"/>
    <xsl:choose>
      <xsl:when test="contains($thestring, '\')">
        <func:result select="concat(substring-before($thestring, '\'), '\\', edit:jsEscapeBackslashes(substring-after($thestring, '\')))"/>
      </xsl:when>
      <xsl:otherwise>
        <func:result select="$thestring"/>
      </xsl:otherwise>
    </xsl:choose>
  </func:function>
  
  <func:function name="edit:jsEscapeQuotes">
    <xsl:param name="thestring"/>
    <xsl:choose>
      <xsl:when test="contains($thestring, '&quot;')">
        <func:result select="concat(substring-before($thestring, '&quot;'), '\&quot;', edit:jsEscapeQuotes(substring-after($thestring, '&quot;')))"/>
      </xsl:when>
      <xsl:otherwise>
        <func:result select="$thestring"/>
      </xsl:otherwise>
    </xsl:choose>
  </func:function>
  
</xsl:stylesheet>
