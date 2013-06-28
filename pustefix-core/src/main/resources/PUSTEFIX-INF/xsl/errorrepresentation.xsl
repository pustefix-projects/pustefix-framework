<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <html>
      <head>
        <title>Error</title>
      </head>
      <body style="font-size:xx-small">
        <br/><br/>
        <div align="center">
          <table cellpadding="3" cellspacing="0" style="background-color: #aaaacc; border: 1px solid black">
            <tr>
              <td align="center" bgcolor="#cc0000" colspan="2">
                <span style="color:#ffffff; font-weight: bold">
                  <xsl:choose>
         	        <xsl:when test="/error[@type ='xslt']">
                      XML/XSLT Error!
                    </xsl:when>
                    <xsl:when test="/error[@type = 'xslt_ext']">
                      XSLT Extension Function Error!
                    </xsl:when>
                    <xsl:otherwise>
                      Java Error!
                    </xsl:otherwise>
                  </xsl:choose> 
                </span>
              </td>
            </tr>
            <xsl:apply-templates/>
          </table>
        </div>
      </body>
    </html>
  </xsl:template>



  <xsl:template match="/error">
    
    <xsl:if test="@type='xslt'">
      <tr><td colspan="2">
      <xsl:variable name="rootcause" select=".//exception[not(exception)]"/>
      XML/XSLT error at <i><xsl:value-of select="$rootcause/xsltinfo/@systemId"/></i>
      (line <i><xsl:value-of select="$rootcause/xsltinfo/@line"/>, column <xsl:value-of select="$rootcause/xsltinfo/@column"/></i>):<br/>
      <b><xsl:value-of select="$rootcause/@type"/>: <xsl:value-of select="$rootcause/@msg"/></b>
      <xsl:if test="$rootcause/xsltinfo/@context">
        <pre style="font-size: 80%">
          <xsl:value-of select="$rootcause/xsltinfo/@context"/>
        </pre>
      </xsl:if>
      </td></tr>
    </xsl:if>
    <xsl:if test="@type='xslt_ext'">
      <tr><td colspan="2">
      <xsl:variable name="rootcause" select="..//exception[@type='de.schlund.pfixxml.util.XsltExtensionFunctionException']"/>
      XSLT extension function error at <i><xsl:value-of select="$rootcause/xsltinfo/@systemId"/></i>
      (line <i><xsl:value-of select="$rootcause/xsltinfo/@line"/>, column <xsl:value-of select="$rootcause/xsltinfo/@column"/></i>):<br/>
      <b><xsl:value-of select="$rootcause/@type"/>: <xsl:value-of select="$rootcause/@msg"/></b>
      <xsl:if test="$rootcause/xsltinfo/@context">
        <pre style="font-size: 80%">
          <xsl:value-of select="$rootcause/xsltinfo/@context"/>
        </pre>
      </xsl:if>
      </td></tr>
    </xsl:if>
  
    <xsl:apply-templates select="exception"/>
    <xsl:call-template name="sessioninfo"/>
    <xsl:call-template name="requestparams"/>
    <xsl:call-template name="laststeps"/>
    <xsl:call-template name="session_dump"/>
  </xsl:template>
  
  <xsl:template name="sessioninfo">
    <tr>
      <td><b>Session-ID:</b></td>
      <td>
        <xsl:value-of select="sessioninfo/text()"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="requestparams">
    <tr>
      <td colspan="2" bgcolor="#dd9999"><b>Request Parameter:</b></td>
    </tr>
    <xsl:for-each select="requestparams/param">
      <tr>
        <xsl:attribute name="bgcolor">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">#aaaacc</xsl:when>
            <xsl:otherwise>#ccccee</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <td><xsl:value-of select="@key"/></td>
        <td><xsl:value-of select="text()"/></td>
      </tr>
    </xsl:for-each>
  
  </xsl:template>

  <xsl:template name="laststeps">
    <tr>
      <td colspan="2" bgcolor="#dd9999"><b>Last steps:</b></td>
    </tr>
    <xsl:choose>
      <xsl:when test="count(laststeps/step) &lt; 1">
        <tr><td/><td>Not available</td></tr>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="laststeps/step">
          <tr>
            <xsl:attribute name="bgcolor">
              <xsl:choose>
                <xsl:when test="position() mod 2 = 0">#aaaacc</xsl:when>
                <xsl:otherwise>#ccccee</xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
            <td/><td><xsl:value-of select="text()"/></td>
          </tr>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="session_dump">
    <tr>
      <td colspan="2" bgcolor="#dd9999"><b>Session keys and values:</b></td>
    </tr>
    <xsl:for-each select="session_dump/pair">
      <tr>
        <xsl:attribute name="bgcolor">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">#aaaacc</xsl:when>
            <xsl:otherwise>#ccccee</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <td valign="top" style="font-family: monospace">
          <xsl:call-template name="linewrap">
            <xsl:with-param name="str" select="@key"/>
            <xsl:with-param name="len" select="33"/>
          </xsl:call-template>  
        </td>
        <td><pre><xsl:value-of select="text()"/></pre></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="exception">
    <tr>
      <td colspan="2" bgcolor="#dd9999">
        <b>
          <xsl:choose>
            <xsl:when test="parent::exception">Cause:</xsl:when>
            <xsl:otherwise>Exception:</xsl:otherwise>
          </xsl:choose>
        </b>
      </td>
    </tr>
    <tr bgcolor="#ccccee">
      <td>Type</td>
      <td><xsl:value-of select="@type"/></td></tr>
    <tr bgcolor="#aaaacc">
      <td valign="top">Message</td>
      <xsl:choose>
        <xsl:when test="@msg != ''">
          <td><xsl:value-of select="@msg"/></td>
        </xsl:when>
        <xsl:otherwise>
          <td>Not available</td>
        </xsl:otherwise> 
      </xsl:choose>
    </tr>
    <xsl:if test="xsltinfo">
    <tr bgcolor="#ccccee">
      <td>Location</td>
      <td><xsl:value-of select="xsltinfo/@systemId"/><br/>
          Line: <xsl:value-of select="xsltinfo/@line"/> Column: <xsl:value-of select="xsltinfo/@column"/> 
          </td></tr>
    </xsl:if>
    <tr>
      <xsl:choose>
        <xsl:when test="xsltinfo">
          <xsl:attribute name="bgcolor">#aaaacc</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#ccccee</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="stackid">stack<xsl:value-of select="generate-id()"/></xsl:variable>
      <td valign="top">
        Stacktrace 
      </td>
      <td>
        <div id="{$stackid}_short">
          <span style="margin-right: 5px; cursor:pointer; color:#000000; font-family:monospace; border-style:groove; border-width:2px; border-color:#ccccee;" onclick="document.getElementById('{$stackid}').style.display='block';document.getElementById('{$stackid}_short').style.display='none'">+</span>
          <xsl:value-of select="stacktrace/line"/>
        </div>
        <div id="{$stackid}" style="display:none">  
          <span style="margin-right: 5px; cursor:pointer; color:#000000; font-family:monospace; border-style:groove; border-width:2px; border-color:#ccccee;" onclick="document.getElementById('{$stackid}').style.display='none';document.getElementById('{$stackid}_short').style.display='block'">-</span>
          <xsl:for-each select="stacktrace/line">
            <xsl:value-of select="text()"/><br/>
          </xsl:for-each>
        </div>
      </td>
    </tr>
    <xsl:apply-templates select="exception"/>
  </xsl:template>
  
  <xsl:template name="linewrap">
    <xsl:param name="str"/>
    <xsl:param name="len"/>
    <xsl:choose>
      <xsl:when test="string-length($str) &gt; $len">
        <div>
          <xsl:value-of select="substring($str,0,$len)"/>
        </div>
        <xsl:call-template name="linewrap">
          <xsl:with-param name="str" select="substring($str,$len)"/>
          <xsl:with-param name="len" select="$len"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <div>
          <xsl:value-of select="$str"/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
