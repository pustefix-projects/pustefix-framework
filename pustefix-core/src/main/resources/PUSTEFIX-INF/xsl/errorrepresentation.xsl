<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <html>
      <head>
        <title>Error</title>
        <style>
          body {
            font-family: sans-serif;
          }
          table.error {
            background: #aaaacc;
            border: 1px solid black;
          }
          table.error td {
            padding: 4px;
          }
          td.head {
            background: #dd9999;
          }
          td.pre {
            font-family: monospace;
            white-space: pre;
          }
          table.error > tbody > tr:nth-child(odd) {
            background: #ccccee;
          }
          table.error > tbody > tr:nth-child(even) {
            background: #aaaacc;
          }
          table.stack td {
            padding: 0px;
          }
          span.open {
            margin-right: 5px;
            cursor: pointer;
            color: #000000;
            font-family: monospace;
            border-style: groove;
            border-width: 2px;
            border-color: #ccccee;
          }
          span.close {
            margin-right: 5px;
            cursor: pointer;
            color: #000000;
            font-family: monospace;
            border-style: groove;
            border-width: 2px;
            border-color: #ccccee;
          }
        </style>
      </head>
      <body>
        <div align="center">
          <table cellpadding="3" cellspacing="0" class="error">
            <tbody>
              <tr>
                <td align="center" bgcolor="#cc0000" colspan="2">
                  <span style="color:#ffffff; font-weight: bold; letter-spacing: 0.2em; text-transform:uppercase;">
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
            </tbody>
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
      <xsl:if test="$rootcause/xsltinfo/xmlinfo/@context">
        <div style="font-size:80%; padding-top: 0.5em;">XML: <xsl:value-of select="$rootcause/xsltinfo/xmlinfo/@systemId"/></div>
        <pre style="font-size: 80%; padding: 0.5em; border: 1px dotted #333; margin: 0.2em; background: #ccccee;">
          <xsl:value-of select="$rootcause/xsltinfo/xmlinfo/@context"/>
        </pre>
      </xsl:if>
      <xsl:if test="$rootcause/xsltinfo/@context">
        <div style="font-size:80%; padding-top: 0.5em;">XSL: <xsl:value-of select="$rootcause/xsltinfo/@systemId"/></div>
        <pre style="font-size: 80%; padding: 0.5em; border: 1px dotted #333; margin: 0.2em; background: #ccccee;">
          <xsl:value-of select="$rootcause/xsltinfo/@context"/>
        </pre>
      </xsl:if>
      <xsl:if test="$rootcause/xsltinfo/messages">
        <div style="font-size:80%; padding-top: 0.5em;">XSL messages:</div>
        <pre title="XSL message output" style="font-size: 80%; padding: 0.5em; border: 1px dotted #333; background: #ccccee; margin: 0.2em;"><xsl:value-of select="$rootcause/xsltinfo/messages"/></pre>
      </xsl:if>
      </td></tr>
    </xsl:if>
    <xsl:if test="@type='xslt_ext'">
      <tr><td colspan="2">
      <xsl:variable name="rootcause" select="..//exception[@type='de.schlund.pfixxml.util.XsltExtensionFunctionException']"/>
      XSLT extension function error at <i><xsl:value-of select="$rootcause/xsltinfo/@systemId"/></i>
      (line <i><xsl:value-of select="$rootcause/xsltinfo/@line"/>, column <xsl:value-of select="$rootcause/xsltinfo/@column"/></i>):<br/>
      <b><xsl:value-of select="$rootcause/@type"/>: <xsl:value-of select="$rootcause/@msg"/></b>
      <xsl:if test="$rootcause/xsltinfo/xmlinfo/@context">
        <div style="font-size:80%; padding-top: 0.5em;">XML: <xsl:value-of select="$rootcause/xsltinfo/xmlinfo/@systemId"/></div>
        <pre style="font-size: 80%; padding: 0.5em; border: 1px dotted #333; margin: 0.2em; background: #ccccee;">
          <xsl:value-of select="$rootcause/xsltinfo/xmlinfo/@context"/>
        </pre>
      </xsl:if>
      <xsl:if test="$rootcause/xsltinfo/@context">
        <div style="font-size:80%; padding-top: 0.5em;">XSL: <xsl:value-of select="$rootcause/xsltinfo/@systemId"/></div>
        <pre style="font-size: 80%; padding: 0.5em; border: 1px dotted #333; margin: 0.2em; background: #ccccee;">
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
      <td colspan="2" class="head"><b>Session:</b></td>
    </tr>
    <tr>
      <td>ID:</td>
      <td>
        <xsl:value-of select="sessioninfo/text()"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="requestparams">
    <xsl:if test="requestparams/param">
      <tr>
        <td colspan="2" class="head"><b>Request Parameter:</b></td>
      </tr>
      <xsl:for-each select="requestparams/param">
        <tr>
          <td><xsl:value-of select="@key"/></td>
          <td><xsl:value-of select="text()"/></td>
        </tr>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="laststeps">
    <xsl:if test="laststeps/step">
      <tr>
        <td colspan="2" class="head"><b>Last steps:</b></td>
      </tr>
      <xsl:for-each select="laststeps/step">
        <tr>
          <td/><td><xsl:value-of select="text()"/></td>
        </tr>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="session_dump">
    <tr>
      <td colspan="2" class="head"><b>Session keys and values:</b></td>
    </tr>
    <xsl:for-each select="session_dump/pair">
      <tr>
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
      <td colspan="2" class="head">
        <b>
          <xsl:choose>
            <xsl:when test="parent::exception">Cause:</xsl:when>
            <xsl:otherwise>Exception:</xsl:otherwise>
          </xsl:choose>
        </b>
      </td>
    </tr>
    <tr>
      <td>Type</td>
      <td><xsl:value-of select="@type"/></td></tr>
    <tr>
      <td valign="top">Message</td>
      <xsl:choose>
        <xsl:when test="@msg != ''">
          <td><xsl:value-of select="@msg"/></td>
        </xsl:when>
        <xsl:otherwise>
          <td>-</td>
        </xsl:otherwise> 
      </xsl:choose>
    </tr>
    <xsl:if test="@string">
    <tr>
      <td valign="top">ToString</td>
      <td class="pre"><xsl:value-of select="@string"/></td>
    </tr>
    </xsl:if>
    <xsl:if test="xsltinfo">
    <tr>
      <td>Location</td>
      <td><xsl:value-of select="xsltinfo/@systemId"/><br/>
          Line: <xsl:value-of select="xsltinfo/@line"/> Column: <xsl:value-of select="xsltinfo/@column"/> 
          </td></tr>
    </xsl:if>
    <tr>
      <xsl:variable name="stackid">stack<xsl:value-of select="generate-id()"/></xsl:variable>
      <td valign="top">
        Stacktrace 
      </td>
      <td>
        <div id="{$stackid}_short">
          <table class="stack">
            <tr>
              <td valign="top">
                <span class="open" onclick="document.getElementById('{$stackid}').style.display='block';document.getElementById('{$stackid}_short').style.display='none'">+</span>
              </td>
              <td class="pre">
                <xsl:value-of select="stacktrace/line"/>
              </td>
            </tr>
          </table>
        </div>
        <div id="{$stackid}" style="display:none">  
          <table class="stack">
            <tr>
              <td valign="top">
                <span class="close" onclick="document.getElementById('{$stackid}').style.display='none';document.getElementById('{$stackid}_short').style.display='block'">-</span>
              </td>
              <td class="pre">
                <xsl:for-each select="stacktrace/line">
                  <xsl:value-of select="text()"/><br/>
                </xsl:for-each>
              </td>
            </tr>
          </table>
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
