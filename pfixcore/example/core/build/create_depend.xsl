<!-- -*- mode: xsl -*- -->
<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  <xsl:include href="create_lib.xsl"/>

  <xsl:param name="prohibitEdit"/>

  <xsl:template match="global"/>

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

  <xsl:template match="standardmaster">
    <xsl:param name="project"><xsl:value-of select="/make/@project"/></xsl:param>
    <xsl:param name="lang"><xsl:value-of select="/make/@lang"/></xsl:param>
    <target name="master.xsl" type="xsl">
      <depxml name="core/xsl/master.xsl"/>
      <depxsl name="core/xsl/customizemaster.xsl"/>
      <depaux name="{$docroot}/core/xsl/default_copy.xsl"/>
      <depaux name="{$docroot}/core/xsl/include.xsl"/>
      <depaux name="{$docroot}/core/xsl/utils.xsl"/>
      <depaux name="{$docroot}/core/xsl/navigation.xsl"/>
      <depaux name="{$docroot}/core/xsl/forminput.xsl"/>
      <depaux name="{$docroot}/{$project}/conf/depend.xml"/>
      <xsl:call-template name="render_include_ssheets"/>
      <xsl:apply-templates select="param"/>
      <xsl:apply-templates select="depaux"/>
      <param  name="docroot" value="{$docroot}"/>
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
      <depaux name="{$docroot}/core/xsl/default_copy.xsl"/>
      <depaux name="{$docroot}/core/xsl/include.xsl"/>
      <depaux name="{$docroot}/core/xsl/utils.xsl"/>
      <depaux name="{$docroot}/{$project}/conf/depend.xml"/>
      <xsl:call-template name="render_include_ssheets"/>
      <xsl:apply-templates select="param"/>
      <xsl:apply-templates select="depaux"/>
      <param  name="docroot" value="{$docroot}"/>
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
          <depaux name="{$docroot}/{@stylesheet}"/>
        </xsl:for-each>
        <xsl:for-each select="/make/global/include">
          <depaux name="{$docroot}/{@stylesheet}"/>
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
      <param name="docroot" value="{$docroot}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>

    <target name="{@name}.xml" type="xml">
      <depxml name="{@xml}"/>
      <depxsl name="metatags.xsl"/>
      <param name="page" value="{@name}"/>
      <param name="docroot" value="{$docroot}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
    </target>
  </xsl:template>

  <xsl:template name="render_include_ssheets">
    <xsl:if test="./include">
      <xsl:for-each select="include">
        <depaux name="{$docroot}/{@stylesheet}"/>
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
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
      <param name="docroot" value="{$docroot}"/>
      <xsl:if test="not($prohibitEdit = 'no')">
        <param name="prohibitEdit" value="{$prohibitEdit}"/>
      </xsl:if>
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
    <depaux name="{$docroot}/{@name}">
      <xsl:if test="@type">
        <xsl:copy-of  select="@type"/>
      </xsl:if>
    </depaux>
  </xsl:template>


<!--   <xsl:template match="skinning_stylesheets|runtime_stylesheets"> -->
<!--     <param name="{name()}"> -->
<!--       <xsl:attribute name="value"><xsl:value-of select="./text()"/></xsl:attribute> -->
<!--     </param> -->
<!--     <xsl:call-template name="do_depaux_list"> -->
<!--       <xsl:with-param name="ssheets"> -->
<!--         <xsl:value-of select="normalize-space(./text())"/> -->
<!--       </xsl:with-param> -->
<!--     </xsl:call-template> -->
<!--   </xsl:template> -->

<!--   <xsl:template name="do_depaux_list"> -->
<!--     <xsl:param name="ssheets"/> -->
<!--     <xsl:variable name="first"> -->
<!--       <xsl:value-of select="normalize-space(substring-before(concat($ssheets, ' '), ' '))"/> -->
<!--     </xsl:variable> -->
<!--     <xsl:variable name="rest"> -->
<!--       <xsl:value-of select="normalize-space(substring-after($ssheets, ' '))"/> -->
<!--     </xsl:variable> -->
<!--     <xsl:if test="$first != ''"> -->
<!--       <depaux> -->
<!--         <xsl:attribute name="name"><xsl:value-of select="concat($docroot,'/',$first)"/></xsl:attribute> -->
<!--       </depaux> -->
<!--     </xsl:if> -->
<!--     <xsl:if test="$rest != ''"> -->
<!--       <xsl:call-template name="do_depaux_list"> -->
<!--         <xsl:with-param name="ssheets"> -->
<!--           <xsl:value-of select="$rest"/> -->
<!--         </xsl:with-param> -->
<!--       </xsl:call-template> -->
<!--     </xsl:if> -->
<!--   </xsl:template> -->

</xsl:stylesheet>

