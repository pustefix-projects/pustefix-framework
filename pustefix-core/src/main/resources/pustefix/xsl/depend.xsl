<!-- -*- mode: xsl -*- -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:include href="lib.xsl"/>
  <xsl:param name="prohibitEdit"/>
  <xsl:param name="projectsFile"/>
  
  <!-- Saxon/Xalan incompatibility/bug workaround: while the condition [@name=/make/@project] works 
  for Xalan, Saxon needs [@name=current()/make/@project] to select an external document node based
  on a source node within a global variable. Using another variable instead of a xpath expression
  works for both transformers: [@name=$projectName] -->
  <xsl:variable name="projectName" select="/make/@project"/>
  <xsl:variable name="encoding">UTF-8</xsl:variable>
  
  <xsl:template name="encoding">
    <xsl:if test="not(/make/global/param[@name='outputencoding'] or ./param[@name='outputencoding'])">
      <param name="outputencoding" value="{$encoding}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="global">
    <render-params>
      <xsl:if test="include">
        <param name="stylesheets_to_include">
          <xsl:attribute name="value">
            <xsl:for-each select="include">
              <xsl:if test="@module">module://<xsl:value-of select="@module"/>/</xsl:if><xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
            </xsl:for-each>
          </xsl:attribute>
        </param>
      </xsl:if>
      <xsl:apply-templates select="param[not(@name = 'page')]"/>>
    </render-params>
  </xsl:template>

  <xsl:template match="standardmaster">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <xsl:param name="thename">
      <xsl:choose>
        <xsl:when test="@name">master-<xsl:value-of select="@name"/>.xsl</xsl:when>
        <xsl:otherwise>master.xsl</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <target type="xsl">
      <xsl:attribute name="name"><xsl:value-of select="$thename"/></xsl:attribute>
      <xsl:call-template name="render_themes">
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="xsl/master.xsl" module="pustefix-core"/>
      <depxsl name="xsl/customizemaster.xsl" module="pustefix-core"/>
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
    <xsl:param name="thename">
      <xsl:choose>
        <xsl:when test="@name">metatags-<xsl:value-of select="@name"/>.xsl</xsl:when>
        <xsl:otherwise>metatags.xsl</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <target type="xsl">
      <xsl:attribute name="name"><xsl:value-of select="$thename"/></xsl:attribute>
      <xsl:call-template name="render_themes">
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="xsl/metatags.xsl" module="pustefix-core"/>
      <depxsl name="xsl/customizemaster.xsl" module="pustefix-core"/>
      <depaux name="{$project}/WEB-INF/depend.xml"/>
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
    <xsl:if test="not(/make/standardpage[@name = current()/@name and not(@variant)] or /make/target[@page = current()/@name and not(@variant)])">
      <xsl:message terminate="yes">*** Can't create a variant of a page that's not defined! ***</xsl:message>
    </xsl:if>
    <xsl:variable name="mastername">
      <xsl:choose>
        <xsl:when test="@master">master-<xsl:value-of select="@master"/>.xsl</xsl:when>
        <xsl:otherwise>master.xsl</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="metatagsname">
      <xsl:choose>
        <xsl:when test="@metatags">metatags-<xsl:value-of select="@metatags"/>.xsl</xsl:when>
        <xsl:otherwise>metatags.xsl</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thename">
      <xsl:choose>
        <xsl:when test="@variant"><xsl:value-of select="@name"/>::<xsl:value-of select="@variant"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <target name="{$thename}.xsl" type="xsl" page="{@name}">
      <xsl:if test="@variant">
        <xsl:attribute name="variant"><xsl:value-of select="@variant"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="@defining-module">
        <xsl:attribute name="defining-module"><xsl:value-of select="@defining-module"/></xsl:attribute>
      </xsl:if>
      <xsl:call-template name="render_themes">
        <xsl:with-param name="variant" select="@variant"/>
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <depxml name="{$thename}.xml"/>
      <depxsl>
        <xsl:attribute name="name"><xsl:value-of select="$mastername"/></xsl:attribute>
      </depxsl>
      <xsl:if test="./include or /make/global/include">
        <param name="stylesheets_to_include">
          <xsl:attribute name="value">
            <xsl:for-each select="include">
              <xsl:if test="@module">module://<xsl:value-of select="@module"/>/</xsl:if><xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
            </xsl:for-each>
            <xsl:for-each select="/make/global/include">
              <xsl:if test="@module">module://<xsl:value-of select="@module"/>/</xsl:if><xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
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
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>

    <target name="{$thename}.xml" type="xml">
      <xsl:call-template name="render_themes">
        <xsl:with-param name="variant" select="@variant"/>
        <xsl:with-param name="local_themes" select="@themes"/>
      </xsl:call-template>
      <xsl:if test="@defining-module">
        <xsl:attribute name="defining-module"><xsl:value-of select="@defining-module"/></xsl:attribute>
      </xsl:if>
      <depxml name="{@xml}">
        <xsl:if test="@module"><xsl:attribute name="module"><xsl:value-of select="@module"/></xsl:attribute></xsl:if>
      </depxml>
      <depxsl>
        <xsl:attribute name="name"><xsl:value-of select="$metatagsname"/></xsl:attribute>
      </depxsl>
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
      <param name="stylesheets_to_include">
        <xsl:attribute name="value">
          <xsl:for-each select="include">
            <xsl:if test="@module">module://<xsl:value-of select="@module"/>/</xsl:if><xsl:value-of select="@stylesheet"/><xsl:text> </xsl:text>
          </xsl:for-each>
        </xsl:attribute>
      </param>
    </xsl:if>
  </xsl:template>

  <xsl:template match="target">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <xsl:if test="@page and not(/make/standardpage[@name = current()/@page and not(@variant)] or
                                                   /make/target[@page = current()/@page and not(@variant)])">
      <xsl:message terminate="yes">*** Can't create a variant of a page that's not defined! ***</xsl:message>
    </xsl:if>
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates select="*[not(name() = 'param' and (@name='page' or @name='product' or @name='lang'))]"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
      <xsl:choose>
         <xsl:when test="@page">
	   <param name="page" value="{@page}"/>
	 </xsl:when>
	 <xsl:when test="./param[@name='page']">
	   <xsl:apply-templates select="./param[@name='page']"/>
	 </xsl:when>
      </xsl:choose>
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

