<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<!--  <xsl:param name="docroot"/>
  <xsl:param name="uid"/>
  <xsl:param name="machine"/>
  <xsl:param name="fqdn"/>
  <xsl:param name="mode"/>-->

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
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <!-- match text nodes but just if it contains a property value -->
  <xsl:template match="text()">
    <xsl:param name="doit"/>
    <xsl:if test="$doit"> 
      <xsl:value-of select="translate(normalize-space(.), '&#xa;&#xd;', '  ')"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="pagerequest">
    <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:text>pagerequest.</xsl:text><xsl:value-of select="$name"/><xsl:text>.classname=</xsl:text>
    <xsl:choose>
      <xsl:when test="./state"><xsl:value-of select="./state/@class"/></xsl:when>
      <xsl:when test="./ihandler"><xsl:text>de.schlund.pfixcore.workflow.app.DefaultIWrapperState</xsl:text></xsl:when>
      <xsl:when test="./authhandler"><xsl:text>de.schlund.pfixcore.workflow.app.DefaultAuthIWrapperState</xsl:text></xsl:when>
      <xsl:otherwise><xsl:text>de.schlund.pfixcore.workflow.app.StaticState</xsl:text></xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates>
      <xsl:with-param name="prefix">pagerequest.<xsl:value-of select="$name"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

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
      <xsl:value-of select="$prefix"/><xsl:text>.ignoreforactive=</xsl:text>
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
    <xsl:value-of select="$prefix"/>.<xsl:value-of select="@name"/>
    <xsl:text>=</xsl:text><xsl:apply-templates>
      <xsl:with-param name="doit">yes</xsl:with-param>
    </xsl:apply-templates><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="pageflow">
    <xsl:variable name="prefix">context.pageflow.<xsl:value-of select="@name"/>.</xsl:variable>
    <xsl:for-each select="flowstep">
      <xsl:value-of select="$prefix"/><xsl:value-of select="position()"/>=<xsl:value-of select="@name"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:if test="@final">
      <xsl:value-of select="$prefix"/>FINAL=<xsl:value-of select="@final"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="context">
    <xsl:text>context.class=</xsl:text>      
    <xsl:choose>
      <xsl:when test="@class"><xsl:value-of select="@class"/></xsl:when>
      <xsl:when test="@authpage">de.schlund.pfixcore.workflow.AuthContext</xsl:when>
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
      <xsl:value-of select="position()"/>.<xsl:value-of select="./@class"/><xsl:text>=</xsl:text>
      <xsl:for-each select="./implements">
        <xsl:value-of select="./@class"/><xsl:text> </xsl:text>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="servletinfo">
    <xsl:if test="./sslneeded/node()">
      <xsl:text>servlet.needsSSL=</xsl:text>
      <xsl:apply-templates select="./sslneeded/node()">
        <xsl:with-param name="doit" select="'yes'"/>
      </xsl:apply-templates>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:text>xmlserver.depend.xml=</xsl:text>
    <xsl:apply-templates select="./depend/node()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>xmlserver.servlet.name=</xsl:text>
    <xsl:apply-templates select="./servletname/node()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>xmlserver.noeditmodeallowed=</xsl:text>
    <xsl:apply-templates select="./prohibitedit/node()">
      <xsl:with-param name="doit" select="'yes'"/>
    </xsl:apply-templates>
    <xsl:text>&#xa;</xsl:text>
    <xsl:if test="./adminmode">
      <xsl:text>context.adminmode.watch=</xsl:text>
      <xsl:value-of select="./adminmode/@watch"/><xsl:text>&#xa;</xsl:text>
      <xsl:text>context.adminmode.page=</xsl:text>
      <xsl:value-of select="./adminmode/@page"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
