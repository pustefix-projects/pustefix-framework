<!-- -*- mode: xsl -*- -->
<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="cus xsl">

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  <xsl:include href="create_lib.xsl"/>

  <xsl:param name="prohibitEdit"/>

  <xsl:template match="global"/>

  <xsl:template match="standardmaster">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <target name="master.xsl" type="xsl">
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
    <target name="{@name}.xsl" type="xsl">
      <depxml name="{@name}.xml"/>
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
      <xsl:variable name="allp" select="./param"/>
      <xsl:for-each select="/make/global/param">
        <xsl:variable name="pn"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:if test="not($allp[@name = $pn])">
          <xsl:apply-templates select="current()"/>
        </xsl:if>
      </xsl:for-each>
      <xsl:apply-templates select="param"/>
      <param name="page" value="{@name}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>

    <target name="{@name}.xml" type="xml">
      <depxml name="{@xml}"/>
      <depxsl name="metatags.xsl"/>
      <xsl:variable name="allp" select="./param"/>
      <xsl:for-each select="/make/global/param">
        <xsl:variable name="pn"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:if test="not($allp[@name = $pn])">
          <xsl:apply-templates select="current()"/>
        </xsl:if>
      </xsl:for-each>
      <xsl:apply-templates select="param"/>
      <param name="page" value="{@name}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>
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
      <xsl:apply-templates select="*[not(name() = 'param' and (@name='product' or @name='lang'))]"/>
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

