<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
  <xsl:template match="projects">
    <projects>
      <xsl:apply-templates select="./project"/>
    </projects>
  </xsl:template>

  <xsl:template match="project">
    <xsl:if test="servlet[@useineditor = 'true']">
      <project name="{@name}">
        <comment><xsl:apply-templates select="./comment/node()"/></comment>
	<depend><xsl:apply-templates select="./depend/node()"/></depend>
        <xsl:for-each select="./namespace">
          <namespace uri="{@uri}" prefix="{@prefix}"/>
        </xsl:for-each>
        <xsl:for-each select="./servlet[@useineditor = 'true']">
          <handler>
            <xsl:attribute name="name">/xml/<xsl:value-of select="@name"/></xsl:attribute>
	    <properties><xsl:apply-templates select="./propfile/node()"/></properties>
	  </handler>
	</xsl:for-each>
        <xsl:for-each select="/projects/common/documentation/doc_file">
          <documentation>
            <xsl:apply-templates select="./node()"/>
          </documentation>
        </xsl:for-each> 
        <xsl:for-each select="./documentation/doc_file">
          <documentation>
            <xsl:apply-templates select="./node()"/>
          </documentation>
        </xsl:for-each> 
      </project>
    </xsl:if>
  </xsl:template>
    
</xsl:stylesheet>

