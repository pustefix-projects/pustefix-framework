<xsl:stylesheet version="1.0"
                exclude-result-prefixes="xsl cus" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>

  <xsl:param name="pwd"/>

  <xsl:template match="/">
    <cleanup>
      <xsl:apply-templates select="/checkresult/includefiles/incfile"/>
    </cleanup>
  </xsl:template>

  <xsl:template match="incfile">
    <xsl:choose>
      <xsl:when test="not(./part/theme[not(@UNUSED = 'true')])">
        <clean type="file" path="{@name}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="./part">
          <xsl:with-param name="file"><xsl:value-of select="substring-after(@name, $pwd)"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="part">
    <xsl:param name="file"/>
    <xsl:choose>
      <xsl:when test="not(./theme[not(@UNUSED = 'true')])">
        <clean type="part" path="{$file}" part="{@name}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="./theme">
          <xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
          <xsl:with-param name="part"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="theme">
    <xsl:param name="file"/>
    <xsl:param name="part"/>
    <xsl:if test="@UNUSED = 'true'">
      <clean type="theme" path="{$file}" part="{$part}" theme="{@name}"/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
