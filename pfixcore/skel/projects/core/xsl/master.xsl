<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:cus="http://www.schlund.de/pustefix/customize" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>

  
  <xsl:include href="core/xsl/default_copy.xsl"/>
  
  
  <xsl:param name="docroot"><cus:docroot/></xsl:param>
  <xsl:param name="lang"><cus:lang/></xsl:param>
  <xsl:param name="product"><cus:product/></xsl:param>
  <xsl:include href="core/xsl/include.xsl"/>
  
  
  <xsl:param name="navigation"><cus:navigation/></xsl:param>
  <xsl:param name="page"/>
  <xsl:param name="navitree" select="document(concat('file://',$navigation))/make/navigation"/>

  <xsl:include href="core/xsl/navigation.xsl"/>

  
  <xsl:include href="core/xsl/utils.xsl"/>

  
  <xsl:include href="core/xsl/forminput.xsl"/>

  <cus:custom_xsl/>

  <xsl:key name="frameset_key" match="pfx:frameset" use="'fset'"/>
  <xsl:key name="frame_key" match="pfx:frame" use="'frame'"/>

  <xsl:template match="/">
    <ixsl:stylesheet version="1.0">
      <cus:final-output-method/>

      
      
      <ixsl:param name="__uri"/>
      
      
      
      <ixsl:param name="__servletpath"/>

      
      <ixsl:param name="__frame">_top</ixsl:param>

      <ixsl:param name="__reusestamp">-1</ixsl:param>
      
      
      <ixsl:param name="docroot"><xsl:value-of select="$docroot"/></ixsl:param>
      <ixsl:param name="lang"><xsl:value-of select="$lang"/></ixsl:param>
      <ixsl:param name="product"><xsl:value-of select="$product"/></ixsl:param>
      <ixsl:include href="core/xsl/include.xsl"/>

      
      <ixsl:include href="core/xsl/default_copy.xsl"/>

      <cus:custom_ixsl/>

      <ixsl:template match="/">
        <xsl:choose>
          
          <xsl:when test="key('frameset_key','fset')">
            <ixsl:choose>
              <ixsl:when test="$__frame = '_top'"> 
                <html>
                  <xsl:apply-templates select="/pfx:document/node()"/>
                </html>
              </ixsl:when>
              <xsl:for-each select="key('frame_key','frame')"> 
                <xsl:choose>
                  <xsl:when test="not(./pfx:frameset)">
                    <ixsl:when test="$__frame = '{./@name}'">
                      <xsl:apply-templates select="./node()"/>
                    </ixsl:when>
                  </xsl:when>
                  <xsl:otherwise>
                    <ixsl:when test="$__frame = '{./@name}'">
                      <html>
                        <head/>
			<xsl:apply-templates select="./pfx:frameset"/>
                      </html>
                    </ixsl:when>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </ixsl:choose>
          </xsl:when>
          <xsl:otherwise> 
            <xsl:apply-templates select="/pfx:document/node()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:template>
    </ixsl:stylesheet>
  </xsl:template>

  
  <xsl:template match="pfx:rem">
   
  </xsl:template>

  <xsl:template match="pfx:head">
    
    <head>
      <script language="JavaScript" src="/core/script/baselib.js" type="text/javascript"/>
      <xsl:apply-templates select="./node()"/>
    </head>
  </xsl:template>
  
  <xsl:template match="pfx:script">
     
    <script>
      <xsl:attribute name="language">JavaScript</xsl:attribute>
      <xsl:copy-of select="@*"/>
      <ixsl:comment>
	<xsl:copy-of select="./node()"/>
	//</ixsl:comment>
    </script>
  </xsl:template>
  
  <xsl:template match="pfx:frameset">
  

    <frameset frameborder="0" framespacing="0" border="0">
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates select="./pfx:frameset | ./pfx:frame"/>
    </frameset>
  </xsl:template>
  
  <xsl:template match="pfx:frame">
    
    
    <frame scrolling="auto" marginwidth="1" marginheight="1">
      <xsl:copy-of select="./@*[name()!='noresize']"/>
      <xsl:if test="@noresize!='false'">
	<xsl:attribute name="noresize">1</xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="not(./pfx:frameset) and .//pfx:external">
	  <xsl:choose>
	    <xsl:when test=".//pfx:external[position()=1]/@src">
	      <xsl:copy-of select=".//pfx:external[position()=1]/@src"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:copy-of select=".//pfx:external[position()=1]/node()"/>
	    </xsl:otherwise>
	  </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <ixsl:attribute name="src">
            <ixsl:value-of select="$__uri"/>?__frame=<xsl:value-of select="@name"/>&amp;__reuse=<ixsl:value-of select="$__reusestamp"/>
            <ixsl:if test="/formresult/frameanchor[@frame = '{@name}']">#<ixsl:value-of select="/formresult/frameanchor[@frame = '{@name}']/@anchor"/></ixsl:if>
          </ixsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </frame>
  </xsl:template>
  
</xsl:stylesheet>