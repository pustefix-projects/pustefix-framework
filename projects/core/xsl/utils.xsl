<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:param name="maincontentpath"/>
  
  <xsl:template match="pfx:maincontent">
    <xsl:param name="noerror" select="@noerror"/>  
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path">
          <xsl:value-of select="@path"/>
        </xsl:when>
        <xsl:when test="string($maincontentpath) != ''"><xsl:value-of select="$maincontentpath"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$product"/>/txt/pages</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="prefix">
      <xsl:choose>
        <xsl:when test="@prefix">
          <xsl:value-of select="@prefix"/>
        </xsl:when>
        <xsl:otherwise>main_</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="postfix">
      <xsl:choose>
        <xsl:when test="@postfix">
          <xsl:value-of select="@postfix"/>
        </xsl:when>
        <xsl:otherwise>.xml</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="part">
      <xsl:choose>
        <xsl:when test="@part">
          <xsl:value-of select="@part"/>
        </xsl:when>
        <xsl:otherwise>content</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="pfx:include">
      <xsl:with-param name="href"><xsl:value-of select="$path"/>/<xsl:value-of select="$prefix"/><xsl:value-of select="$page"/><xsl:value-of select="$postfix"/></xsl:with-param>
      <xsl:with-param name="part" select="$part"/>
      <!-- this is tricky to understand --> 
      <xsl:with-param name="computed_inc">true</xsl:with-param>
      <xsl:with-param name="noerr" select="$noerror"/>      
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:editconsole">
    <xsl:if test="$prohibitEdit = 'no'">
      <form target="_top">
        <table cellpadding="0" cellspacing="0" border="0">
          <tr>
            <td width="1px">
              <ixsl:choose>
                <ixsl:when test="$__editmode='admin'">
                  <a target="_top">
                    <ixsl:attribute name="href">
                      <ixsl:value-of select="$__uri"/>?__editmode=none</ixsl:attribute>
                    <img border="0" title="Switch edit mode OFF" src="{{$__contextpath}}/core/img/do_noedit.gif"/>
                  </a>
                </ixsl:when>
                <ixsl:otherwise>
                  <a target="_top">
                    <ixsl:attribute name="href">
                      <ixsl:value-of select="$__uri"/>?__editmode=admin</ixsl:attribute>
                    <img border="0" title="Switch edit mode ON" src="{{$__contextpath}}/core/img/do_edit.gif"/>
                  </a>
                </ixsl:otherwise>
              </ixsl:choose>
            </td>
            <td align="left">
              <a target="__xml_source__">
                <ixsl:attribute name="href">
                  <ixsl:value-of select="$__uri"/>?__reuse=<ixsl:value-of select="$__reusestamp"/>&amp;__xmlonly=1</ixsl:attribute>
                <img border="0" title="Show last XML tree" src="{{$__contextpath}}/core/img/show_xml.gif"/></a>
            </td>
          </tr>
          <tr>
            <td nowrap="nowrap" colspan="2" style="font-family: Verdana,Sans; font-size: 10px; background-color: black; color: white; padding-left: 5px; padding-right: 2px;">
              P: <ixsl:value-of select="$page"/>
            </td>
          </tr>
          <tr>
            <td nowrap="nowrap" colspan="2" style="font-family: Verdana,Sans; font-size: 10px; background-color: black; color: white; padding-left: 5px; padding-right: 2px;">
              F: <ixsl:value-of select="$pageflow"/>
            </td>
          </tr>
        </table>
      </form>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:webserviceconsole">
    <xsl:if test="$prohibitEdit = 'no'">
      <span>
        Web service tools:
        <a target="__WEB_SERVICE_MONITOR__">
          <ixsl:attribute name="href">
            <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?monitor')"/>
          </ixsl:attribute>
          Monitor
        </a>
        <a target="__WEB_SERVICE_ADMIN__">
          <ixsl:attribute name="href">
            <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?admin')"/>
          </ixsl:attribute>
          Admin
        </a>
      </span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:blank">
    <img src="/core/img/blank.gif" width="1" height="1" border="0" alt="">
      <xsl:copy-of select="@*"/>
    </img>
  </xsl:template>
    
</xsl:stylesheet>
