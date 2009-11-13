<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:json="xalan://org.pustefixframework.util.json.JSONUtil"
                xmlns:func="http://exslt.org/functions"
                exclude-result-prefixes="json">
  
  <xsl:template match="pfx:json-resource">
    <xsl:choose>
      <xsl:when test="@variable-name">
        <xsl:value-of select="@variable-name"/><xsl:text>=(</xsl:text>
        <ixsl:call-template name="pfx:xml-element-to-json">
          <ixsl:with-param name="element" select="/formresult/{@node-name}"/>
        </ixsl:call-template>
        <xsl:text>);</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>(</xsl:text>
        <ixsl:call-template name="pfx:xml-element-to-json">
          <ixsl:with-param name="element" select="/formresult/{@node-name}"/>
        </ixsl:call-template>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="pfx:json-resources">
    <xsl:choose>
      <xsl:when test="@variable-name">
        <xsl:value-of select="@variable-name"/><xsl:text>=({</xsl:text>
        <xsl:for-each select="resource">
          <xsl:text>"</xsl:text>
          <xsl:choose>
            <xsl:when test="@json-name">
              <xsl:value-of select="@json-name"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@node-name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>":</xsl:text>
          <ixsl:call-template name="pfx:xml-element-to-json">
            <ixsl:with-param name="element" select="/formresult/{@node-name}"/>
          </ixsl:call-template>
          <xsl:if test="following-sibling::resource">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>});</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>({</xsl:text>
        <xsl:for-each select="resource">
          <xsl:text>"</xsl:text>
          <xsl:choose>
            <xsl:when test="@json-name">
              <xsl:value-of select="@json-name"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@node-name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>":</xsl:text>
          <ixsl:call-template name="pfx:xml-element-to-json">
            <ixsl:with-param name="element" select="/formresult/{@node-name}"/>
          </ixsl:call-template>
          <xsl:if test="following-sibling::resource">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>})</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pfx:xml-element-to-json">
    <xsl:param name="element"/>
    <xsl:value-of select="json:xmlElementToJSON($element)"/>
  </xsl:template>
  
</xsl:stylesheet>
