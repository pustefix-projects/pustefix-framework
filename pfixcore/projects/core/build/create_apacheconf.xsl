<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
        xmlns:jee="http://java.sun.com/xml/ns/javaee"
        xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo">

  <xsl:param name="commonprojectsfile"/>
  <xsl:param name="customizationinfo"/>
  <xsl:param name="docroot"/>
  
  <xsl:variable name="common-temp" select="document(concat('file://', $commonprojectsfile))" />
  <xsl:variable name="common">
    <xsl:apply-templates select="$common-temp" mode="customization"/>
  </xsl:variable>
  
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:template match="/">
    <xsl:variable name="tree">
      <xsl:apply-templates mode="customization" select="self::node()"/>
    </xsl:variable>
    <xsl:apply-templates select="$tree/*" mode="ports"/>
  </xsl:template>
  
  <xsl:template mode="ports" match="p:http-port|p:https-port">
    <xsl:variable name="currentprj" select="ancestor::p:project-config"></xsl:variable>
    <xsl:variable name="active">
      <xsl:choose>
        <xsl:when test="normalize-space($currentprj/enabled/text()) = 'false'">false</xsl:when>
        <xsl:otherwise>true</xsl:otherwise> 
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="address-temp">
      <xsl:apply-templates select="p:address/node()"/>
    </xsl:variable>
    <xsl:variable name="address">
      <xsl:choose>
        <xsl:when test="self::p:https-port and not(contains($address-temp, ':'))">
          <xsl:value-of select="$address-temp"/><xsl:text>:443</xsl:text>
        </xsl:when>
        <xsl:when test="not(contains($address-temp, ':'))">
          <xsl:value-of select="$address-temp"/><xsl:text>:80</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$address-temp"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$active = 'true'">
&lt;VirtualHost <xsl:value-of select="$address"/>&gt;
ServerName <xsl:apply-templates select="$currentprj/p:http-server/p:server-name/node()"/>
<xsl:if test="count($currentprj/p:http-server/p:server-alias/node()) > 0">
ServerAlias <xsl:apply-templates select="$currentprj/p:http-server/p:server-alias/node()"/>
</xsl:if>

RewriteEngine on

<xsl:if test="$currentprj/p:application/p:default-path/node()">
  RewriteRule ^/$ <xsl:choose>
    <xsl:when test="self::p:https-port">https</xsl:when><xsl:otherwise>http</xsl:otherwise>
  </xsl:choose>://%{SERVER_NAME}<xsl:apply-templates select="$currentprj/p:application/p:default-path/node()"/> [NC,R,L]
</xsl:if>

<xsl:if test="self::p:https-port">
  SSLEngine on
  <xsl:apply-templates select="p:ssl-crt"/>
  <xsl:apply-templates select="p:ssl-key"/>
</xsl:if>

