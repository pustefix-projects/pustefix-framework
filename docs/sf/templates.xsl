<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon" version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="page">
    <saxon:output href="gen/{@name}.html">
      <html>
        <head>
          <title>Pustefix: <xsl:value-of select="@title"/></title>
          <style type="text/css">
            body        { font-family: verdana, arial, helvetica; font-size: 10px; background-color: #ffeedd; }
            td          { font-family: verdana, arial, helvetica; font-size: 10px; }
            
            #navigation   { padding: 0px; background-color: #aaccff; }
            #navigation a { text-decoration: none; color: #aa0000; }
            .menuentry        { padding: 1px 15px 1px 5px; border-left: solid 1px black; border-right: solid 1px black; }
            .menuentry:hover  { background-color: #88aa88; color: #ffffff; }
            .submenuentry     {  background-color: #99bbee; padding: 1px 2px 1px 20px; text-align: right;
                                 border-left: solid 1px black; border-right: solid 1px black;}
            .submenuentry:hover  { background-color: #88aa88; color: #ffffff; }

            .selected         { background-color: #88aa88; color: #ffffff; border: solid 1px black; }
            
            #top        { text-align: right; padding-top: 0px;  border-bottom: 1px solid black;}
          </style>
        </head>
        
        <body>
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
              <td colspan="3">
                <div id="top">
                  <h1>The Pustefix Framework</h1>
                </div>
              </td>
            </tr>
            <tr valign="top">
              <td width="120">
                <xsl:call-template name="gen_navi">
                  <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param>
                  <xsl:with-param name="parent" select="/pagedef"/>
                </xsl:call-template>
              </td>
              <td width="1%">&#160;</td>
              <td>
                <div id="body">
                  <xsl:apply-templates select="document(concat(@name, '_main.xml'))">
                    <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param></xsl:apply-templates>
                </div>
              </td>
            </tr>
            <tr>
              <td colspan="3" align="right">
                <br/>
                <hr style="border: 0px; background-color:black; height: 0px;"/>
                <br/>
                <a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=72089&amp;type=5"
                    width="210" height="62" border="0" alt="SourceForge.net Logo"/></a>
              </td>
            </tr>
          </table>
        </body>
      </html>
    </saxon:output>
    <xsl:apply-templates/>
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
            <xsl:if test="not(preceding-sibling::page) and $depth = 0">
              <xsl:attribute name="style">border-top: 1px black solid;</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(following-sibling::page) and $depth = 0">
              <xsl:attribute name="style">border-bottom: 1px black solid;</xsl:attribute>
            </xsl:if>
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
