<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">

  <xsl:template match="xmloutput">
    <xsl:for-each select="*">
      <xsl:call-template name="xmlformat"/>
    </xsl:for-each>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template name="xmlformat">
   <table class="xmloutput"><tr><td>
    <span class="xmloutput">
    &lt;<span class="tagname"><xsl:value-of select="name()"/><xsl:text> </xsl:text></span>
    <xsl:for-each select="@*">
      <xsl:text> </xsl:text><span class="attname"><xsl:value-of select="name()"/></span>="<span class="attval"><xsl:value-of select="."/></span>"&#160;
    </xsl:for-each>
    /&gt;
    </span>
    </td></tr></table>
  </xsl:template>

  <xsl:template match="partinfo">
    <table class="partinfo" cellspacing="0">
      <xsl:if test="@module">
        <xsl:attribute name="class">partinfo partinfo_module</xsl:attribute>
      </xsl:if>
      <tr>
        <th>Part:</th>
        <td><xsl:value-of select="@part"/></td>
      </tr>
      <tr>
        <th>Theme:</th>
        <td><xsl:value-of select="@theme"/></td>
      </tr>
      <tr>
        <th>Path:</th>
        <td><xsl:value-of select="@path"/></td>
      </tr>
      <xsl:if test="@module">
        <tr>
          <th>Module:</th>
          <td><xsl:value-of select="@module"/></td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>
  
  <xsl:template match="xmlinfo">
    <table class="xmlinfo">
      <tr>
        <th>Path:</th>
        <td><xsl:value-of select="@path"/></td>
      </tr>
      <xsl:if test="@module">
        <tr>
          <th>Module:</th>
          <td><xsl:value-of select="@module"/></td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>

</xsl:stylesheet>
