<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns="http://pustefix.sourceforge.net/properties200401"
  xmlns:cus="http://www.schlund.de/pustefix/customize"
  exclude-result-prefixes=""
  >
  
  <!-- for being able to comment out whole element trees -->
  <xsl:import href="verb.xsl"/>

  <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>
  
  
  <!--<xsl:namespace-alias stylesheet-prefix="cus" result-prefix="cus"/>-->
  <!--<xsl:namespace-alias stylesheet-prefix="" result-prefix=""/>-->
  
  <xsl:param name="roottag">
    <xsl:message terminate="yes">The name of the root element has to be specified as param roottag.</xsl:message>
  </xsl:param>
  <xsl:param name="version">1.0</xsl:param>

  <xsl:template name="copypreceding">
    <xsl:param name="idThis" select="generate-id(self::node())"/>
    <xsl:param name="followingElementSiblings" select="following-sibling::*"/>
    <xsl:copy-of select="preceding-sibling::text()[generate-id(following-sibling::*) = $idThis] | preceding-sibling::comment()[generate-id(following-sibling::*) = $idThis]"/>
  </xsl:template>
  
  <xsl:template name="copycontent">
    <xsl:param name="childElements" select="*"/>
    <xsl:copy-of select="@*"/>
    <xsl:copy-of select="text()[not($childElements)] | comment()[not($childElements)]"/>
  </xsl:template>
  
  <xsl:template name="copyfollowing">
    <xsl:param name="followingElementSiblings" select="following-sibling::*"/>
    <xsl:copy-of select="following-sibling::text()[not($followingElementSiblings)] | following-sibling::comment()[not($followingElementSiblings)]"/>
  </xsl:template>

  <!-- copies an element-node with its corresponding comment- and text-nodes -->
  <xsl:template match="*" name="copyelem">
    <!-- If param rename is given, the name of the element is replaced with its content. -->
    <!-- The name may but does not have to be a QName -->
    <xsl:param name="rename" select="name()"/>
    <xsl:param name="followingElementSiblings" select="following-sibling::*"/>
    
    <xsl:call-template name="copypreceding">
      <xsl:with-param name="followingElementSiblings" select="$followingElementSiblings"/>
    </xsl:call-template>
    <!--
    <xsl:variable name="idThis" select="generate-id(self::node())"/>
    <xsl:variable name="followingElementSiblings" select="following-sibling::*"/>
    <xsl:copy-of select="preceding-sibling::text()[generate-id(following-sibling::*) = $idThis] | preceding-sibling::comment()[generate-id(following-sibling::*) = $idThis]"/>
    -->
    
    <xsl:variable name="childElements" select="*"/>
    <xsl:choose>
      <xsl:when test="$rename">
        <xsl:element name="{$rename}">
          <xsl:call-template name="copycontent">
            <xsl:with-param name="childElements" select="$childElements"/>
          </xsl:call-template>
          <xsl:apply-templates select="*"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <!-- not longer used. instead $rename gets a default.  -->
        <!-- saxon alway does a xmlns="" if xsl:copy is used -->
        <xsl:copy>
          <xsl:call-template name="copycontent">
            <xsl:with-param name="childElements" select="$childElements"/>
          </xsl:call-template>
          <xsl:apply-templates select="*"/>
        </xsl:copy>    
      </xsl:otherwise>
    </xsl:choose>
    
    <xsl:call-template name="copyfollowing">
      <xsl:with-param name="followingElementSiblings" select="$followingElementSiblings"/>
    </xsl:call-template>
  
  </xsl:template>

  <!-- copies all element-nodes with their corresponding comment- and text-nodes WORKING old version
  <xsl:template match="*" name="copyelem">
    <xsl:variable name="idThis" select="generate-id(self::node())"/>
    <xsl:variable name="followingElementSiblings" select="following-sibling::*"/>
    <xsl:copy-of select="preceding-sibling::text()[generate-id(following-sibling::*) = $idThis] | preceding-sibling::comment()[generate-id(following-sibling::*) = $idThis]"/>
    <xsl:copy >
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="text()[not(preceding-sibling::* | following-sibling::*)] | comment()[not(preceding-sibling::* | following-sibling::*)]"/>
      <xsl:apply-templates select="*"/>
    </xsl:copy>    
    <xsl:copy-of select="following-sibling::text()[not($followingElementSiblings)] | following-sibling::comment()[not($followingElementSiblings)]"/>
  </xsl:template>
  -->
  
  <xsl:template match="/comment()">
    <xsl:text>&#10;</xsl:text>
    <xsl:copy/>
  </xsl:template>
  
  <!-- replace rootelement  properties  with a rootelement names after param roottag -->
  <xsl:template match="properties">
    <xsl:text>&#10;</xsl:text>
    
    <!--<contextxmlserver xmlns="http://pustefix.sourceforge.net/properties200401" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://pustefix.sourceforge.net/properties200401 http://pustefix.sourceforge.net/properties200401.xsd">-->
    <xsl:element name="{$roottag}" namespace="http://pustefix.sourceforge.net/properties200401">
      <!-- impossible to create an arbitrary namespace node, there copy one  -->
      <xsl:copy-of  select="/properties/namespace::cus"/>
      <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">http://pustefix.sourceforge.net/properties200401 http://pustefix.sourceforge.net/properties200401.xsd</xsl:attribute>
      <xsl:attribute name="version"><xsl:value-of select="$version"/></xsl:attribute>
      <xsl:apply-templates select="servletinfo"/>
      <xsl:apply-templates select="context | foreigncontext"/>
      <!--<xsl:apply-templates select="pageflow"/>-->
      <xsl:apply-templates select="pageflow | pagerequest | directoutputpagerequest"/>
      <!--<xsl:apply-templates select="pagerequest"/>-->
      <xsl:apply-templates select="exception"/>
      <xsl:text>&#10;&#10;  </xsl:text>
      
      <xsl:if test="prop | cus:choose[cus:test/prop]">
        <properties>
          <xsl:apply-templates select="prop | cus:choose[cus:test/prop]"/>
          <xsl:text>  </xsl:text>
        </properties>
      </xsl:if>

      <xsl:variable name="disallowedElements" select="*[not(self::servletinfo | self::context | self::foreigncontext | self::pageflow  | self::pagerequest  | self::directoutputpagerequest | self::exception  | self::prop | self::cus:choose[cus:test/prop])]"></xsl:variable>
      <xsl:if test="$disallowedElements">
        <xsl:variable name="date" select="HUHU"/>
        <xsl:text>&#xa;  </xsl:text><xsl:comment><xsl:text> // TODO_PROPCONV top-level These elements do not conform to the new format as of 2004-11, see http://pustefix.sourceforge.net/prop.html&#xa;  </xsl:text>
          <!--<xsl:apply-templates select="*[not(self::servletinfo | self::context | self::pageflow  | self::pagerequest  | self::exception  | self::prop | self::cus:choose[cus:test/prop])]" mode="verb"/>-->
          <xsl:apply-templates select="$disallowedElements" mode="verb"/>
        <xsl:text>&#xa;  </xsl:text></xsl:comment><xsl:text>&#xa;  </xsl:text>
      </xsl:if>
      <xsl:text>&#10;</xsl:text>
    </xsl:element>
    <xsl:text>&#10;</xsl:text>
    <!--</contextxmlserver>-->
  </xsl:template>

  <!-- comment out cus:choose within servletinfo -->
  <xsl:template match="servletinfo/cus:choose">
    <xsl:text>&#xa;    </xsl:text><xsl:comment><xsl:text> // TODO_PROPCONV servletinfo&#xa;    </xsl:text>
      <xsl:apply-templates select="self::node()" mode="verb"/>
    <xsl:text>&#xa;    </xsl:text></xsl:comment><xsl:text>&#xa;    </xsl:text>
  </xsl:template>

  <!-- match
    <cus:choose>
      <cus:test mode="prod">
        <editmode allow="false"/>
      </cus:test>
      <cus:test>
        <editmode allow="true"/>
      </cus:test>
    </cus:choose>
  -->   
  <xsl:template match="servletinfo/cus:choose[count(*) = 1 and cus:test[@mode = 'prod']/editmode[@allow = 'false'] and not(cus:test[@mode = 'prod']/ssl) and cus:test[not(@*)]/editmode[@allow = 'true']]">
    <xsl:text>&#xa;    </xsl:text>
    <editmode allow="false" modes="prod"/>  <xsl:text>&#xa;  </xsl:text>
  </xsl:template>  

  <!-- match
    <cus:choose>
      <cus:test mode="prod">
        <editmode allow="false"/>
        <ssl force="true"/>
      </cus:test>
      <cus:test>
        <editmode allow="true"/>
      </cus:test>
    </cus:choose>
  -->   
  <xsl:template match="servletinfo/cus:choose[count(*) = 2 and cus:test[@mode = 'prod']/editmode[@allow = 'false'] and cus:test[@mode = 'prod']/ssl[@force = 'true'] and cus:test[not(@*)]/editmode[@allow = 'true']]">
    <xsl:text>&#xa;    </xsl:text>
    <editmode allow="false" modes="prod"/>  <xsl:text>&#xa;  </xsl:text>
    <ssl force="true" modes="prod"/>        <xsl:text>&#xa;    </xsl:text>
  </xsl:template>  

  <!-- rename cus:choose to choose -->
  <xsl:template match="cus:choose[cus:test/prop|cus:test/param]">
    <xsl:call-template name="copyelem">
      <xsl:with-param name="rename">choose</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- rename cus:test to test -->
  <xsl:template match="cus:test[ancestor::cus:choose and (prop|param)]">
    <xsl:call-template name="copyelem">
      <xsl:with-param name="rename">test</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- comment out empty cus:test -->
  <xsl:template match="cus:test[ancestor::cus:choose and not(*)]">
    <xsl:text>&#xa;    </xsl:text><xsl:comment><xsl:text>&#xa;    </xsl:text>
      <xsl:apply-templates select="self::node()" mode="verb"/>
    <xsl:text>&#xa;    </xsl:text></xsl:comment><xsl:text>&#xa;    </xsl:text>
  </xsl:template>
  
  <!-- rename cus:test to test -->
  <xsl:template match="param">
    <xsl:call-template name="copyelem">
      <xsl:with-param name="rename">prop</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- place param within <properties> in pagerequests-->
  <xsl:template match="pagerequest">
    <xsl:call-template name="copypreceding"/>
    
    <pagerequest>
      <xsl:copy-of select="@*"/>
      <xsl:call-template name="copycontent"/>
      <xsl:apply-templates select="ssl"/>
      <xsl:apply-templates select="state"/>
      <xsl:apply-templates select="finalizer"/>
      <xsl:apply-templates select="ihandler"/>
      <xsl:apply-templates select="authhandler"/>
      <xsl:apply-templates select="output"/>
      <!--<xsl:apply-templates select="*[not(self::param | self::cus:choose[cus:test/param])]"/>-->
      <xsl:if test="param | cus:choose[cus:test/param]">
        <xsl:text>&#xa;</xsl:text><properties>
          <xsl:apply-templates select="param | cus:choose[cus:test/param]"></xsl:apply-templates>
        </properties><xsl:text>&#xa;</xsl:text>
      </xsl:if>    
      <xsl:variable name="disallowedElements" select="*[not(self::ssl | self::state | self::finalizer | self::authhandler | self::ihandler | self::output | self::param | self::cus:choose[cus:test/param])]"></xsl:variable>
      <xsl:if test="$disallowedElements">
        <xsl:variable name="date" select="HUHU"/>
        <xsl:text>&#xa;  </xsl:text><xsl:comment><xsl:text> // TODO_PROPCONV pagerequest These elements do not conform to the new format as of 2004-11, see http://pustefix.sourceforge.net/prop.html&#xa;  </xsl:text>
          <xsl:apply-templates select="$disallowedElements" mode="verb"/>
        <xsl:text>&#xa;  </xsl:text></xsl:comment><xsl:text>&#xa;  </xsl:text>
      </xsl:if>    
    </pagerequest>

    <xsl:call-template name="copyfollowing"/>
  </xsl:template>
  
  <!-- place param within <properties> in directoutputpagerequest-->
  <xsl:template match="directoutputpagerequest">
    <xsl:call-template name="copypreceding"/>
    
    <directoutputpagerequest>
      <xsl:copy-of select="@*"/>
      <xsl:call-template name="copycontent"/>
      <xsl:apply-templates select="directoutputstate"/>
      <!--<xsl:apply-templates select="*[not(self::param | self::cus:choose[cus:test/param])]"/>-->
      <xsl:if test="param | cus:choose[cus:test/param]">
        <xsl:text>&#xa;</xsl:text><properties>
          <xsl:apply-templates select="param | cus:choose[cus:test/param]"></xsl:apply-templates>
        </properties><xsl:text>&#xa;</xsl:text>
      </xsl:if>    
      <xsl:variable name="disallowedElements" select="*[not(self::directoutputstate | self::param | self::cus:choose[cus:test/param])]"></xsl:variable>
      <xsl:if test="$disallowedElements">
        <xsl:variable name="date" select="HUHU"/>
        <xsl:text>&#xa;  </xsl:text><xsl:comment><xsl:text> // TODO_PROPCONV pagerequest These elements do not conform to the new format as of 2004-11, see http://pustefix.sourceforge.net/prop.html&#xa;  </xsl:text>
          <xsl:apply-templates select="$disallowedElements" mode="verb"/>
        <xsl:text>&#xa;  </xsl:text></xsl:comment><xsl:text>&#xa;  </xsl:text>
      </xsl:if>    
    </directoutputpagerequest>

    <xsl:call-template name="copyfollowing"/>
  </xsl:template>
  
  <!-- place param within <properties> in resources-->
  <xsl:template match="resource">
    <xsl:call-template name="copypreceding"/>

    <resource>
      <xsl:copy-of select="@*"/>    
      <xsl:apply-templates select="*[not(self::param | self::cus:choose[cus:test/param])]"/>
      <xsl:if test="param | cus:choose[cus:test/param]">
        <xsl:text>&#xa;</xsl:text><properties>
          <xsl:apply-templates select="param | cus:choose[cus:test/param]"></xsl:apply-templates>
        </properties><xsl:text>&#xa;</xsl:text>
      </xsl:if>
    </resource>

    <xsl:call-template name="copyfollowing"/>
  </xsl:template>
  
  
  <!-- rename  -->
  <xsl:template match="servletinfo">
    <xsl:choose>
      <xsl:when test="$roottag = 'directoutputserver'">
        <xsl:call-template name="copyelem">
          <xsl:with-param name="rename">directoutputservletinfo</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="copyelem"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- rename ihandler to input -->
  <xsl:template match="ihandler">
    <xsl:call-template name="copyelem">
      <xsl:with-param name="rename">input</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- rename authhandler to auth -->
  <xsl:template match="authhandler">
    <xsl:call-template name="copyelem">
      <xsl:with-param name="rename">auth</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>
