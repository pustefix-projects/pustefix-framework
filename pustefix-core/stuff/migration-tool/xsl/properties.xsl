<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:conf="http://pustefix.sourceforge.net/properties200401"
                xmlns="http://pustefix.sourceforge.net/properties200401"
>
  
  <xsl:import href="defaultcopy.xsl"/>
  <xsl:import href="customization.xsl"/>
  
  <xsl:param name="targetNamespace">http://pustefix.sourceforge.net/properties200401</xsl:param>
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="conf:choose">
    <choose>
      <xsl:call-template name="choose"/>
    </choose>
  </xsl:template>

    <xsl:template match="/conf:contextxmlserver/conf:servletinfo/conf:editmode">
    <xsl:choose>
      <xsl:when test="./@modes">
        <choose>
          <when>
            <xsl:attribute name="test">
              <xsl:call-template name="modesToTestString">
                <xsl:with-param name="modestring" select="./@modes"/>
              </xsl:call-template>
            </xsl:attribute>
            <editmode allow="{./@allow}"/>
          </when>
          <otherwise>
            <xsl:if test="./@allow='false'">
              <editmode allow="true"/>
            </xsl:if>
            <xsl:if test="./@allow='true'">
              <editmode allow="false"/>
            </xsl:if>
          </otherwise>
        </choose>
      </xsl:when>
      <xsl:otherwise>
        <editmode allow="{./@allow}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/conf:contextxmlserver/conf:servletinfo/conf:ssl|/conf:contextxmlserver/conf:pagerequest/conf:ssl|/conf:contextxmlserver/conf:pagerequest/conf:default/conf:ssl|/conf:contextxmlserver/conf:pagerequest/conf:variant/conf:ssl">
    <xsl:choose>
      <xsl:when test="./@modes">
        <choose>
          <when>
            <xsl:attribute name="test">
              <xsl:call-template name="modesToTestString">
                <xsl:with-param name="modestring" select="./@modes"/>
              </xsl:call-template>
            </xsl:attribute>
            <ssl force="{./@force}"/>
          </when>
          <otherwise>
            <xsl:if test="./@force='false'">
              <ssl force="true"/>
            </xsl:if>
            <xsl:if test="./@force='true'">
              <ssl force="false"/>
            </xsl:if>
          </otherwise>
        </choose>
      </xsl:when>
      <xsl:otherwise>
        <ssl force="{./@force}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/conf:directoutputserver/conf:directoutputservletinfo/conf:editmode">
    <xsl:choose>
      <xsl:when test="./@modes">
        <choose>
          <when>
            <xsl:attribute name="test">
              <xsl:call-template name="modesToTestString">
                <xsl:with-param name="modestring" select="./@modes"/>
              </xsl:call-template>
            </xsl:attribute>
            <editmode allow="{./@allow}"/>
          </when>
          <otherwise>
            <xsl:if test="./@allow='false'">
              <editmode allow="true"/>
            </xsl:if>
            <xsl:if test="./@allow='true'">
              <editmode allow="false"/>
            </xsl:if>
          </otherwise>
        </choose>
      </xsl:when>
      <xsl:otherwise>
        <editmode allow="{./@allow}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/conf:directoutputserver/conf:directoutputservletinfo/conf:ssl">
    <xsl:choose>
      <xsl:when test="./@modes">
        <choose>
          <when>
            <xsl:attribute name="test">
              <xsl:call-template name="modesToTestString">
                <xsl:with-param name="modestring" select="./@modes"/>
              </xsl:call-template>
            </xsl:attribute>
            <ssl force="{./@force}"/>
          </when>
          <otherwise>
            <xsl:if test="./@force='false'">
              <ssl force="true"/>
            </xsl:if>
            <xsl:if test="./@force='true'">
              <ssl force="false"/>
            </xsl:if>
          </otherwise>
        </choose>
      </xsl:when>
      <xsl:otherwise>
        <ssl force="{./@force}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/conf:standardprops/conf:properties//conf:prop[@name='apploader.propertyfile']">
    <xsl:choose>
      <xsl:when test="text()='common/conf/apploader.prop'">
        <prop name="apploader.propertyfile">common/conf/apploader.xml</prop>
      </xsl:when>
      <xsl:otherwise>
        <prop name="apploader.propertyfile"><xsl:value-of select="text()"/></prop>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/conf:standardprops/conf:properties//conf:prop[@name='exceptionhandler.propertyfile']">
    <xsl:choose>
      <xsl:when test="text()='common/conf/exceptionhandler.prop'">
        <prop name="exceptionhandler.propertyfile">common/conf/exceptionhandler.xml</prop>
      </xsl:when>
      <xsl:otherwise>
        <prop name="exceptionhandler.propertyfile"><xsl:value-of select="text()"/></prop>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>