<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" xmlns:cus="http://www.schlund.de/pustefix/customize" version="1.0">
  
  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>

  <xsl:template match="pfx:label" name="pfx:label">
      
    <xsl:param name="name" select="@name"/>
    <xsl:param name="content">
      <xsl:apply-templates/>
    </xsl:param>
    <ixsl:choose>
      <ixsl:when test="/formresult/formerrors/error[@name='{$name}']">
        <xsl:call-template name="pfx:include">
          <xsl:with-param name="href">common/txt/general.xml</xsl:with-param>
          <xsl:with-param name="part">error_icon</xsl:with-param>
        </xsl:call-template>
        <xsl:if test="not(@type = 'hidden')">
          <span class="core_label_error"><xsl:apply-templates/></span>
        </xsl:if>
      </ixsl:when>
      <xsl:if test="not(@type = 'hidden')">
        <ixsl:otherwise>
          <xsl:apply-templates/>
        </ixsl:otherwise>
      </xsl:if>
    </ixsl:choose>
  </xsl:template>

  <xsl:template match="pfx:paramref"/>
  
  <xsl:template match="pfx:errorbox">
     

    
    <xsl:param name="paramref" select="./pfx:paramref"/>
    <ixsl:if test="/formresult/formerrors/node()">
      <table class="core_errorbox_table">
        <xsl:if test="@width"><xsl:copy-of select="@width"/></xsl:if>
	<xsl:choose>
	  <xsl:when test="not($paramref)">
	    <xsl:for-each select="//pfx:label">
	      <xsl:variable name="name" select="./@name"/>
	      <xsl:variable name="label" select="./node()"/>
	      <ixsl:if test="/formresult/formerrors/error[@name='{$name}']">
		<tr>
		  <td nowrap="nowrap" class="core_errorlabel_td" valign="top">
		    <span class="core_errorlabel_text"><xsl:apply-templates select="$label"/></span>
		  </td>
		  <td class="core_errortext_td">
		    <span class="core_errortext">
		      <ixsl:apply-templates select="/formresult/formerrors/error[@name='{$name}']"/>
		    </span>
		  </td>
		</tr>
	      </ixsl:if>
	    </xsl:for-each>
	  </xsl:when>
	  <xsl:otherwise>
            <xsl:for-each select="$paramref">
              <xsl:variable name="labelname"><xsl:value-of select="./@name"/></xsl:variable>
              <xsl:variable name="label" select="//pfx:label[@name = $labelname]"/>
              <ixsl:if test="/formresult/formerrors/error[@name='{$labelname}']">
                <tr>
                  <td nowrap="nowrap" class="core_errorlabel_td" valign="top">
                    <span class="core_errorlabel_text"><xsl:apply-templates select="$label[position() = 1]/node()"/></span>
                  </td>
                  <td class="core_errortext_td">
                    <span class="core_errortext">
                      <ixsl:apply-templates select="/formresult/formerrors/error[@name='{$labelname}']"/>
                    </span>
                  </td>
                </tr>
              </ixsl:if>
            </xsl:for-each>
	  </xsl:otherwise>
	</xsl:choose>
      </table>
    </ixsl:if>
  </xsl:template>

  
  <xsl:template match="pfx:checkactive">
      
    <xsl:variable name="prefix"><xsl:value-of select="@prefix"/></xsl:variable>
    <xsl:variable name="pg"><xsl:value-of select="@page"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="not($pg = '') and not($prefix = '')">
        <b>[Error: You can't give both attributes "prefix" and "page" to &lt;pfx:checkactive&gt;]</b>
      </xsl:when>
      <xsl:when test="not($prefix = '')">
        <ixsl:if test="/formresult/iwrappergroups/group[@current = 'true']/interface[@prefix = '{$prefix}' and @active = 'true']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:when test="not($pg = '')">
        <ixsl:if test="/formresult/navigation//page[@name = '{$pg}' and @visible = '1']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:otherwise>
        <b>[Error: You need to specify exactly one of the attributes "prefix" or "page" for &lt;pfx:checkactive&gt;]</b>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  
  <xsl:template match="pfx:checknotactive">
      
    <xsl:variable name="prefix"><xsl:value-of select="@prefix"/></xsl:variable>
    <xsl:variable name="pg"><xsl:value-of select="@page"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="not($pg = '') and not($prefix = '')">
        <b>[Error: You can't give both attributes "prefix" and "page" to &lt;pfx:checknotactive&gt;]</b>
      </xsl:when>
      <xsl:when test="not($prefix = '')">
        <ixsl:if test="/formresult/iwrappergroups/group[@current = 'true']/interface[@prefix = '{$prefix}' and @active = 'false']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:when test="not($pg = '')">
        <ixsl:if test="/formresult/navigation//page[@name = '{$pg}' and @visible = '0']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:otherwise>
        <b>[Error: You need to specify exactly one of the attributes "prefix" or "page" for &lt;pfx:checknotactive&gt;]</b>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 

  
  <xsl:template match="pfx:groupdisplayonly">
      
    <xsl:variable name="group"><xsl:value-of select="@name"/></xsl:variable>
    <ixsl:if test="/formresult/iwrappergroups/group[@current = 'true' and @name = '{$group}']">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template> 

  
  <xsl:template match="pfx:flatdisplayonly">
      
    <ixsl:if test="/formresult/iwrappergroups[not(@groupdisplay = 'true')]">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template> 

  
  <xsl:template match="pfx:forminput">
      
    <xsl:variable name="send-to-page" select="@send-to-page"/>
    <xsl:variable name="theframe">
      <xsl:choose>
        <xsl:when test="@frame">
          <xsl:value-of select="./@frame"/>
        </xsl:when>
        <xsl:when test="@target">
          <xsl:choose>
            <xsl:when test="@target = '_parent'">
              <xsl:choose>
                <xsl:when test="ancestor::pfx:frame[position()=2]">
                  <xsl:value-of select="ancestor::pfx:frame[position()=2]"/>
                </xsl:when>
                <xsl:otherwise>_top</xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="./@target"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="ancestor-or-self::pfx:frame[position()=1]/@name">
            <xsl:value-of select="ancestor-or-self::pfx:frame[position()=1]/@name"/>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="sendingdata">
      <xsl:choose>
        <xsl:when test="@type = 'auth'">__sendingauthdata</xsl:when>
        <xsl:otherwise>__sendingdata</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thehandler">
      <xsl:choose>
        <xsl:when test="$send-to-page">
          <xsl:value-of select="$navitree//page[@name=$send-to-page]/@handler"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$navitree//page[@name=$page]/@handler"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <form method="post">
      <xsl:copy-of select="./@*"/>
      <ixsl:attribute name="action">
        <xsl:choose>
          <xsl:when test="$send-to-page">
            <xsl:value-of select="concat($thehandler, '/', $send-to-page)"/>;<ixsl:value-of select="$__sessid"/><xsl:if test="not($theframe = '')"/>?__frame=<xsl:value-of select="$theframe"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($thehandler, '/', $page)"/>;<ixsl:value-of select="$__sessid"/><xsl:if test="not($theframe = '')"/>?__frame=<xsl:value-of select="$theframe"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:attribute>
      <input type="hidden" name="{$sendingdata}" value="1"/>

      <ixsl:if test="/formresult/iwrappergroups/@currentindex">
        <input type="hidden">
          <xsl:attribute name="name">__currentindex[<xsl:value-of select="$page"/>]</xsl:attribute>
          <ixsl:attribute name="value"><ixsl:value-of select="/formresult/iwrappergroups/@currentindex"/></ixsl:attribute>
        </input>
      </ixsl:if>


      <ixsl:for-each select="/formresult/formhiddenvals/hidden">
        <input type="hidden">
          <ixsl:attribute name="name"><ixsl:value-of select="./@name"/></ixsl:attribute>
          <ixsl:attribute name="value"><ixsl:value-of select="./text()"/></ixsl:attribute>
        </input>
      </ixsl:for-each>
      <xsl:apply-templates/>
      
    </form>

    
  </xsl:template>





  <xsl:template name="generate_coded_name">
    
    <ixsl:attribute name="name">
      <xsl:for-each select="./pfx:argument">__DATA:<xsl:value-of select="@name"/>:<xsl:apply-templates select="./node()"/>:</xsl:for-each>
      <xsl:for-each select="./pfx:anchor">__DATA:__anchor:<xsl:value-of select="@frame"/>|<xsl:apply-templates select="./node()"/>:</xsl:for-each>
      <xsl:for-each select="./pfx:command">__CMD[<xsl:choose>
          <xsl:when test="@page">
            <xsl:value-of select="@page"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$page"/>
          </xsl:otherwise>
        </xsl:choose>]:<xsl:value-of select="@name"/>:<xsl:apply-templates select="./node()"/>:</xsl:for-each>
    </ixsl:attribute>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='image']">
    
    <input border="0"> 
      <xsl:copy-of select="./@*"/>
      <xsl:call-template name="pfx:image_geom_impl"/>
      <xsl:if test="./pfx:argument or ./pfx:command or ./pfx:anchor">
        <xsl:call-template name="generate_coded_name"/>
      </xsl:if> 
      <xsl:apply-templates/>
    </input>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='submit']">
      
    <input> 
      <xsl:copy-of select="./@*"/>
      <xsl:choose>
        <xsl:when test="not(@value)">
          <xsl:if test="./pfx:argument or ./pfx:command or ./pfx:anchor">
            <xsl:call-template name="generate_coded_name"/>
          </xsl:if> 
          <xsl:attribute name="value">
            <xsl:apply-templates/>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="./pfx:argument or ./pfx:command or ./pfx:anchor">
            <xsl:call-template name="generate_coded_name"/>
          </xsl:if> 
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </input>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='hidden']">
    
    <input type="hidden">
      <xsl:copy-of select="@name"/>
      <ixsl:attribute name="value">
        <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
      </ixsl:attribute>
      <xsl:apply-templates/>
    </input>
  </xsl:template>


  <xsl:template match="pfx:xinp[@type='radio']">
      
    <input type="radio">
      <xsl:copy-of select="@*"/>
      <ixsl:if test="/formresult/formvalues/param[@name='{@name}']/text()='{@value}'">
        <ixsl:attribute name="checked">checked</ixsl:attribute>
      </ixsl:if>
      <xsl:apply-templates/>
    </input>
  </xsl:template>

  
  <xsl:template name="renderoptions">
    
    <xsl:param name="options"/>
    <xsl:param name="name"/>
    <xsl:for-each select="$options">
      <xsl:variable name="value">
	<xsl:choose>
	  <xsl:when test="@value">
	    <xsl:value-of select="@value"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:apply-templates select="./node()"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <option>
	<xsl:copy-of select="@*[name()!= 'position']"/>
	<ixsl:if test="/formresult/formvalues/param[@name='{$name}']/text()='{$value}'">
	  <ixsl:attribute name="selected">selected</ixsl:attribute>
	</ixsl:if>
	<xsl:apply-templates select="./node()"/>
      </option>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='select']">
      
    <xsl:variable name="name" select="string(@name)"/>
    <select name="{$name}">
      <xsl:copy-of select="@*[name()!='type']"/>
      <xsl:if test="@multiple"><xsl:attribute name="multiple">multiple</xsl:attribute></xsl:if>
      <xsl:call-template name="renderoptions">
	<xsl:with-param name="options" select=".//pfx:option"/>
	<xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
      </xsl:call-template>
    </select>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='dynselect']">
    
    
    <xsl:variable name="name" select="string(@name)"/>
    <select name="{$name}">
      <xsl:copy-of select="@*[name()!='type' and name()!='optionpath']"/>
      <xsl:if test="@multiple"><xsl:attribute name="multiple">multiple</xsl:attribute></xsl:if>

      <xsl:call-template name="renderoptions">
	<xsl:with-param name="options" select=".//pfx:option[not(@position = 'end')]"/>
	<xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
      </xsl:call-template>
      
      <ixsl:for-each select="{@optionpath}/option">
        <ixsl:variable name="value">
          <ixsl:choose>
            <ixsl:when test="@value">
              <ixsl:value-of select="@value"/>
            </ixsl:when>
            <ixsl:otherwise>
              <ixsl:apply-templates select="./node()"/>
            </ixsl:otherwise>
          </ixsl:choose>
        </ixsl:variable>
        <option>
          <ixsl:copy-of select="./@*"/>
          <ixsl:if test="/formresult/formvalues/param[@name='{$name}']/text() = $value">
            <ixsl:attribute name="selected">selected</ixsl:attribute>
          </ixsl:if>
          <ixsl:apply-templates select="./node()"/></option>
      </ixsl:for-each>
      
      <xsl:call-template name="renderoptions">
	<xsl:with-param name="options" select=".//pfx:option[@position = 'end']"/>
	<xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
      </xsl:call-template>
      
    </select>
  </xsl:template>
                                                                                   
  <xsl:template match="pfx:xinp[@type='check']">
        
    <input type="checkbox">
      <xsl:copy-of select="@*[name()!='type']"/>
      <ixsl:if test="/formresult/formvalues/param[@name='{@name}']/text()='{@value}'">
        <ixsl:attribute name="checked">checked</ixsl:attribute>
      </ixsl:if>
      <xsl:apply-templates/>
    </input>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='text']">
    
    <input type="text" size="40" maxlength="40">
      <xsl:copy-of select="@*"/>
      <ixsl:attribute name="value">
        <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
      </ixsl:attribute>
    </input>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='password']">
   
    <input type="password" size="40" maxlength="40">
      <xsl:copy-of select="@*"/>
      <ixsl:attribute name="value">
        <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
      </ixsl:attribute>
    </input>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='area']">
       
    <textarea cols="38" rows="20">
      <xsl:copy-of select="@*"/>
      <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
    </textarea>
  </xsl:template>
  
  <xsl:template match="pfx:xoutp">
      
    <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
  </xsl:template>
  
  <xsl:template match="pfx:xoutperror">
    <ixsl:apply-templates select="/formresult/formerrors/error[@name='{@name}']"/>
  </xsl:template>



  

</xsl:stylesheet>