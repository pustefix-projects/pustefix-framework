<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  <xsl:include href="create_lib.xsl"/>

  <!--- match the root node -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- the property template -->
  <xsl:template match="prop">
    <xsl:param name="key"><xsl:value-of select="./@name"/></xsl:param>
    <xsl:value-of select="$key"/><xsl:text>=</xsl:text>
    <xsl:apply-templates  select="./* | ./text()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- match text nodes but just if it contains a property value -->
  <xsl:template match="text()">
    <xsl:param name="doit"/>
    <xsl:if test="$doit">
      <xsl:value-of select="translate(normalize-space(.), '&#xa;&#xd;', '  ')"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="foreigncontext">
    <xsl:text>foreigncontextservlet.foreignservletname=</xsl:text>
    <xsl:value-of select="@externalservletname"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="directoutputpagerequest">
    <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.classname=</xsl:text>
    <xsl:value-of select="./directoutputstate/@class"/><xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates select="./param">
      <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="pagerequest">
    <xsl:param name="name" select="@name"/>
    <xsl:choose>
      <xsl:when test="@copyfrom">
        <xsl:variable name="copyfrom_name" select="@copyfrom"/>
        <xsl:apply-templates select="//pagerequest[@name = $copyfrom_name]">
          <xsl:with-param name="name" select="$name"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="nostore">
          <xsl:choose>
            <xsl:when test="@nostore and @nostore = 'true'">true</xsl:when>
            <xsl:otherwise>false</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="$nostore = 'true'">
          <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.nostore=true&#xa;</xsl:text>
        </xsl:if>
        <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.classname=</xsl:text>
        <xsl:choose>
          <xsl:when test="./state"><xsl:value-of select="./state/@class"/></xsl:when>
          <xsl:when test="./ihandler">
            <xsl:choose>
              <xsl:when test="/properties/servletinfo/defaultihandlerstate">
                <xsl:value-of select="/properties/servletinfo/defaultihandlerstate/@class"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>de.schlund.pfixcore.workflow.app.DefaultIWrapperState</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="./authhandler"><xsl:text>de.schlund.pfixcore.workflow.app.DefaultAuthIWrapperState</xsl:text></xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="/properties/servletinfo/defaultstate">
                <xsl:value-of select="/properties/servletinfo/defaultstate/@class"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>de.schlund.pfixcore.workflow.app.StaticState</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#xa;</xsl:text>
        <xsl:apply-templates>
          <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/properties/servletinfo/defaultstate"/>
  <xsl:template match="/properties/servletinfo/defaultihandlerstate"/>

  <xsl:template match="finalizer">
    <xsl:param name="prefix"/>
    <xsl:value-of select="$prefix"/><xsl:text>.resdocfinalizer=</xsl:text>
    <xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="ihandler">
    <xsl:param name="prefix"/>
    <xsl:if test="@policy">
      <xsl:value-of select="$prefix"/>.ihandlercontainer.policy=<xsl:value-of select="@policy"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="./interface[@activeignore = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.ihandlercontainer.ignoreforactive=</xsl:text>
      <xsl:for-each select="./interface[@activeignore = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="./interface[@continue = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.restrictedcontinue=</xsl:text>
      <xsl:for-each select="./interface[@continue = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="./interface[@alwaysretrieve = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.alwaysretrieve=</xsl:text>
      <xsl:for-each select="./interface[@alwaysretrieve = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="./interface[@logging = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.loginterfaces=</xsl:text>
      <xsl:for-each select="./interface[@logging = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:for-each select="interface">
      <xsl:value-of select="$prefix"/>.interface.<xsl:value-of select="position()"/>.<xsl:value-of select="@prefix"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="authhandler">
    <xsl:param name="prefix"/>
    <xsl:value-of select="$prefix"/>.interface.<xsl:value-of select="authinterface/@prefix"/>=<xsl:value-of select="authinterface/@class"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:for-each select="auxinterface">
      <xsl:value-of select="$prefix"/>.auxinterface.<xsl:value-of select="position()"/>.<xsl:value-of select="@prefix"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="output">
    <xsl:param name="prefix"/>
    <xsl:for-each select="./resource">
      <xsl:value-of select="$prefix"/>.insertcr.<xsl:value-of select="@node"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="param">
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="not($prefix)">
        <xsl:value-of select="concat('pagerequest.',ancestor::pagerequest/@name,'.',@name)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$prefix"/>.<xsl:value-of select="@name"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>=</xsl:text><xsl:apply-templates>
      <xsl:with-param name="doit">yes</xsl:with-param>
    </xsl:apply-templates><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="pageflow">
    <xsl:variable name="prefix">context.pageflow.<xsl:value-of select="@name"/>.</xsl:variable>
    <xsl:variable name="flowname"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name="stopnext"><xsl:value-of select="@stopnext"/></xsl:variable>
    <xsl:for-each select="flowstep">
      <xsl:value-of select="$prefix"/><xsl:value-of select="position()"/>=<xsl:value-of select="@name"/><xsl:text>&#xa;</xsl:text>
      <xsl:if test="@stophere = 'true' or $stopnext = 'true'">
        <xsl:text>context.pageflowproperty.</xsl:text>
        <xsl:value-of select="$flowname"/>.<xsl:value-of select="@name"/>.stophere=true<xsl:text>&#xa;</xsl:text>
      </xsl:if>
      <xsl:if test="./oncontinue/@applyall = 'true'">
        <xsl:text>context.pageflowproperty.</xsl:text>
        <xsl:value-of select="$flowname"/>.<xsl:value-of select="@name"/>.oncontinue.applyall=true<xsl:text>&#xa;</xsl:text>
      </xsl:if>
      <xsl:if test="./oncontinue">
        <xsl:call-template name="render_tests">
          <xsl:with-param name="tests" select="./oncontinue/when"/>
          <xsl:with-param name="prefix">oncontinue</xsl:with-param>
          <xsl:with-param name="flow"><xsl:value-of select="$flowname"/></xsl:with-param>
          <xsl:with-param name="page"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="@final">
      <xsl:value-of select="$prefix"/>FINAL=<xsl:value-of select="@final"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="render_tests">
    <xsl:param name="prefix"/>
    <xsl:param name="page"/>
    <xsl:param name="flow"/>
    <xsl:param name="tests"/>
    <xsl:for-each select="$tests">
      <xsl:variable name="xpath"><xsl:value-of select="@test"/></xsl:variable>
      <xsl:variable name="pos" select="position()"/>
      <xsl:variable name="actionnode" select="./action[position() = 1]"/>
      <xsl:text>context.pageflowaction.</xsl:text><xsl:value-of select="$flow"/>.<xsl:value-of select="$page"/>
      <xsl:text>.</xsl:text><xsl:value-of select="$prefix"/>.<xsl:value-of select="$pos"/>.test=<xsl:value-of select="$xpath"/><xsl:text>&#xa;</xsl:text>
      <xsl:text>context.pageflowaction.</xsl:text><xsl:value-of select="$flow"/>.<xsl:value-of select="$page"/>
      <xsl:text>.</xsl:text><xsl:value-of select="$prefix"/>.<xsl:value-of select="$pos"/>.action=<xsl:value-of select="$actionnode/@type"/><xsl:text>&#xa;</xsl:text>
      <xsl:for-each select="$actionnode/@*[name() != 'type']">
        <xsl:text>context.pageflowaction.</xsl:text><xsl:value-of select="$flow"/>.<xsl:value-of select="$page"/>
        <xsl:text>.</xsl:text><xsl:value-of select="$prefix"/>.<xsl:value-of select="$pos"/>.data.<xsl:value-of select="name()"/>=<xsl:value-of select="current()"/><xsl:text>&#xa;</xsl:text>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="context">
    <xsl:text>context.class=</xsl:text>
    <xsl:choose>
      <xsl:when test="@class"><xsl:value-of select="@class"/></xsl:when>
      <xsl:otherwise>de.schlund.pfixcore.workflow.Context</xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>context.defaultpageflow=</xsl:text>
    <xsl:value-of select="@defaultflow"/><xsl:text>&#xa;</xsl:text>
    <xsl:if test="@authpage">
      <xsl:text>authcontext.authpage=</xsl:text>
      <xsl:value-of select="@authpage"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:for-each select="./resource">
      <xsl:text>context.resource.</xsl:text>
      <xsl:value-of select="format-number(position(),'00')"/>.<xsl:value-of select="./@class"/><xsl:text>=</xsl:text>
      <xsl:for-each select="./implements">
        <xsl:value-of select="./@class"/><xsl:if test="following-sibling::implements"><xsl:text>, </xsl:text></xsl:if>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
      <xsl:for-each select="./param">
        <xsl:apply-templates select=".">
          <xsl:with-param name="prefix">context.resourceparameter.<xsl:value-of select="../@class"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="servletinfo">
    <xsl:text>xmlserver.depend.xml=</xsl:text>
<!--     <xsl:choose> -->
<!--       <xsl:when test="starts-with(@depend, '/')"><xsl:value-of select="@depend"/></xsl:when> -->
<!--       <xsl:otherwise><xsl:value-of select="$docroot"/>/<xsl:value-of select="@depend"/></xsl:otherwise> -->
<!--     </xsl:choose> -->
    <xsl:value-of select="@depend"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>xmlserver.servlet.name=</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="servletinfo//ssl">
    <xsl:text>servlet.needsSSL=</xsl:text>
    <xsl:value-of select="./@force"/>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>


  <xsl:template match="pagerequest//ssl">
    <xsl:text>pagerequest.</xsl:text><xsl:value-of select="ancestor::pagerequest/@name"/><xsl:text>.needsSSL=</xsl:text>
    <xsl:value-of select="./@force"/>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="editmode">
    <xsl:text>xmlserver.noeditmodeallowed=</xsl:text>
    <xsl:choose>
      <xsl:when test="@allow = 'true'">false</xsl:when>
      <xsl:otherwise>true</xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="adminmode">
    <xsl:text>context.adminmode.watch=</xsl:text>
    <xsl:value-of select="./@watch"/><xsl:text>&#xa;</xsl:text>
    <xsl:text>context.adminmode.page=</xsl:text>
    <xsl:value-of select="./@page"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="exceptions">
    <xsl:for-each select="exception">
      <xsl:variable name="processor">
        <xsl:choose>
          <xsl:when test="@processor"><xsl:value-of select="@processor" /></xsl:when>
          <xsl:otherwise>de.schlund.pfixxml.exceptionprocessor.PageForwardingExceptionProcessor</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="translatedtype" select="translate(@type, '.', '')" />
      <xsl:text>exception.</xsl:text><xsl:value-of select="$translatedtype"/><xsl:text>.type=</xsl:text>
      <xsl:value-of select="@type"/><xsl:text>&#xa;</xsl:text>
      <xsl:text>exception.</xsl:text><xsl:value-of select="$translatedtype"/><xsl:text>.forward=</xsl:text>
      <xsl:value-of select="@forward"/><xsl:text>&#xa;</xsl:text>
      <xsl:text>exception.</xsl:text><xsl:value-of select="$translatedtype"/><xsl:text>.page=</xsl:text>
      <xsl:value-of select="@page"/><xsl:text>&#xa;</xsl:text>
      <xsl:text>exception.</xsl:text><xsl:value-of select="$translatedtype"/><xsl:text>.processor=</xsl:text>
      <xsl:value-of select="$processor"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>

<!--
Local Variables:
mode: xsl
End:
-->
