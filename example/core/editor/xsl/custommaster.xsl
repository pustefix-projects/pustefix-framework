<xsl:stylesheet version="1.0"
                exclude-result-prefixes="xsl cus" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

  <xsl:param name="docroot"/>
  <xsl:param name="product"/>
  <xsl:param name="lang"/>
  <xsl:param name="__target_gen"/>
  
  <!-- ******************************************
  Customize below
  ******************************************-->
  
  <xsl:template match="cus:custom_xsl">
    <!-- Add all the stuff you want at that place in the customized master sheet -->
  </xsl:template>

  <xsl:template match="cus:custom_ixsl">
    <!-- Add all the stuff you want at that place in the customized master sheet -->
    <ixsl:template match="*" mode="static_disp">
      <ixsl:param name="ind">&#160;&#160;</ixsl:param>
      <ixsl:param name="break">true</ixsl:param>
      <ixsl:param name="col">
        <ixsl:choose>
          <ixsl:when test="starts-with(name(),'xsl:')">#dd0000</ixsl:when>
          <ixsl:when test="starts-with(name(),'ixsl:')">#cc44aa</ixsl:when>
          <ixsl:when test="starts-with(name(),'pfx:')">#0000aa</ixsl:when>
          <ixsl:otherwise>#ffaa44</ixsl:otherwise>
        </ixsl:choose>
      </ixsl:param>
      <ixsl:if test="$break='false'">
        <br/>
      </ixsl:if>
      <ixsl:if test="(name() = 'xsl:template') or (name() = 'ixsl:template')">
        <br/></ixsl:if>
      <ixsl:value-of select="$ind"/>
      <font color="#ff0000">&lt;</font>
      <font><ixsl:attribute name="color"><ixsl:value-of select="$col"/>
        </ixsl:attribute><b><ixsl:value-of select="name()"/></b></font>
      <ixsl:for-each select="@*">&#160;<font color="#0000ff">
          <ixsl:value-of select="name()"/></font><ixsl:text>="</ixsl:text><font color="#22aa00">
          <ixsl:value-of select="."/></font><ixsl:text>"</ixsl:text></ixsl:for-each><font color="#ff0000">
        <ixsl:if test="count(./node()) = 0">/</ixsl:if>&gt;</font>
      <ixsl:apply-templates mode="static_disp">
        <ixsl:with-param name="ind">
          <ixsl:value-of select="$ind"/>&#160;&#160;&#160;&#160;</ixsl:with-param>
        <ixsl:with-param name="break">false</ixsl:with-param>
      </ixsl:apply-templates>
      <ixsl:if test="not(count(./node()) = 0)">
        <ixsl:if test="count(./*) > 0">
          <br/>
          <ixsl:value-of select="$ind"/>
        </ixsl:if>
        <font color="#ff0000">&lt;/</font>
        <font>
          <ixsl:attribute name="color"><ixsl:value-of select="$col"/></ixsl:attribute>
          <b><ixsl:value-of select="name()"/></b></font>
        <font color="#ff0000">&gt;</font>
      </ixsl:if>
    </ixsl:template>

    
  
    <ixsl:template match="text()" mode="static_disp">
      <ixsl:value-of select="normalize-space(current())"/>
    </ixsl:template>


     <ixsl:template match="xmlcode">
      <div style="background: #ffffff; border-style: ridge; border-width: 2px;">
        <ixsl:attribute name="style">
          <ixsl:choose>
            <ixsl:when test="@width">width:<ixsl:value-of select="@width"/>px; background: #ffffff; border-style: ridge; border-width: 2px;</ixsl:when>
            <ixsl:otherwise>background: #ffffff; border-style: ridge; border-width: 2px;</ixsl:otherwise>
          </ixsl:choose>
        </ixsl:attribute>
        <ixsl:apply-templates mode="static_disp" select="node()"/>
      </div>
    </ixsl:template>
    
    <ixsl:template match="comment()" mode="static_disp">
      <br/> <font color="#999999">&lt;!--<ixsl:value-of select="."/>--&gt;</font>
    </ixsl:template>

  <!--Permission stuff below --> 
 
    <ixsl:template name="incl_perm_denied_usedby_other_prods_no_branch">
      <ixsl:param name="prods"/>
      <ixsl:call-template name="perm_denied_usedby_other_prods">
        <ixsl:with-param name="type" select="'include'"/>
        <ixsl:with-param name="prods" select="$prods"/>
        <ixsl:with-param name="text" select="'You do not have the permission to edit includes of these products.'"/>
      </ixsl:call-template>
    </ixsl:template>

    <ixsl:template name="incl_perm_denied_usedby_other_prods_but_branch">
      <ixsl:param name="prods"/>
      <ixsl:call-template name="perm_denied_usedby_other_prods">
        <ixsl:with-param name="type" select="'include'"/>
        <ixsl:with-param name="prods" select="$prods"/>
        <ixsl:with-param name="text" select="'You do not have the permission to edit includes of these products, but you
      can create a product specific branch.'"/>
      </ixsl:call-template>
    </ixsl:template>
  
  
    <ixsl:template name="perm_denied_usedby_other_prods">
      <ixsl:param name="prods"/>
      <ixsl:param name="text"/>
      <ixsl:param name="type"/>
      <table class="core_errorbox_table" width="100%">
        <tr valign="top">
          <td class="core_errorbox_td">
            <img src="/core/img/error.gif"/>
          </td>
          <td  class="core_errorlabel_text">
            Permission denied!
          </td>
        </tr>
        <tr>
          <td colspan="2">
            This <ixsl:value-of select="$type"/> is used by the following products:
          </td>
        </tr> 
        <tr>
          <td colspan="2">
            <ul>
              <ixsl:copy-of select="$prods"/>
            </ul>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <ixsl:value-of select="$text"/>
          </td>
        </tr>
      </table>
    </ixsl:template> 
 
    <ixsl:template name="image_perm_denied_usedby_other_prods">
      <ixsl:param name="prods"/>
      <ixsl:call-template name="perm_denied_usedby_other_prods">
        <ixsl:with-param name="type" select="'image'"/>
        <ixsl:with-param name="prods" select="$prods"/>
        <ixsl:with-param name="text" select="'You are not allowed to edit this image.'"/>
      </ixsl:call-template>
    </ixsl:template>

  </xsl:template>


  <xsl:template match="cus:final-output-method">
    <ixsl:output encoding="iso-8859-1" indent="no"
                 doctype-public="-//W3C//DTD HTML 4.01//EN"
                 doctype-system="http://www.w3.org/TR/html40/strict.dtd">
      <!-- Note that the method isn't set here, but in the master styelsheet itself. Below copies the relevant code -->
      <xsl:copy-of select="./node()"/>
    </ixsl:output>
  </xsl:template>
  

  <!-- No customizeable parts below -->
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xsl:include | ixsl:include">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="href"><xsl:value-of select="concat('file://',$docroot,'/',@href)"/></xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="cus:navigation">
    <xsl:value-of select="$__target_gen"/>
  </xsl:template>

  <xsl:template match="cus:docroot">
    <xsl:value-of select="$docroot"/>
  </xsl:template>

  <xsl:template match="cus:product">
    <xsl:value-of select="$product"/>
  </xsl:template>

  <xsl:template match="cus:lang">
    <xsl:value-of select="$lang"/>
  </xsl:template>


</xsl:stylesheet>


<!--
Local Variables:
mode: xml
End:
-->
