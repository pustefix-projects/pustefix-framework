<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon" version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="page">
    <xsl:message>*** Generating <xsl:value-of select="@name"/></xsl:message>
    <saxon:output href="gen/{@name}.html">
      <html>
        <head>
          <title>Pustefix: <xsl:value-of select="@title"/></title>
          <link rel="stylesheet" href="styles.css"/>
        </head>
        <body>
          <div style="position: absolute; bottom: 0%; left: 0px; z-index: 2;">
            <a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=72089&amp;type=4" width="125" height="37" border="0" alt="SourceForge.net Logo" /></a>
          </div>
          <div class="topframe">
            The Pustefix Framework
          </div>
          <div class="navibody">
            <xsl:call-template name="gen_navi">
              <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param>
              <xsl:with-param name="parent" select="/pagedef"/>
            </xsl:call-template>
          </div>
          <div class="mainbody">
            <xsl:apply-templates select="document(concat(@name, '_main.xml'))">
              <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param></xsl:apply-templates>
          </div>
        </body>
      </html>
    </saxon:output>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="stdimg">
    <div class="stdimg">
      <div class="stdimg-img"><img src="{@src}"/></div>
      <div class="stdimg-txt"><xsl:apply-templates/></div>
    </div>
  </xsl:template>
  
  <xsl:template match="pustefix">
    <i><b>Pustefix</b></i>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xmlcodeOFF" mode="static_disp">
    <br/><br/>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="*" mode="static_disp">
    <xsl:param name="ind">&#160;&#160;</xsl:param>
    <xsl:param name="break">true</xsl:param>
    <xsl:param name="col">
      <xsl:choose>
        <xsl:when test="starts-with(name(),'xsl:')">tagxsl</xsl:when>
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
  

  <xsl:template match="xmlcode">
    <div class="xmlcode">
      <xsl:apply-templates mode="static_disp" select="node()"/>
    </div>
  </xsl:template>
  
  
  <xsl:template name="gen_navi">
    <xsl:param name="parent"/>
    <xsl:param name="thepage"/>
    <div id="navigation">
      <xsl:for-each select="$parent/page">
        <xsl:variable name="depth" select="count(ancestor::page)"/>
        <xsl:variable name="class"><xsl:choose>
            <xsl:when test="$depth = 0">menuentry</xsl:when>
            <xsl:otherwise>submenuentry</xsl:otherwise></xsl:choose></xsl:variable>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="@name"/>.html</xsl:attribute>
          <div class="menuentry">
            <xsl:attribute name="class">
              <xsl:value-of select="$class"/>
              <xsl:if test="@name = $thepage"> selected</xsl:if>
            </xsl:attribute>
            <xsl:value-of select="@title"/>
          </div>
        </a>
        <xsl:if test="@name = $thepage or .//page[@name = $thepage]"><xsl:call-template name="gen_navi">
            <xsl:with-param name="thepage"><xsl:value-of select="$thepage"/></xsl:with-param>
            <xsl:with-param name="parent" select="."/>
          </xsl:call-template></xsl:if>
      </xsl:for-each>
    </div>
  </xsl:template>
</xsl:stylesheet>
