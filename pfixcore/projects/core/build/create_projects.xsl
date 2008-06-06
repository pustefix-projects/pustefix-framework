<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>
  <xsl:param name="files"/>

  <xsl:template match="insertexternalprojects">
    <xsl:call-template name="recurser">
      <xsl:with-param name="remaining" select="$files"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="recurser">
    <xsl:param name="remaining"/>
    
    <xsl:variable name="file">
      <xsl:choose>
        <xsl:when test="contains($remaining, '|')">
          <xsl:value-of select="substring-before($remaining, '|')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$remaining"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="not($file = '')">
      <xsl:apply-templates select="document(concat($docroot, '/', $file))"/>
    </xsl:if>
    
    <xsl:if test="contains($remaining, '|')">
      <xsl:call-template name="recurser">
        <xsl:with-param name="remaining" select="substring-after($remaining, '|')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
