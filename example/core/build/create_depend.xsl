<!-- -*- mode: xml -*- -->
<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  <xsl:include href="create_lib.xsl"/>

  <xsl:param name="prohibitEdit"/>

  <xsl:template match="make">
    <xsl:param name="cache" select="./@cachedir"/> 
    <xsl:param name="recallow" select="./@record_allowed"/>
    <xsl:param name="recdir" select="./@record_dir"/>
    <xsl:if test="not($cache)">
      <xsl:message terminate="yes">
        *** Error *** You must specify a cachedir attribute to the make node
      </xsl:message>
    </xsl:if>
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:choose>
        <xsl:when test="not(starts-with($cache, '/'))"> <!-- The path isn't absolute -->
          <xsl:attribute name="cachedir"><xsl:value-of select="concat($docroot,'/',$cache)"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="cachedir"><xsl:value-of select="$cache"/></xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>            
      <xsl:if test="$recdir">
        <xsl:choose>
        <xsl:when test="not(starts-with($recdir, '/'))"> <!-- The path isn't absolute -->
          <xsl:attribute name="record_dir"><xsl:value-of select="concat($docroot, '/', $recdir)"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="record_dir"><xsl:value-of select="$recdir"/></xsl:attribute>
        </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:attribute name="docroot"><xsl:value-of select="$docroot"/></xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="target">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
      <param name="docroot"><xsl:attribute name="value"><xsl:value-of select="$docroot"/></xsl:attribute></param>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="standardpage">
    <target name="{@name}.xsl" type="xsl">
      <depxml name="{@name}.xml"/>
      <depxsl name="master.xsl"/>
      <xsl:apply-templates/>
      <param name="page" value="{@name}"/>
      <param name="docroot" value="{$docroot}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
      <xsl:if test="@outputmethod">
        <param name="outputmethod" value="{@outputmethod}"/>
      </xsl:if>
    </target>
    
    <target name="{@name}.xml" type="xml">
      <depxml name="{@xml}"/>
      <depxsl name="metatags.xsl"/>
      <xsl:apply-templates/>
      <param name="page" value="{@name}"/>
      <param name="docroot" value="{$docroot}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>
  </xsl:template>
  
  <xsl:template match="param">
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">
          <xsl:value-of select="@value"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <param>
      <xsl:copy-of select="@name"/>
      <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
    </param>
  </xsl:template>
  
  <xsl:template match="depaux">
    <depaux>
      <xsl:attribute name="name"><xsl:value-of select="concat($docroot,'/',@name)"/></xsl:attribute>
      <xsl:if test="@type">
        <xsl:copy-of  select="@type"/>
      </xsl:if>
    </depaux>
  </xsl:template>
    
  <!--<xsl:template match="*" name="copy">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>-->
  
</xsl:stylesheet>

