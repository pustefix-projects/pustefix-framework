<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!--<xsl:output encoding="ISO-8859-1" indent="yes"/>-->
  <xsl:param name="projectname"/>
  <xsl:param name="docroot"/>

  <!-- apply templates for all  elements -->
  <xsl:template match="/">
    <xsl:apply-templates select="*"/>
    <xsl:text>
</xsl:text>
  </xsl:template>

  <!-- special template for comments -->
  <xsl:template match="comment()">
    <xsl:copy><xsl:copy-of select="."/></xsl:copy>
  </xsl:template>
  
  <!-- default templates, copy-trough for all unknown templates -->
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="./@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
     
  <xsl:template match="/projects/project[not(@name = $projectname)]"></xsl:template>


  <xsl:template match="/projects/project[@name = $projectname]">
    <xsl:copy>
        <xsl:copy-of select="./@*"/><xsl:apply-templates/>
    <!-- The autostart attribute will start the servlet as soon as the container starts -->
        <xsl:apply-templates select="/projects/project[@name='admcore']/servlet"/>
    </xsl:copy>
  </xsl:template>
  
  
  <xsl:template match="/projects/project/servlet/init-param[param-name/text() = 'pustefix.docroot']"><!-- no docroot specified in true webapps --></xsl:template>
  
</xsl:transform>