&lt;IfDefine PFX_USE_JK&gt;
JkMount /xml/* <xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>
<xsl:for-each select="$currentprj/p:application/p:web-xml/jee:web-app/jee:servlet-mapping/jee:url-pattern">
JkMount <xsl:value-of select="text()"/> <xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>
</xsl:for-each>
&lt;/IfDefine&gt;

&lt;IfDefine PFX_USE_PROXY_AJP&gt;
ProxyPass /xml/ balancer://<xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>/xml/ nofailover=On stickysession=jsessionid
<xsl:for-each select="$currentprj/p:application/p:web-xml/jee:web-app/jee:servlet-mapping/jee:url-pattern">
<xsl:variable name="proxypath">
  <xsl:choose>
    <xsl:when test="substring-before(text(), '*') = ''">
      <xsl:value-of select="text()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="substring-before(text(), '*')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>
ProxyPass <xsl:value-of select="$proxypath"/> balancer://<xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/><xsl:value-of select="$proxypath"/> nofailover=On stickysession=jsessionid
</xsl:for-each>
Include <xsl:value-of select="$docroot"/>/servletconf/tomcat/ajp.conf
&lt;/IfDefine&gt;

&lt;IfDefine !PFX_USE_PROXY_AJP&gt;
&lt;IfDefine !PFX_USE_JK&gt;
&lt;IfModule proxy_ajp_module&gt;
ProxyPass /xml/ balancer://<xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>/xml/ nofailover=On stickysession=jsessionid
<xsl:for-each select="$currentprj/p:application/p:web-xml/jee:web-app/jee:servlet-mapping/jee:url-pattern">
<xsl:variable name="proxypath">
  <xsl:choose>
    <xsl:when test="substring-before(text(), '*') = ''">
      <xsl:value-of select="text()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="substring-before(text(), '*')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>
ProxyPass <xsl:value-of select="$proxypath"/> balancer://<xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/><xsl:value-of select="$proxypath"/> nofailover=On stickysession=jsessionid
</xsl:for-each>
Include <xsl:value-of select="$docroot"/>/servletconf/tomcat/ajp.conf
&lt;/IfModule&gt;
&lt;IfModule !proxy_ajp_module&gt;
JkMount /xml/* <xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>
<xsl:for-each select="$currentprj/p:application/p:web-xml/jee:web-app/jee:servlet-mapping/jee:url-pattern">
JkMount <xsl:value-of select="text()"/> <xsl:value-of select="$common/p:global-config/p:http-server/p:tomcat/p:jkmount/text()"/>
</xsl:for-each>
&lt;/IfModule&gt;
&lt;/IfDefine&gt;
&lt;/IfDefine&gt;

<xsl:apply-templates select="$currentprj/p:application/p:error-pages/p:error"/>

<xsl:apply-templates select="$currentprj/p:application/p:docroot-path"/>

<xsl:apply-templates select="$common/p:global-config/p:application/p:static/p:path" mode="common"/>

<!-- CAUTION: don't use 'select="$currentprj/passthrough"' here -->
<!-- for beeing able to reference all resources from all projects/products -->
<xsl:apply-templates select="/projects/p:project-config/p:application/p:static/p:path"/>

<xsl:apply-templates select="$currentprj/p:http-server/p:apache/p:literal/node()"/>

<xsl:if test="$common/p:global-config/p:http-server/p:apache/p:logdir">
<xsl:variable name="path">
  <xsl:choose>
    <xsl:when test="starts-with(normalize-space($common/p:global-config/p:http-server/p:apache/p:logdir/text()), 'pfixroot:')">
      <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space($common/p:global-config/p:http-server/p:apache/p:logdir/text()), 'pfixroot:')"/>
    </xsl:when>
    <xsl:when test="starts-with(normalize-space($common/p:global-config/p:http-server/p:apache/p:logdir/text()), 'file:')">
      <xsl:value-of select="substring-after(normalize-space($common/p:global-config/p:http-server/p:apache/p:logdir/text()), 'file:')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="normalize-space($common/p:global-config/p:http-server/p:apache/p:logdir/text())"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>
ErrorLog   <xsl:value-of select="$path"/>/error_log
CustomLog  <xsl:value-of select="$path"/>/access_log combined
</xsl:if>

&lt;/VirtualHost&gt;
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="p:ssl-crt">
    <xsl:if test="node()">
      <xsl:variable name="path">
        <xsl:choose>
          <xsl:when test="starts-with(normalize-space(text()), 'pfixroot:')">
            <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space(text()), 'pfixroot:')"/>
          </xsl:when>
          <xsl:when test="starts-with(normalize-space(text()), 'file:')">
            <xsl:value-of select="substring-after(normalize-space(text()), 'file:')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(text())"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      SSLCertificateFile <xsl:value-of select="$path"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="p:ssl-key">
    <xsl:if test="node()">
      <xsl:variable name="path">
        <xsl:choose>
          <xsl:when test="starts-with(normalize-space(text()), 'pfixroot:')">
            <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space(text()), 'pfixroot:')"/>
          </xsl:when>
          <xsl:when test="starts-with(normalize-space(text()), 'file:')">
            <xsl:value-of select="substring-after(normalize-space(text()), 'file:')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(text())"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      SSLCertificateKeyFile <xsl:value-of select="$path"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="p:docroot-path">
    <xsl:if test="node()">
      <xsl:variable name="path">
        <xsl:choose>
          <xsl:when test="starts-with(normalize-space(text()), 'pfixroot:')">
            <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space(text()), 'pfixroot:')"/>
          </xsl:when>
          <xsl:when test="starts-with(normalize-space(text()), 'file:')">
            <xsl:value-of select="substring-after(normalize-space(text()), 'file:')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(text())"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      DocumentRoot <xsl:value-of select="$path"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="p:error">
      ErrorDocument <xsl:value-of select="@code"/><xsl:text> </xsl:text><xsl:value-of select="text()"/>
  </xsl:template>
  
  <xsl:template match="p:path">
    <xsl:variable name="path"><xsl:value-of select="text()"/></xsl:variable>
    <xsl:if test="not(preceding::p:path[text()=$path]) and not($common/p:global-config/p:application/p:static/p:path[text()=$path])">
      Alias        /<xsl:value-of select="$path"/><xsl:text> </xsl:text><xsl:value-of select="$docroot"/>/<xsl:value-of select="$path"/>
    </xsl:if> 
  </xsl:template>
  
  <xsl:template mode="common" match="p:path">
    <xsl:variable name="path"><xsl:value-of select="text()"/></xsl:variable>
    <xsl:if test="not(preceding::p:path[text()=$path])">
      Alias        /<xsl:value-of select="$path"/><xsl:text> </xsl:text><xsl:value-of select="$docroot"/>/<xsl:value-of select="$path"/>
    </xsl:if> 
  </xsl:template>
  
  <xsl:template mode="customization" match="p:choose">
    <xsl:variable name="matches" select="p:when[ci:evaluateXPathExpression($customizationinfo,@test)]"/>
    <xsl:choose>
      <xsl:when test="count($matches)=0">
        <xsl:apply-templates select="p:otherwise/node()" mode="customization"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$matches[1]/node()" mode="customization"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="customization" match="text()">
    <xsl:value-of select="ci:replaceVariables($customizationinfo,.)"/>
  </xsl:template>
  
  <xsl:template mode="customization" match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates mode="customization"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template mode="ports" match="*">
    <xsl:apply-templates mode="ports"/>
  </xsl:template>
  
  <xsl:template mode="ports" match="text()">
    <!-- Ignore text -->
  </xsl:template>
  
</xsl:stylesheet>

