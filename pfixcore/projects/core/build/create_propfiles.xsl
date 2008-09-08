<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:prop="http://pustefix.sourceforge.net/properties200401"
                xmlns:su="xalan://de.schlund.pfixxml.util.StringUtil"
                >

  <xsl:output method="text" encoding="UTF-8" indent="no"/>
  <xsl:include href="create_lib.xsl"/>

  <!-- =============== -->
  <!-- namespace: null -->
  <!-- =============== -->
  
  <!--- match the root node -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- the property template -->
  <xsl:template match="prop">
    <xsl:param name="key"><xsl:value-of select="./@name"/></xsl:param>
    <xsl:value-of select="$key"/><xsl:text>=</xsl:text>
    <xsl:apply-templates select="./* | ./text()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
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
          <xsl:when test="./state">
            <xsl:variable name="stateClassStr" select="./state/@class" />
            <xsl:choose>
              <xsl:when test="starts-with($stateClassStr, 'script:')">
                <xsl:text>de.schlund.pfixcore.scripting.ScriptingState>&#xa;</xsl:text>
                <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:value-of select="concat('.SCRIPTINGSTATE_SRC=', substring-after($stateClassStr, 'script:'))"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="./state/@class"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
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

  <!-- // TODO_REMOVE origianl location of match="param" -->

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

  <xsl:template match="prop:encoding">
    <xsl:text>servlet.encoding=</xsl:text>
    <xsl:value-of select="text()"/>
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
  
  
  













  <!-- =============== -->
  <!-- namespace: prop -->
  <!-- =============== -->

  <xsl:template match="prop:test"><xsl:apply-templates/></xsl:template>

  <xsl:template match="prop:choose" name="propchoose">
    <xsl:param name="in" select="prop:test"/>
    <xsl:variable name="result">
      <xsl:call-template name="proptestit">
        <xsl:with-param name="thenode" select="$in[position()=1]"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$result = 'true'"><xsl:apply-templates select="$in[position()=1]"/></xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="propchoose">
          <xsl:with-param name="in" select="$in[position() != 1]"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="proptestit">
    <xsl:param name="thenode"/>
    <xsl:choose> 
      <xsl:when test="$thenode/@bool = 'or'">
      <!-- or -->
        <xsl:if test="$thenode/@uid = $uid
                or $thenode/@machine = $machine or $thenode/@mode = $mode">true</xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- and -->
        <xsl:if test="($thenode/@uid = $uid or not($thenode/@uid))
                and ($thenode/@machine = $machine or not($thenode/@machine))
                and ($thenode/@mode = $mode or not($thenode/@mode))">true</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  <xsl:template match="prop:/">
    <xsl:apply-templates/>
  </xsl:template>
  -->
  
   <xsl:template match="prop:properties">
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="$prefix">
        <xsl:apply-templates>
          <xsl:with-param name="prefix" select="$prefix"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="prop:propertiesDELME">
    <xsl:param name="key"/>
    <xsl:param name="prefix"/>
    <xsl:apply-templates></xsl:apply-templates>
  </xsl:template>

  <!-- the property template -->
  <!-- [prefix.]key=value -->
  <xsl:template match="prop:prop">
    <xsl:param name="prefix">
      <xsl:if test="ancestor::prop:pagerequest">
        <xsl:choose>
          <xsl:when test="ancestor::prop:variant">
            <xsl:value-of select="concat('pagerequest.',ancestor::prop:pagerequest/@name,'::',ancestor::prop:variant/@name)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('pagerequest.',ancestor::prop:pagerequest/@name)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:param>
    <xsl:param name="key"><xsl:value-of select="./@name"/></xsl:param>
    <xsl:value-of select="$prefix"/><xsl:if test="string-length($prefix) > 0">.</xsl:if><xsl:value-of select="$key"/><xsl:text>=</xsl:text>
    <xsl:apply-templates  select="* | text()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- replaced by match="prop:prop"
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
 -->
  
  <!-- match text nodes but just if it contains a property value -->
  <!--
  <xsl:template match="text()">
    <xsl:param name="doit"/>
    <xsl:if test="$doit">
      <xsl:value-of select="translate(normalize-space(.), '&#xa;&#xd;', '  ')"/>
    </xsl:if>
  </xsl:template>
  -->
  <xsl:template match="prop:foreigncontext">
    <xsl:text>foreigncontextservlet.foreignservletname=</xsl:text>
    <xsl:value-of select="@externalservletname"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="prop:directoutputpagerequest">
    <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.classname=</xsl:text>
    <xsl:value-of select="./prop:directoutputstate/@class"/><xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates select="./prop:properties">
      <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="prop:pagerequest">
    <xsl:param name="name" select="@name"/>
    <xsl:choose>
      <xsl:when test="@copyfrom">
        <xsl:variable name="copyfrom_name" select="@copyfrom"/>
        <xsl:apply-templates select="//prop:pagerequest[@name = $copyfrom_name]">
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
        <xsl:choose>
          <xsl:when test="not(./prop:default)">
            <xsl:if test="$nostore = 'true'">
              <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.nostore=true&#xa;</xsl:text>
            </xsl:if>
            <xsl:call-template name="gen_pagerequest">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="nodes" select="."/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="$nostore = 'true'">
              <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.nostore=true&#xa;</xsl:text>
            </xsl:if>
            <xsl:call-template name="gen_pagerequest">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="nodes" select="./prop:default"/>
            </xsl:call-template>
            <xsl:for-each select="./prop:variant">
              <xsl:if test="$nostore = 'true'">
                <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/>\:\:<xsl:value-of select="su:replaceAll(./@name, ':', '\\:')"/><xsl:text>.nostore=true&#xa;</xsl:text>
              </xsl:if>
              <xsl:call-template name="gen_pagerequest">
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="nodes" select="current()"/>
                <xsl:with-param name="variant"><xsl:value-of select="./@name"/></xsl:with-param>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="gen_pagerequest">
    <xsl:param name="name"/>
    <xsl:param name="nodes"/>
    <xsl:param name="variant"/>
    <!--     <xsl:message>** GEN: <xsl:value-of select="$name"/>::<xsl:value-of select="$variant"/> ** </xsl:message> -->
    <!--     <xsl:message><xsl:for-each select="$nodes/*"><xsl:value-of select="name()"/>-</xsl:for-each></xsl:message> -->
    <xsl:choose>
      <xsl:when test="$variant">
        <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/>\:\:<xsl:value-of select="su:replaceAll($variant, ':', '\\:')"/><xsl:text>.classname=</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.classname=</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$nodes/prop:state">
        <xsl:variable name="stateClassStr" select="$nodes/prop:state/@class" />
        <xsl:choose>
          <xsl:when test="starts-with($stateClassStr, 'script:')">
            <xsl:text>de.schlund.pfixcore.scripting.ScriptingState&#xa;</xsl:text>
            <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:value-of select="concat('.SCRIPTINGSTATE_SRC_PATH=', substring-after($stateClassStr, 'script:'))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$nodes/prop:state/@class"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$nodes/prop:input">
        <xsl:choose>
          <xsl:when test="//prop:servletinfo/prop:defaultihandlerstate">
            <xsl:value-of select="//prop:servletinfo/prop:defaultihandlerstate/@class"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>de.schlund.pfixcore.workflow.app.DefaultIWrapperState</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$nodes/prop:auth"><xsl:text>de.schlund.pfixcore.workflow.app.DefaultAuthIWrapperState</xsl:text></xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="//prop:servletinfo/prop:defaultstate">
            <xsl:value-of select="//prop:servletinfo/prop:defaultstate/@class"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>de.schlund.pfixcore.workflow.app.StaticState</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
    <xsl:choose>
      <xsl:when test="$variant">
        <xsl:apply-templates select="$nodes/*">
          <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/>\:\:<xsl:value-of select="su:replaceAll($variant, ':', '\\:')"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$nodes/*">
          <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="prop:servletinfo/prop:defaultstate | prop:directoutputservletinfo/prop:defaultstate"/>
  <xsl:template match="prop:servletinfo/prop:defaultihandlerstate | prop:directoutputservletinfo/prop:defaultihandlerstate"/>

  <xsl:template match="prop:finalizer">
    <xsl:param name="prefix"/>
    <xsl:value-of select="$prefix"/><xsl:text>.resdocfinalizer=</xsl:text>
    <xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="prop:input">
    <xsl:param name="prefix"/>
    <xsl:if test="@policy">
      <xsl:value-of select="$prefix"/>.ihandlercontainer.policy=<xsl:value-of select="@policy"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="prop:interface[@activeignore = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.ihandlercontainer.ignoreforactive=</xsl:text>
      <xsl:for-each select="prop:interface[@activeignore = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="prop:interface[@continue = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.restrictedcontinue=</xsl:text>
      <xsl:for-each select="prop:interface[@continue = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="prop:interface[@alwaysretrieve = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.alwaysretrieve=</xsl:text>
      <xsl:for-each select="prop:interface[@alwaysretrieve = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:if test="prop:interface[@logging = 'true']">
      <xsl:value-of select="$prefix"/><xsl:text>.loginterfaces=</xsl:text>
      <xsl:for-each select="prop:interface[@logging = 'true']">
        <xsl:value-of select="@prefix"/><xsl:text> </xsl:text>
      </xsl:for-each><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:for-each select="prop:interface">
      <xsl:value-of select="$prefix"/>.interface.<xsl:value-of select="position()"/>.<xsl:value-of select="@prefix"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="prop:auth">
    <xsl:param name="prefix"/>
    <xsl:value-of select="$prefix"/>.interface.<xsl:value-of select="prop:authinterface/@prefix"/>=<xsl:value-of select="prop:authinterface/@class"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:for-each select="prop:auxinterface">
      <xsl:value-of select="$prefix"/>.auxinterface.<xsl:value-of select="position()"/>.<xsl:value-of select="@prefix"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="prop:output">
    <xsl:param name="prefix"/>
    <xsl:for-each select="prop:resource">
      <xsl:value-of select="$prefix"/>.insertcr.<xsl:value-of select="@node"/>
      <xsl:text>=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <!-- // TODO_REMOVE original location of match="param" ( prop: ) -->
  
  <xsl:template match="prop:pageflow">
    <xsl:choose>
      <xsl:when test="not(./prop:default)">
        <xsl:call-template name="gen_pageflow">
          <xsl:with-param name="prefix_flow">context.pageflow.<xsl:value-of select="@name"/>.</xsl:with-param>
          <xsl:with-param name="prefix_prop">context.pageflowproperty.<xsl:value-of select="@name"/>.</xsl:with-param>
          <xsl:with-param name="stopnext"><xsl:value-of select="@stopnext"/></xsl:with-param>
          <xsl:with-param name="flowname"><xsl:value-of select="@name"/></xsl:with-param>
          <xsl:with-param name="final"><xsl:value-of select="@final"/></xsl:with-param>
          <xsl:with-param name="nodes" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="gen_pageflow">
          <xsl:with-param name="prefix_flow">context.pageflow.<xsl:value-of select="@name"/>.</xsl:with-param>
          <xsl:with-param name="prefix_prop">context.pageflowproperty.<xsl:value-of select="@name"/>.</xsl:with-param>
          <xsl:with-param name="stopnext"><xsl:value-of select="@stopnext"/></xsl:with-param>
          <xsl:with-param name="flowname"><xsl:value-of select="@name"/></xsl:with-param>
          <xsl:with-param name="final"><xsl:value-of select="@final"/></xsl:with-param>
          <xsl:with-param name="nodes" select="./prop:default"/>
        </xsl:call-template>
        <xsl:for-each select="prop:variant">
          <xsl:call-template name="gen_pageflow">
            <xsl:with-param name="prefix_flow">context.pageflow.<xsl:value-of select="../@name"/>\:\:<xsl:value-of select="su:replaceAll(current()/@name, ':', '\\:')"/>.</xsl:with-param>
            <xsl:with-param name="prefix_prop">context.pageflowproperty.<xsl:value-of select="../@name"/>\:\:<xsl:value-of select="su:replaceAll(current()/@name, ':', '\\:')"/>.</xsl:with-param>
            <xsl:with-param name="stopnext"><xsl:value-of select="../@stopnext"/></xsl:with-param>
            <xsl:with-param name="flowname"><xsl:value-of select="../@name"/>\:\:<xsl:value-of select="su:replaceAll(current()/@name, ':', '\\:')"/></xsl:with-param>
            <xsl:with-param name="final"><xsl:value-of select="../@final"/></xsl:with-param>
            <xsl:with-param name="nodes" select="current()"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="gen_pageflow">  
    <xsl:param name="flowname"/>
    <xsl:param name="prefix_flow"/>
    <xsl:param name="prefix_prop"/>
    <xsl:param name="stopnext"/>
    <xsl:param name="final"/>
    <xsl:param name="nodes"/>
    <!-- <xsl:message><xsl:value-of select="concat($flowname, '|', $prefix_flow, '|', $prefix_prop, '|', $stopnext)"/></xsl:message>-->
    <xsl:for-each select="$nodes/prop:flowstep">
      <xsl:value-of select="$prefix_flow"/><xsl:value-of select="position()"/>=<xsl:value-of select="@name"/><xsl:text>&#xa;</xsl:text>
      <xsl:if test="@stophere = 'true' or $stopnext = 'true'">
        <xsl:value-of select="$prefix_prop"/><xsl:value-of select="@name"/>.stophere=true<xsl:text>&#xa;</xsl:text>
      </xsl:if>
      <xsl:if test="prop:oncontinue/@applyall = 'true'">
        <xsl:value-of select="$prefix_prop"/><xsl:value-of select="@name"/>.oncontinue.applyall=true<xsl:text>&#xa;</xsl:text>
      </xsl:if>
      <xsl:if test="prop:oncontinue">
        <xsl:call-template name="prop:render_tests">
          <xsl:with-param name="tests" select="prop:oncontinue/prop:when"/>
          <xsl:with-param name="prefix">oncontinue</xsl:with-param>
          <xsl:with-param name="flow"><xsl:value-of select="$flowname"/></xsl:with-param>
          <xsl:with-param name="page"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not($final = '')">
      <xsl:value-of select="$prefix_flow"/>FINAL=<xsl:value-of select="$final"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="prop:render_tests">
    <xsl:param name="prefix"/>
    <xsl:param name="page"/>
    <xsl:param name="flow"/>
    <xsl:param name="tests"/>
    <xsl:for-each select="$tests">
      <xsl:variable name="xpath"><xsl:value-of select="@test"/></xsl:variable>
      <xsl:variable name="pos" select="position()"/>
      <xsl:variable name="actionnode" select="prop:action[position() = 1]"/>
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

  <xsl:template match="prop:context">
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
    <xsl:for-each select="./prop:resource">
      <xsl:text>context.resource.</xsl:text>
      <xsl:value-of select="format-number(position(),'000')"/>.<xsl:value-of select="./@class"/><xsl:text>=</xsl:text>
      <xsl:for-each select="./prop:implements">
        <xsl:value-of select="./@class"/><xsl:if test="following-sibling::prop:implements"><xsl:text>, </xsl:text></xsl:if>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
      <!-- OLD
      <xsl:for-each select="./param">
        <xsl:apply-templates select=".">
          <xsl:with-param name="prefix">context.resourceparameter.<xsl:value-of select="../@class"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
      -->
      <xsl:apply-templates select="prop:properties">
        <xsl:with-param name="prefix">context.resourceparameter.<xsl:value-of select="@class"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="prop:interceptors">
      <xsl:for-each select="prop:start/prop:interceptor">
		<xsl:text>context.startinterceptor.</xsl:text>
        <xsl:value-of select="format-number(position(),'000')"/>=<xsl:value-of select="@class"/>
        <xsl:text>&#xa;</xsl:text>
      </xsl:for-each>
      <xsl:for-each select="prop:end/prop:interceptor">
		<xsl:text>context.endinterceptor.</xsl:text>
        <xsl:value-of select="format-number(position(),'000')"/>=<xsl:value-of select="@class"/>
        <xsl:text>&#xa;</xsl:text>
      </xsl:for-each>
  </xsl:template>
  
    
  <xsl:template match="prop:scriptedflow">
    <xsl:text>scriptedflow.</xsl:text><xsl:value-of select="@name"/>=<xsl:value-of select="@file"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="prop:servletinfo | prop:directoutputservletinfo">
    <xsl:text>xmlserver.depend.xml=</xsl:text>
    <xsl:value-of select="@depend"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>xmlserver.servlet.name=</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="prop:servletinfo/prop:ssl | prop:directoutputservletinfo/prop:ssl">
    <xsl:variable name="applicable"><xsl:call-template name="prop:modeApplicable"></xsl:call-template></xsl:variable>
    <xsl:text>servlet.needsSSL=</xsl:text>
    <xsl:choose>
      <xsl:when test="$applicable = 'true'">
        <xsl:value-of select="@force"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="not(@force)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="prop:pagerequest//prop:ssl">
    <xsl:variable name="applicable"><xsl:call-template name="prop:modeApplicable"></xsl:call-template></xsl:variable>
    <xsl:choose>
      <xsl:when test="ancestor::prop:variant">
        <xsl:text>pagerequest.</xsl:text><xsl:value-of select="ancestor::prop:pagerequest/@name"/>\:\:<xsl:value-of select="su:replaceAll(ancestor::prop:variant/@name, ':', '\\:')"/><xsl:text>.needsSSL=</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>pagerequest.</xsl:text><xsl:value-of select="ancestor::prop:pagerequest/@name"/><xsl:text>.needsSSL=</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$applicable = 'true'">
        <xsl:value-of select="@force"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="not(@force)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>
  
  <xsl:template name="prop:modeApplicable">
    <xsl:param name="remainingModes" select="concat(normalize-space(@modes), ' ')"/>
    <xsl:choose>
      <xsl:when test="@modes">
        <xsl:variable name="m"><xsl:value-of select="substring-before($remainingModes, ' ')"/></xsl:variable>
        <xsl:choose>
          <xsl:when test="$m = $mode">
            <xsl:text>true</xsl:text>
          </xsl:when>
          <xsl:when test="$m = ''">
            <xsl:text>false</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="prop:modeApplicable">
              <xsl:with-param name="remainingModes"><xsl:value-of select="substring-after($remainingModes, ' ')"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>  
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>true</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  


  <xsl:template match="prop:editmode">
    <xsl:variable name="applicable"><xsl:call-template name="prop:modeApplicable"></xsl:call-template></xsl:variable>
    <xsl:text>xmlserver.noeditmodeallowed=</xsl:text>
    <xsl:choose>
      <xsl:when test="$applicable = 'true'">
        <xsl:choose>
          <xsl:when test="@allow = 'true'">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@allow = 'true'">true</xsl:when>
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="prop:adminmode">
    <xsl:text>context.adminmode.watch=</xsl:text>
    <xsl:value-of select="./@watch"/><xsl:text>&#xa;</xsl:text>
    <xsl:text>context.adminmode.page=</xsl:text>
    <xsl:value-of select="./@page"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!--
  <xsl:template match="prop:exceptions">
    <xsl:for-each select="prop:exception">
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
  -->
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xsl
End


  <xsl:template match="prop:editmode">
    <xsl:variable name="applicable"><xsl:call-template name="prop:modeApplicable"></xsl:call-template></xsl:variable>
    <xsl:text>xmlserver.noeditmodeallowed=</xsl:text>
    <xsl:choose>
      <xsl:when test="$applicable = 'true'">
        <xsl:choose>
          <xsl:when test="@allow = 'true'">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@allow = 'true'">true</xsl:when>
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>
:
-->
