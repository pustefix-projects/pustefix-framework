<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:param name="targetPrefix"></xsl:param>
  
  <xsl:template name="choose">
    <xsl:variable name="prefix">
      <xsl:if test="$targetPrefix">
        <xsl:value-of select="$targetPrefix"/><xsl:text>:</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:for-each select="./node()">
      <xsl:choose>
        <xsl:when test="self::text()|self::comment()|self::processing-instruction()">
          <xsl:apply-templates select="."/>
        </xsl:when>
        <xsl:when test="local-name(.) = 'test'">
          <xsl:choose>
            <xsl:when test="not(./@mode) and not(./@uid) and not(./@machine)">
              <xsl:if test="preceding-sibling::*[not(./@mode) and not(./@uid) and not(./@machine)]">
                <xsl:message terminate="yes">
                  More than one unconditional test element per choose is not allowed!
                </xsl:message>
              </xsl:if>
              <xsl:element name="{$prefix}otherwise" namespace="{$targetNamespace}">
                <xsl:apply-templates select="./node()"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="conj">
                <xsl:choose>
                  <xsl:when test="./@bool = 'or'"><xsl:text> or </xsl:text></xsl:when>
                  <xsl:when test="not(./@bool)"><xsl:text> and </xsl:text></xsl:when>
                  <xsl:otherwise>
                    <xsl:message terminate="yes">
                      Only 'or' is allowed for the bool attribute!
                    </xsl:message>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="testexpr">
                <xsl:if test="./@mode">$mode='<xsl:value-of select="./@mode"/>'</xsl:if>
                <xsl:if test="./@uid"><xsl:if test="./@mode"><xsl:value-of select="$conj"/></xsl:if>$uid='<xsl:value-of select="./@uid"/>'</xsl:if>
                <xsl:if test="./@machine"><xsl:if test="./@mode or ./@uid"><xsl:value-of select="$conj"/></xsl:if>$machine='<xsl:value-of select="./@machine"/>'</xsl:if>
              </xsl:variable>
              <xsl:element name="{$prefix}when" namespace="{$targetNamespace}">
                <xsl:attribute name="test">
                  <xsl:value-of select="$testexpr"/>
                </xsl:attribute>
                <xsl:apply-templates select="./node()"/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            Found illegal sub-element in choose-element!
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="modesToTestString"><xsl:param name="modestring"/>$mode='<xsl:choose><xsl:when test="contains(normalize-space($modestring), ' ')"><xsl:value-of select="substring-before(normalize-space($modestring), ' ')"/>' or <xsl:call-template name="modesToTestString"><xsl:with-param name="modestring" select="substring-after(normalize-space($modestring), ' ')"/></xsl:call-template></xsl:when><xsl:otherwise><xsl:value-of select="normalize-space($modestring)"/>'</xsl:otherwise></xsl:choose></xsl:template>

</xsl:stylesheet>