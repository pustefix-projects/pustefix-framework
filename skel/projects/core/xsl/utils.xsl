<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" xmlns:cus="http://www.schlund.de/pustefix/customize" version="1.0">

  <xsl:template match="pfx:on">
    
  </xsl:template>
  
  <xsl:template match="pfx:off">
    
  </xsl:template>

  
  <xsl:template match="pfx:maincontent">
   
          
    <xsl:variable name="path">
      <xsl:value-of select="@path"/>
    </xsl:variable>
    <xsl:variable name="prefix">
      <xsl:choose>
        <xsl:when test="@prefix">
          <xsl:value-of select="@prefix"/>
        </xsl:when>
        <xsl:otherwise>main_</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="postfix">
      <xsl:choose>
        <xsl:when test="@postfix">
          <xsl:value-of select="@postfix"/>
        </xsl:when>
        <xsl:otherwise>.xml</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="part">
      <xsl:choose>
        <xsl:when test="@part">
          <xsl:value-of select="@part"/>
        </xsl:when>
        <xsl:otherwise>content</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="pfx:include">
      <xsl:with-param name="href"><xsl:value-of select="$path"/>/<xsl:value-of select="$prefix"/><xsl:value-of select="$page"/><xsl:value-of select="$postfix"/></xsl:with-param>
      <xsl:with-param name="part" select="$part"/>
       
      <xsl:with-param name="parent_path"/>
      <xsl:with-param name="parent_part"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:progressbar">
  

    <div id="core_progress_back"/>
    <div id="core_progress_wait">
      <center><span align="center" id="core_progress_waittext"><xsl:apply-templates/></span></center>
      <div id="core_progress_frame">
        <div id="core_progress_bar">
        </div>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template match="pfx:blank"> 
   
    <img src="/core/img/blank.gif" width="1" height="1" border="0" alt="">
      <xsl:copy-of select="@*"/>
    </img>
  </xsl:template>

  <xsl:template match="pfx:layer">
    
      <xsl:param name="name" select="concat($page, '_', @name)"/>
       <div id="{$name}">
         <xsl:copy-of select="@style"/>
         <xsl:copy-of select="@class"/>
         <xsl:apply-templates/>
       </div>
       <script>
         var layer = top.__js_getLayer("<xsl:value-of select="$name"/>");
         layer.init("<xsl:value-of select="@visible"/>",window,"<xsl:value-of select="@store"/>");
         <xsl:if test="@pos-left">
           layer.moveLeft(<xsl:value-of select="@pos-left"/>);
         </xsl:if>
         <xsl:if test="@pos-top">
           layer.moveTop(<xsl:value-of select="@pos-top"/>);
         </xsl:if>
         <xsl:if test="@pos-right">
           layer.moveRight(<xsl:value-of select="@pos-right"/>);
         </xsl:if>
         <xsl:if test="@pos-bottom">
           layer.moveBottom(<xsl:value-of select="@pos-bottom"/>);
         </xsl:if>
     </script>
  </xsl:template>
  
  <xsl:template match="pfx:switch">

      

    <xsl:param name="class">
      <xsl:choose>
        <xsl:when test="not(@class = '')"><xsl:value-of select="@class"/></xsl:when>
        <xsl:otherwise>core_button_normal</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <a class="{$class}" href="javascript://">
      <xsl:attribute name="onclick">top.__js_toggleLayer(<xsl:for-each select=".//pfx:layer_ref">
          '<xsl:value-of select="concat($page,'_',@name)"/>'
          <xsl:if test="count(following-sibling::pfx:layer_ref) &gt; 0">,</xsl:if>
        </xsl:for-each>); return false;</xsl:attribute>      
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="pfx:layer_check">

    
    
    <xsl:variable name="name"><xsl:value-of select="concat($page,'_',@name)"/></xsl:variable>
    <xsl:variable name="unique"><xsl:value-of select="generate-id(.)"/></xsl:variable>
    <span>
      <xsl:attribute name="id">SWITCH_ON_<xsl:value-of select="$unique"/></xsl:attribute>
      <xsl:apply-templates select="./pfx:on/node()"/>
    </span>
    <span>
      <xsl:attribute name="id">SWITCH_OFF_<xsl:value-of select="$unique"/></xsl:attribute>
      <xsl:apply-templates select="./pfx:off/node()"/>
    </span>
    <script>
      top.__js_registerSwitchOn("<xsl:value-of select="$name"/>", window.document.getElementById("SWITCH_ON_<xsl:value-of select="$unique"/>"));
      top.__js_registerSwitchOff("<xsl:value-of select="$name"/>",window.document.getElementById("SWITCH_OFF_<xsl:value-of select="$unique"/>"));
    </script>
  </xsl:template>
  
  <xsl:template match="pfx:layer_ref">
    
    <xsl:param name="parent">
      <xsl:choose>
        <xsl:when test="count(ancestor::pfx:layer) &gt; 0">
          <xsl:value-of select="ancestor::pfx:layer[position() = 1]/@name"/>
        </xsl:when>
        <xsl:otherwise>__ROOT_LAYER__</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="child" select="concat($page, '_', @name)"/>
    <script>
      top.__js_registerLayerChild("<xsl:value-of select="concat($page, '_', $parent)"/>","<xsl:value-of select="$child"/>");
    </script>
  </xsl:template>
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
</xsl:stylesheet>