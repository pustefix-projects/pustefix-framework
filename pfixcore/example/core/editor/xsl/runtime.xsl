<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:template match="*" mode="static_disp">
      <xsl:param name="ind">&#160;&#160;</xsl:param>
      <xsl:param name="break">true</xsl:param>
      <xsl:param name="col">
        <xsl:choose>
          <xsl:when test="starts-with(name(),'xsl:')">#dd0000</xsl:when>
          <xsl:when test="starts-with(name(),'ixsl:')">#cc44aa</xsl:when>
          <xsl:when test="starts-with(name(),'pfx:')">#0000aa</xsl:when>
          <xsl:otherwise>#ffaa44</xsl:otherwise>
        </xsl:choose>
      </xsl:param>
      <xsl:if test="$break='false'">
        <br/>
      </xsl:if>
      <xsl:if test="(name() = 'xsl:template') or (name() = 'ixsl:template')">
        <br/></xsl:if>
      <xsl:value-of select="$ind"/>
      <font color="#ff0000">&lt;</font>
      <font><xsl:attribute name="color"><xsl:value-of select="$col"/>
        </xsl:attribute><b><xsl:value-of select="name()"/></b></font>
      <xsl:for-each select="@*">&#160;<font color="#0000ff">
          <xsl:value-of select="name()"/></font><xsl:text>="</xsl:text><font color="#22aa00">
          <xsl:value-of select="."/></font><xsl:text>"</xsl:text></xsl:for-each><font color="#ff0000">
        <xsl:if test="count(./node()) = 0">/</xsl:if>&gt;</font>
      <xsl:apply-templates mode="static_disp">
        <xsl:with-param name="ind">
          <xsl:value-of select="$ind"/>&#160;&#160;&#160;&#160;</xsl:with-param>
        <xsl:with-param name="break">false</xsl:with-param>
      </xsl:apply-templates>
      <xsl:if test="not(count(./node()) = 0)">
        <xsl:if test="count(./*) > 0">
          <br/>
          <xsl:value-of select="$ind"/>
        </xsl:if>
        <font color="#ff0000">&lt;/</font>
        <font>
          <xsl:attribute name="color"><xsl:value-of select="$col"/></xsl:attribute>
          <b><xsl:value-of select="name()"/></b></font>
        <font color="#ff0000">&gt;</font>
      </xsl:if>
    </xsl:template>

    
  
    <xsl:template match="text()" mode="static_disp">
      <xsl:value-of select="normalize-space(current())"/>
    </xsl:template>


     <xsl:template match="xmlcode">
      <div>
        <xsl:attribute name="style">
          <xsl:if test="@width">width:<xsl:value-of select="@width"/>px; </xsl:if>
          <xsl:text>background: #ffffff; border: 1px solid #AEAEAE; padding: 4px 4px 4px 4px; overflow:auto</xsl:text>
        </xsl:attribute>
        <xsl:apply-templates mode="static_disp" select="node()"/>
      </div>
    </xsl:template>
    
    <xsl:template match="comment()" mode="static_disp">
      <br/> <span style="color:#999999">&lt;!--<xsl:value-of select="."/>--&gt;</span>
    </xsl:template>

  <!--Permission stuff below --> 
 
    <xsl:template name="incl_perm_denied_usedby_other_prods_no_branch">
      <xsl:param name="prods"/>
      <xsl:call-template name="perm_denied_usedby_other_prods">
        <xsl:with-param name="type" select="'include'"/>
        <xsl:with-param name="prods" select="$prods"/>
        <xsl:with-param name="text" select="'You do not have the permission to edit includes of these products.'"/>
      </xsl:call-template>
    </xsl:template>

    <xsl:template name="incl_perm_denied_usedby_other_prods_but_branch">
      <xsl:param name="prods"/>
      <xsl:call-template name="perm_denied_usedby_other_prods">
        <xsl:with-param name="type" select="'include'"/>
        <xsl:with-param name="prods" select="$prods"/>
        <xsl:with-param name="text" select="'You do not have the permission to edit includes of these products, but you
      can create a product specific branch.'"/>
      </xsl:call-template>
    </xsl:template>
  
  
    <xsl:template name="perm_denied_usedby_other_prods">
      <xsl:param name="prods"/>
      <xsl:param name="text"/>
      <xsl:param name="type"/>
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
            This <xsl:value-of select="$type"/> is used by the following products:
          </td>
        </tr> 
        <tr>
          <td colspan="2">
            <ul>
              <xsl:copy-of select="$prods"/>
            </ul>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <xsl:value-of select="$text"/>
          </td>
        </tr>
      </table>
    </xsl:template> 
 
    <xsl:template name="image_perm_denied_usedby_other_prods">
      <xsl:param name="prods"/>
      <xsl:call-template name="perm_denied_usedby_other_prods">
        <xsl:with-param name="type" select="'image'"/>
        <xsl:with-param name="prods" select="$prods"/>
        <xsl:with-param name="text" select="'You are not allowed to edit this image.'"/>
      </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
