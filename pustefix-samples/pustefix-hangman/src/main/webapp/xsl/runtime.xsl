<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="highscoreloop">
    <xsl:param name="start" select="1"/>
    <xsl:param name="stop" select="1"/>
    <xsl:if test="$start &lt; $stop">
      <tr>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$start mod 2 = 1">odd</xsl:when>
            <xsl:otherwise>even</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <td>-</td>
        <td>-</td>
        <td align="right">-</td>
        <td align="right">-</td>
      </tr>
      <xsl:call-template name="highscoreloop">
        <xsl:with-param name="start" select="$start + 1"/>
        <xsl:with-param name="stop" select="$stop"/>
      </xsl:call-template>
    </xsl:if>  
  </xsl:template>
  
  <xsl:template name="toLowerCase">
    <xsl:param name="text"/>
    <xsl:value-of select="translate($text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
  </xsl:template>
  
</xsl:stylesheet>
