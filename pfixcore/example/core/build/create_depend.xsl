<!-- -*- mode: xsl -*- -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>

  <xsl:include href="create_lib.xsl"/>
  <xsl:param name="prohibitEdit"/>
  <xsl:param name="projectsFile"/>
  
  <xsl:variable name="defaultEncoding">UTF-8</xsl:variable>
  <xsl:variable name="projectEncoding" select="normalize-space(document($projectsFile)/projects/project[@name=current()/make/@project]/encoding/text())"/>
  <xsl:variable name="encoding">
    <xsl:choose>
      <xsl:when test="$projectEncoding and string-length($projectEncoding) &gt; 0">
        <xsl:value-of select="$projectEncoding"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$defaultEncoding"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:template name="encoding">
    <xsl:if test="not(/make/global/param[@name='outputencoding'] or ./param[@name='outputencoding'])">
      <param name="outputencoding" value="{$encoding}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="global"/>

  <xsl:template match="standardmaster">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <target name="master.xsl" type="xsl">
      <xsl:call-template name="render_themes">
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="core/xsl/master.xsl"/>
      <depxsl name="core/xsl/customizemaster.xsl"/>
      <depaux name="core/xsl/default_copy.xsl"/>
      <depaux name="core/xsl/include.xsl"/>
      <depaux name="core/xsl/utils.xsl"/>
      <depaux name="core/xsl/navigation.xsl"/>
      <depaux name="core/xsl/forminput.xsl"/>
      <depaux name="{$project}/conf/depend.xml"/>
      <xsl:call-template name="render_include_ssheets"/>
      <xsl:apply-templates select="param"/>
      <xsl:apply-templates select="depaux"/>
      <param  name="product" value="{$project}"/>
      <param  name="lang" value="{$lang}"/>
    </target>
  </xsl:template>

  <xsl:template match="standardmetatags">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <target name="metatags.xsl" type="xsl">
      <xsl:call-template name="render_themes">
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="core/xsl/metatags.xsl"/>
      <depxsl name="core/xsl/customizemaster.xsl"/>
      <depaux name="core/xsl/default_copy.xsl"/>
      <depaux name="core/xsl/include.xsl"/>
      <depaux name="core/xsl/utils.xsl"/>
      <depaux name="{$project}/conf/depend.xml"/>
      <xsl:call-template name="render_include_ssheets"/>
      <xsl:apply-templates select="param"/>
      <xsl:apply-templates select="depaux"/>
      <param  name="product" value="{$project}"/>
      <param  name="lang" value="{$lang}"/>
    </target>
  </xsl:template>

  <xsl:template match="standardpage">
    <xsl:if test="not(@name)">
      <xsl:message terminate="yes">*** standardpage needs to have a "name" attribute given! ***</xsl:message>
    </xsl:if>
    <xsl:if test="not(/make/standardpage[@name = current()/@name and not(@variant)])">
      <xsl:message terminate="yes">*** Can't create a variant of a page that's not defined! ***</xsl:message>
    </xsl:if>
    <xsl:variable name="thename">
      <xsl:choose>
        <xsl:when test="@variant"><xsl:value-of select="@name"/>::<xsl:value-of select="@variant"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <target name="{$thename}.xsl" type="xsl">
      <xsl:call-template name="render_themes">
        <xsl:with-param name="variant" select="@variant"/>
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="{$thename}.xml"/>
      <depxsl name="master.xsl"/>
      <xsl:if test="./include or /make/global/include">
        <xsl:for-each select="./include">
          <depaux name="{@stylesheet}"/>
        </xsl:for-each>
        <xsl:for-each select="/make/global/include">
          <depaux name="{@stylesheet}"/>
        </xsl:for-each>
        <param name="stylesheets_to_include">
          <xsl:attribute name="value">
            <xsl:for-each select="include">
              <xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
            </xsl:for-each>
            <xsl:for-each select="/make/global/include">
              <xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
            </xsl:for-each>
          </xsl:attribute>
        </param>
      </xsl:if>
      <xsl:call-template name="encoding"/>
      <xsl:variable name="allp" select="./param[not(@name = 'page')]"/>
      <xsl:for-each select="/make/global/param[not(@name = 'page')]">
        <xsl:variable name="pn"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:if test="not($allp[@name = $pn])">
          <xsl:apply-templates select="current()"/>
        </xsl:if>
      </xsl:for-each>
      <xsl:apply-templates select="$allp"/>
      <param name="page" value="{@name}"/>
      <xsl:if test="@variant">
        <param name="variant" value="{@variant}"/>
      </xsl:if>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>

    <target name="{$thename}.xml" type="xml">
      <xsl:call-template name="render_themes">
        <xsl:with-param name="variant" select="@variant"/>
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="{@xml}"/>
      <depxsl name="metatags.xsl"/>
      <xsl:call-template name="encoding"/>
      <xsl:variable name="allp" select="./param[not(@name = 'page')]"/>
      <xsl:for-each select="/make/global/param[not(@name = 'page')]">
        <xsl:variable name="pn"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:if test="not($allp[@name = $pn])">
          <xsl:apply-templates select="current()"/>
        </xsl:if>
      </xsl:for-each>
      <xsl:apply-templates select="$allp"/>
      <param name="page" value="{@name}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>
  </xsl:template>

  <xsl:template name="render_themes">
    <xsl:param name="variant"/>
    <xsl:param name="local_themes"/>
    <xsl:if test="not((not($variant) or $variant = '') and (not($local_themes) or $local_themes = ''))">
      <xsl:variable name="global_themes">
        <xsl:choose>
          <xsl:when test="$local_themes"><xsl:value-of select="$local_themes"/></xsl:when>
          <xsl:when test="/make/@themes"><xsl:value-of select="/make/@themes"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="/make/@project"/> default</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="fullthemes"><xsl:call-template name="recurse_variant">
        <xsl:with-param name="variant_tail" select="$variant"/>
      </xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$global_themes"/></xsl:variable>
      <xsl:attribute name="themes"><xsl:value-of select="normalize-space($fullthemes)"/></xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="recurse_variant">
    <xsl:param name="variant_tail"/>
    <xsl:param name="variant_list"/>
    <xsl:choose>
      <xsl:when test="contains($variant_tail, ':')">
        <xsl:variable name="curr_list">
          <xsl:value-of select="substring-before($variant_tail, ':')"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$variant_list"/>
        </xsl:variable>
        <xsl:variable name="remain_tail"><xsl:value-of select="substring-after($variant_tail, ':')"/></xsl:variable>
        <xsl:call-template name="recurse_variant">
          <xsl:with-param name="variant_tail" select="$remain_tail"/>
          <xsl:with-param name="variant_list" select="$curr_list"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(concat($variant_tail, ' ', $variant_list))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="render_include_ssheets">
    <xsl:if test="./include">
      <xsl:for-each select="include">
        <depaux name="{@stylesheet}"/>
      </xsl:for-each>
      <param name="stylesheets_to_include">
        <xsl:attribute name="value">
          <xsl:for-each select="include">
            <xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
          </xsl:for-each>
        </xsl:attribute>
      </param>
    </xsl:if>
  </xsl:template>

  <xsl:template match="target">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates select="*[not(name() = 'param' and (@name='product' or @name='themes' or @name='lang'))]"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
      <param  name="product" value="{$project}"/>
      <param  name="lang" value="{$lang}"/>
    </xsl:copy>
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
    <param name="{@name}" value="{$value}"/>
  </xsl:template>

  <xsl:template match="depaux">
    <depaux name="{@name}">
      <xsl:if test="@type">
        <xsl:copy-of  select="@type"/>
      </xsl:if>
    </depaux>
  </xsl:template>

</xsl:stylesheet>

