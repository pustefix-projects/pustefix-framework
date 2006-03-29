<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <html>
      <head>
        <title>Error</title>
      </head>
      <body style="font-size:xx-small">
        <br/><br/>
        <div align="center">
          <table cellpadding="2" cellspacing="0" style="background-color: #aaaacc; border: 1px solid black">
            <tr>
              <td align="center" bgcolor="#cc0000" colspan="2">
                <span style="color:#ffffff; font-weight: bold">
                  <xsl:choose>
         	    <xsl:when test="/error[@type ='xslt']">
                      XML/XSLT Error!
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



  <xsl:template match="/error[@type != 'xslt']">
    <xsl:call-template name="exception"/>
    <xsl:call-template name="sessioninfo"/>
    <xsl:call-template name="requestparams"/>
    <xsl:call-template name="laststeps"/>
    <xsl:call-template name="session_dump"/>
  </xsl:template>
  
  <xsl:template match="/error[@type = 'xslt']">
    <xsl:call-template name="exceptions"/>
  </xsl:template>


  <xsl:template name="exceptions">
    <xsl:for-each select="exception">
      <tr>
        <td bgcolor="#dd9999" colspan="2" align="center">
          <b><xsl:value-of select="@type"/></b>
        </td>
      </tr>
      <xsl:for-each select="info">
        <tr>
          <xsl:attribute name="bgcolor">
            <xsl:choose>
              <xsl:when test="count(preceding-sibling::info) mod 2 = 0">#aaaacc</xsl:when>
              <xsl:otherwise>#ccccee</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <td><b><xsl:value-of select="@key"/>:</b></td>
          <td><xsl:value-of select="@value"/></td>
        </tr>
      </xsl:for-each>
    </xsl:for-each>
  
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
        <td valign="top"><xsl:value-of select="@key"/></td>
        <td><pre><xsl:value-of select="text()"/></pre></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="exception">
    <tr>
      <td colspan="2" bgcolor="#dd9999"><b>Exception:</b></td>
    </tr>
    <tr bgcolor="#ccccee"><td>Type</td><td><xsl:value-of select="exception/@type"/></td></tr>
    <tr bgcolor="#aaaacc">
      <td>Message</td>
      <xsl:choose>
        <xsl:when test="exception/@msg != ''">
          <td><xsl:value-of select="exception/@msg"/></td>
        </xsl:when>
        <xsl:otherwise>
          <td>Not available</td>
        </xsl:otherwise> 
      </xsl:choose>
    </tr>
    <xsl:for-each select="exception/stacktrace/line">
      
      <tr>
        <xsl:choose>
        <xsl:when test="position() = 1">
          <td bgcolor="#ccccee">Stacktrace</td>
        </xsl:when>
        <xsl:otherwise>
          <td/>
        </xsl:otherwise>
        </xsl:choose>
        <td>
          <xsl:attribute name="bgcolor">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">#aaaacc</xsl:when>
              <xsl:otherwise>#ccccee</xsl:otherwise>
            </xsl:choose> 
          </xsl:attribute>
          <xsl:value-of select="text()"/>
        </td>
      </tr>
    </xsl:for-each>
    
  </xsl:template>


  
  
</xsl:stylesheet>
