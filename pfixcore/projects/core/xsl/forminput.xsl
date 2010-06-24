<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
		xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="pfx:checkactive">
    <xsl:variable name="prefix"><xsl:value-of select="@prefix"/></xsl:variable>
    <xsl:variable name="pg"><xsl:value-of select="@page"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="not($pg = '') and not($prefix = '')">
        <b>[Error: You can't give both attributes "prefix" and "page" to &lt;pfx:checkactive&gt;]</b>
      </xsl:when>
      <xsl:when test="not($prefix = '')">
        <ixsl:if test="$__root/formresult/wrapperstatus/wrapper[@prefix = '{$prefix}' and @active = 'true']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:when test="not($pg = '')">
        <ixsl:if test="callback:isAccessible($__context__, $__target_gen, '{$pg}') = 1">
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
        <ixsl:if test="$__root/formresult/wrapperstatus/wrapper[@prefix = '{$prefix}' and @active = 'false']">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:when test="not($pg = '')">
        <ixsl:if test="callback:isAccessible($__context__, $__target_gen, '{$pg}') = 0">
          <xsl:apply-templates/>
        </ixsl:if>
      </xsl:when>
      <xsl:otherwise>
        <b>[Error: You need to specify exactly one of the attributes "prefix" or "page" for &lt;pfx:checknotactive&gt;]</b>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 

  <xsl:template match="pfx:checkmessage">
    <ixsl:if test="true()">
      <ixsl:variable name="pfx_allmessages_check">
        <xsl:attribute name="select">
          <xsl:choose>
            <xsl:when test="@level">$__root/formresult/pagemessages/message[@level = '<xsl:value-of select="string(@level)"/>']</xsl:when>
            <xsl:otherwise>$__root/formresult/pagemessages/message</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </ixsl:variable>
      <ixsl:if test="$pfx_allmessages_check">
        <xsl:apply-templates/>
      </ixsl:if>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:messageloop">
    <ixsl:if test="true()">
      <ixsl:variable name="pfx_allmessages">
        <xsl:attribute name="select">
          <xsl:choose>
            <xsl:when test="@level">$__root/formresult/pagemessages/message[@level = '<xsl:value-of select="string(@level)"/>']</xsl:when>
            <xsl:otherwise>$__root/formresult/pagemessages/message</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </ixsl:variable>
      <ixsl:for-each select="$pfx_allmessages">
        <ixsl:variable name="pfx_scode" select="."/>
        <ixsl:variable name="pfx_level" select="string(./@level)"/>
        <ixsl:variable name="pfx_class">PfxMessage PfxMessageLevel_<ixsl:value-of select="$pfx_level"/></ixsl:variable>
        <xsl:apply-templates/>
      </ixsl:for-each>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:checkerror">
    <ixsl:if test="true()">
      <ixsl:variable name="pfx_allerrors_check">
        <xsl:attribute name="select">
          <xsl:choose>
            <xsl:when test="@level">$__root/formresult/formerrors/error[@level = '<xsl:value-of select="string(@level)"/>']</xsl:when>
            <xsl:otherwise>$__root/formresult/formerrors/error</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </ixsl:variable>
      <ixsl:if test="$pfx_allerrors_check">
        <xsl:apply-templates/>
      </ixsl:if>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:checkfield">
    <ixsl:if test="true()"> <!-- make sure to have a local scope --> 
      <ixsl:variable name="pfx_name">
        <xsl:choose>
          <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
          <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"/>
          <ixsl:when test="not($pfx_level)">PfxError</ixsl:when>
          <ixsl:otherwise>PfxError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:error">
    <!-- $pfx_scode is defined in parent pfx:checkfield -->
    <ixsl:if test="$pfx_scode">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:normal">
    <!-- $pfx_scode is defined in parent pfx:checkfield -->
    <ixsl:if test="not($pfx_scode)">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:scode">
    <!-- $pfx_scode is defined in parent pfx:checkfield or pfx:pagemessageloop -->
    <ixsl:apply-templates select="$pfx_scode/node()"/>
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
      <xsl:copy-of select="./@*[name()!='send-to-page' and name()!='send-to-pageflow' and name()!='type']"/>
      <ixsl:attribute name="action">
        <ixsl:value-of select="$__contextpath"/>
        <xsl:choose>
          <xsl:when test="$send-to-page">
            <xsl:value-of select="concat($thehandler, '/', $send-to-page)"/>;<ixsl:value-of select="$__sessid"/><xsl:if test="not($theframe = '')"></xsl:if>?__frame=<xsl:value-of select="$theframe"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="@type='auth'">
                <ixsl:choose>
                  <ixsl:when test="$__root/formresult/authentication/@targetpage">
                    <xsl:value-of select="$thehandler"/>/<ixsl:value-of select="$__root/formresult/authentication/@targetpage"/>;<ixsl:value-of select="$__sessid"/>
                  </ixsl:when>
                  <ixsl:otherwise>
                    <xsl:value-of select="concat($thehandler, '/', $page)"/>;<ixsl:value-of select="$__sessid"/><xsl:if test="not($theframe = '')"></xsl:if>?__frame=<xsl:value-of select="$theframe"/>
                  </ixsl:otherwise>
                </ixsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($thehandler, '/', $page)"/>;<ixsl:value-of select="$__sessid"/><xsl:if test="not($theframe = '')"></xsl:if>?__frame=<xsl:value-of select="$theframe"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:attribute>
      <xsl:if test="not(.//fieldset or .//pfx:hiddenfields)">
        <xsl:apply-templates select="." mode="render_hidden_fields"/>
      </xsl:if>
      <xsl:apply-templates/>
    </form>
  </xsl:template>
  
  <xsl:template match="pfx:forminput" mode="render_hidden_fields">
    <ixsl:if test="not($__lf = '')">
      <input type="hidden" name="__lf">
        <ixsl:attribute name="value"><ixsl:value-of select="$__lf"/></ixsl:attribute>
      </input>
    </ixsl:if>
    <input type="hidden" name="__sendingdata" value="1"/>
    <xsl:if test="@type='auth'">
      <input type="hidden" name="__sendingauthdata" value="1"/>
    </xsl:if>
    <xsl:if test="@send-to-pageflow">
      <input type="hidden" name="__pageflow" value="{@send-to-pageflow}"/>
    </xsl:if>
    <ixsl:for-each select="$__root/formresult/formhiddenvals/hidden">
      <input type="hidden">
        <ixsl:attribute name="name"><ixsl:value-of select="./@name"/></ixsl:attribute>
        <ixsl:attribute name="value"><ixsl:value-of select="./text()"/></ixsl:attribute>
      </input>
    </ixsl:for-each>
    <xsl:if test="not(.//pfx:token)">
      <xsl:variable name="pageName">
        <xsl:choose>
          <xsl:when test="@send-to-page"><xsl:value-of select="@send-to-page"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <ixsl:if test="pfx:requiresToken('{$pageName}')">
        <xsl:call-template name="createToken">
          <xsl:with-param name="tokenName"><xsl:value-of select="concat($pageName,'#',generate-id())"/></xsl:with-param>
        </xsl:call-template>
      </ixsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:hiddenfields">
    <xsl:apply-templates select="ancestor::pfx:forminput[1]" mode="render_hidden_fields"/>
  </xsl:template>
  
  <xsl:template match="fieldset">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="form" select="ancestor::pfx:forminput[1]"/>
      <xsl:if test="$form and generate-id($form//fieldset[1])=generate-id(.) and not($form//pfx:hiddenfields)">
        <xsl:apply-templates select="$form" mode="render_hidden_fields"/>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="generate_coded_input">
    <xsl:variable name="current" select="generate-id(.)"/>
    <xsl:if test="@action">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__action</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@action"/></ixsl:attribute></input>
    </xsl:if>
    <xsl:if test="@jumptopage">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__jumptopage</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@jumptopage"/></ixsl:attribute></input>
    </xsl:if>
    <xsl:if test="@jumptopageflow">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__jumptopageflow</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@jumptopageflow"/></ixsl:attribute></input>
    </xsl:if>
    <xsl:if test="@forcestop">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__forcestop</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@forcestop"/></ixsl:attribute></input>
    </xsl:if>
    <xsl:if test="@pageflow">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__pageflow</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@pageflow"/></ixsl:attribute></input>
    </xsl:if>
    <xsl:for-each select="./pfx:argument">
      <ixsl:if test="1">
      <ixsl:variable name="pfx_name"><xsl:choose>
        <xsl:when test="./@name"><xsl:value-of select="./@name"/></xsl:when>
        <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:call-template name="__formwarn">
        <ixsl:with-param name="targetpage"><xsl:choose>
          <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
        </xsl:choose></ixsl:with-param>
        <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
      </ixsl:call-template>
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:<ixsl:value-of select="$pfx_name"/></ixsl:attribute>
        <ixsl:attribute name="value"><xsl:apply-templates select="./node()"/></ixsl:attribute></input>
       </ixsl:if>
    </xsl:for-each>
    <xsl:for-each select="./pfx:anchor">
      <input type="hidden">
        <ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__anchor</ixsl:attribute>
      <ixsl:attribute name="value"><xsl:value-of select="@frame"/>|<xsl:apply-templates select="./node()"/></ixsl:attribute></input>
    </xsl:for-each>    
    <xsl:for-each select="./pfx:command">
      <input type="hidden"><ixsl:attribute name="name">__SYNT:<ixsl:value-of select="$genname_{$current}"/>:__CMD[<xsl:choose>
      <xsl:when test="@page">
        <xsl:value-of select="@page"/>
      </xsl:when>
      <xsl:when test="ancestor::pfx:forminput[position() = 1 and @send-to-page != '']">
        <xsl:value-of select="ancestor::pfx:forminput[position() = 1]/@send-to-page"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$page"/>
      </xsl:otherwise>
    </xsl:choose>]:<xsl:value-of select="@name"/></ixsl:attribute><ixsl:attribute name="value"><xsl:apply-templates select="./node()"/></ixsl:attribute></input>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='image']" name="pfx:xinp_image">
    <xsl:param name="src" select="@src"/>
    <xsl:param name="themed-path" select="@themed-path"/>
    <xsl:param name="themed-img"  select="@themed-img"/>
    <xsl:param name="alt" select="@alt"/>
    <xsl:param name="exclude-attributes"/>
    <xsl:param name="module" select="@module"/>
    <xsl:param name="search" select="@search"/>
    <xsl:variable name="always-exclude-attributes" select="'src|themed-path|themed-img|alt|width|height|type|name|jumptopage|jumptopageflow|forcestop|pageflow|module|search|action|level'"/>
    <xsl:choose>
      <xsl:when test="not(@level='runtime')">
        <xsl:variable name="realsrc">
          <xsl:call-template name="pfx:image_register_src">
            <xsl:with-param name="src" select="$src"/>
            <xsl:with-param name="themed-path" select="$themed-path"/>
            <xsl:with-param name="themed-img" select="$themed-img"/>
            <xsl:with-param name="module" select="$module"/>
            <xsl:with-param name="search" select="$search"/>
          </xsl:call-template>
        </xsl:variable>
        <ixsl:variable><xsl:attribute name="name">genname_<xsl:value-of select="generate-id(.)"/></xsl:attribute><xsl:value-of select="generate-id(.)"/><ixsl:value-of select="generate-id(.)"/></ixsl:variable>
        <input type="image" src="{{$__contextpath}}/{$realsrc}" alt="{$alt}"> 
          <xsl:copy-of select="@*[not(contains(concat('|',$always-exclude-attributes,'|',$exclude-attributes,'|') , concat('|',name(),'|')))]"/>
          <xsl:attribute name="class"><xsl:value-of select="@class"/> PfxInputImage</xsl:attribute>
          <xsl:call-template name="pfx:image_geom_impl_new">
            <xsl:with-param name="src" select="$realsrc"/>
          </xsl:call-template>
          <ixsl:attribute name="name">__SBMT:<ixsl:value-of select="$genname_{generate-id(.)}"/>:</ixsl:attribute>
          <xsl:apply-templates/>
        </input>
        <xsl:call-template name="generate_coded_input"/>
      </xsl:when>
      <xsl:otherwise>
        <ixsl:variable><xsl:attribute name="name">genname_<xsl:value-of select="generate-id(.)"/></xsl:attribute><xsl:value-of select="generate-id(.)"/><ixsl:value-of select="generate-id(.)"/></ixsl:variable>
        <input type="image">
          <xsl:copy-of select="@*[not(contains(concat('|',$always-exclude-attributes,'|',$exclude-attributes,'|') , concat('|',name(),'|')))]"/>
          <ixsl:variable name="realsrc">
            <ixsl:call-template name="pfx:image_register_src">
              <ixsl:with-param name="src">
                <xsl:choose>
                  <xsl:when test="pfx:src"><xsl:apply-templates select="pfx:src/node()"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="@src"/></xsl:otherwise>
                </xsl:choose>
              </ixsl:with-param>
              <ixsl:with-param name="themed-path">
                <xsl:choose>
                  <xsl:when test="pfx:themed-path"><xsl:apply-templates select="pfx:themed-path/node()"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="@themed-path"/></xsl:otherwise>
                </xsl:choose>
              </ixsl:with-param>
              <ixsl:with-param name="themed-img">
                <xsl:choose>
                  <xsl:when test="pfx:themed-img"><xsl:apply-templates select="pfx:themed-img/node()"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="@themed-img"/></xsl:otherwise>
                </xsl:choose>
              </ixsl:with-param>
              <ixsl:with-param name="module"><xsl:value-of select="@module"/></ixsl:with-param>
              <ixsl:with-param name="search"><xsl:value-of select="@search"/></ixsl:with-param>
            </ixsl:call-template>
          </ixsl:variable>
          <ixsl:attribute name="src"><ixsl:value-of select="concat($__contextpath,'/',$realsrc)"/></ixsl:attribute>
          <ixsl:attribute name="alt">
            <xsl:choose>
              <xsl:when test="pfx:alt"><xsl:apply-templates select="pfx:alt/node()"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="@alt"/></xsl:otherwise>
            </xsl:choose>
          </ixsl:attribute>
          <ixsl:attribute name="class"><xsl:value-of select="@class"/> PfxInputImage</ixsl:attribute>
          <ixsl:call-template name="pfx:image_geom_impl_new">
            <ixsl:with-param name="src" select="$realsrc"/>
          </ixsl:call-template>
          <ixsl:attribute name="name">__SBMT:<ixsl:value-of select="$genname_{generate-id(.)}"/>:</ixsl:attribute>
        </input>
        <xsl:call-template name="generate_coded_input"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="pfx:submitbutton">
    <xsl:call-template name="pfx:xinp_submit">
      <xsl:with-param name="tag-name">button</xsl:with-param>
      <xsl:with-param name="tag-content">true</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='submit']" name="pfx:xinp_submit">
    <xsl:param name="exclude-attributes"/>
    <xsl:param name="tag-name">input</xsl:param>
    <xsl:param name="tag-content">false</xsl:param>
    <xsl:variable name="always-exclude-attributes" select="'type|name|jumptopage|jumptopageflow|forcestop|pageflow|action'"/>
    <ixsl:variable><xsl:attribute name="name">genname_<xsl:value-of select="generate-id(.)"/></xsl:attribute><xsl:value-of select="generate-id(.)"/><ixsl:value-of select="generate-id(.)"/></ixsl:variable>
    <xsl:element name="{$tag-name}">
      <xsl:attribute name="type">submit</xsl:attribute>
      <xsl:copy-of select="@*[not(contains(concat('|',$always-exclude-attributes,'|',$exclude-attributes,'|') , concat('|',name(),'|')))]"/>
      <xsl:attribute name="class"><xsl:value-of select="@class"/> PfxInputSubmit</xsl:attribute>
      <ixsl:attribute name="name">__SBMT:<ixsl:value-of select="$genname_{generate-id(.)}"/>:</ixsl:attribute>
      <ixsl:attribute name="value">
        <xsl:choose>
          <xsl:when test="@value"><xsl:value-of select="@value"/></xsl:when>
          <xsl:when test="./pfx:value"><xsl:apply-templates select="./pfx:value/node()"/></xsl:when>
          <xsl:when test="not($tag-content='true')"><xsl:apply-templates/></xsl:when>
        </xsl:choose>
      </ixsl:attribute>
      <xsl:if test="$tag-content='true'">
        <xsl:apply-templates/>
      </xsl:if>
    </xsl:element>
    <xsl:call-template name="generate_coded_input"/>
  </xsl:template>

  <xsl:template match="pfx:value"/>
  <xsl:template match="pfx:name"/>
  <xsl:template match="pfx:default"/>
 
  <xsl:template match="pfx:xinp[@type='hidden']">
    <input type="hidden">
      <xsl:variable name="thename" select="@name"/>
      <ixsl:variable name="pfx_name"><xsl:choose>
        <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$thename"/></xsl:otherwise>
      </xsl:choose></ixsl:variable>
      <ixsl:attribute name="name"><ixsl:value-of select="$pfx_name"/></ixsl:attribute>
      <ixsl:variable name="pfx_default"><xsl:choose>
        <xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
        <xsl:when test="./pfx:default"><xsl:apply-templates select="./pfx:default/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <xsl:variable name="pos">
        <xsl:choose>
          <xsl:when test="@position"><xsl:value-of select="@position"/></xsl:when>
          <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <ixsl:attribute name="value">
        <ixsl:choose>
          <ixsl:when test="not($__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}])"><ixsl:value-of select="$pfx_default"/></ixsl:when>
          <ixsl:otherwise><ixsl:value-of select="$__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}]"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:attribute>
      <xsl:apply-templates/>
    </input>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='select']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <select>
      <xsl:copy-of select="@*[name()!='type' and name()!='name']"/>
      <xsl:if test="@multiple"><xsl:attribute name="multiple">multiple</xsl:attribute></xsl:if>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <xsl:apply-templates/>
    </select>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:option">
    <!-- $pfx_name will be set in the parent select -->
    <option>
      <xsl:copy-of select="@*[name()!= 'default' and name()!= 'value']"/>
      <ixsl:variable name="pfx_value"><xsl:choose>
        <xsl:when test="@value"><xsl:value-of select="@value"/></xsl:when>
        <xsl:when test="./pfx:value"><xsl:apply-templates select="./pfx:value/node()"/></xsl:when>
        <xsl:otherwise><xsl:apply-templates select="./node()[name() != 'ixsl:attribute']"/></xsl:otherwise>
      </xsl:choose></ixsl:variable>
      <ixsl:variable name="pfx_default"><xsl:choose>
        <xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
        <xsl:when test="./pfx:default"><xsl:apply-templates select="./pfx:default/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:attribute name="value"><ixsl:value-of select="string($pfx_value)"/></ixsl:attribute>
      <ixsl:if test="$__root/formresult/formvalues/param[@name=string($pfx_name)]/text()=string($pfx_value) or
                     ($pfx_default = 'true' and not($__root/formresult/formvalues/param[@name=string($pfx_name)]))">
        <ixsl:attribute name="selected">selected</ixsl:attribute>
      </ixsl:if>
      <xsl:apply-templates select="./node()"/>
    </option>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='radio']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <input type="radio">
      <xsl:copy-of select="@*[name()!='type' and name()!='default' and name()!='value' and name()!='name']"/>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError PfxInputRadioError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxInputRadioError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <ixsl:variable name="pfx_value"><xsl:choose>
        <xsl:when test="@value"><xsl:value-of select="@value"/></xsl:when>
        <xsl:when test="./pfx:value"><xsl:apply-templates select="./pfx:value/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:attribute name="value"><ixsl:value-of select="string($pfx_value)"/></ixsl:attribute>
      <ixsl:variable name="pfx_default"><xsl:choose>
        <xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
        <xsl:when test="./pfx:default"><xsl:apply-templates select="./pfx:default/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:if test="$__root/formresult/formvalues/param[@name=string($pfx_name)]/text()=string($pfx_value) or
                     (string($pfx_default) = 'true' and not($__root/formresult/formvalues/param[@name=string($pfx_name)]))">
        <ixsl:attribute name="checked">checked</ixsl:attribute>
      </ixsl:if>
      <xsl:apply-templates/>
    </input>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:xinp[@type='check']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <input type="checkbox">
      <xsl:copy-of select="@*[name()!='type' and name()!='default' and name()!='value' and name()!='name']"/>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError PfxInputCheckError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxInputCheckError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <ixsl:variable name="pfx_value"><xsl:choose>
        <xsl:when test="@value"><xsl:value-of select="@value"/></xsl:when>
        <xsl:when test="./pfx:value"><xsl:apply-templates select="./pfx:value/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:attribute name="value"><ixsl:value-of select="string($pfx_value)"/></ixsl:attribute>
      <ixsl:variable name="pfx_default"><xsl:choose>
        <xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
        <xsl:when test="./pfx:default"><xsl:apply-templates select="./pfx:default/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:if test="$__root/formresult/formvalues/param[@name=string($pfx_name)]/text()=string($pfx_value) or
                     (string($pfx_default) = 'true' and not($__root/formresult/formvalues/param[@name=string($pfx_name)]))">
        <ixsl:attribute name="checked">checked</ixsl:attribute>
      </ixsl:if>
      <xsl:apply-templates/>
    </input>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='text']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <input type="text" size="40" maxlength="200">
      <xsl:copy-of select="@*[name()!='type' and name()!='default' and name()!='position' and name()!='name']"/>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError PfxInputTextError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxInputTextError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <ixsl:variable name="pfx_default">
        <xsl:choose>
          <xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
          <xsl:when test="./pfx:default"><xsl:apply-templates select="./pfx:default/node()"/></xsl:when>
        </xsl:choose>
      </ixsl:variable>
      <xsl:variable name="pos">
        <xsl:choose>
          <xsl:when test="@position">
            <xsl:value-of select="@position"/>
          </xsl:when>
          <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <ixsl:attribute name="value">
        <ixsl:choose>
          <ixsl:when test="not($__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}])"><ixsl:value-of select="$pfx_default"/></ixsl:when>
          <ixsl:otherwise><ixsl:value-of select="$__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}]"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:attribute>
      <xsl:apply-templates/>
    </input>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='password']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <input type="password" size="40" maxlength="200">
      <xsl:copy-of select="@*[name()!='type' and name()!='name']"/>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError PfxInputPasswordError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxInputPasswordError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <ixsl:attribute name="value">
        <ixsl:value-of select="$__root/formresult/formvalues/param[@name=string($pfx_name)][position() = 1]"/>
      </ixsl:attribute>
      <xsl:apply-templates/>
    </input>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:xinp[@type='area']">
    <ixsl:if test="1">
    <ixsl:variable name="pfx_name"><xsl:choose>
      <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
      <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
    </xsl:choose></ixsl:variable>
    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage"><xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose></ixsl:with-param>
      <ixsl:with-param name="fullname"><ixsl:value-of select="$pfx_name"/></ixsl:with-param>
    </ixsl:call-template>
    <textarea cols="38" rows="20">
      <xsl:copy-of select="@*[name()!='type' and name()!='position' and name()!='name']"/>
      <ixsl:attribute name="name"><ixsl:value-of select="string($pfx_name)"/></ixsl:attribute>
      <ixsl:variable name="pfx_scode" select="$__root/formresult/formerrors/error[@name=string($pfx_name)]"/>
      <ixsl:variable name="pfx_level" select="$pfx_scode/@level"/>
      <ixsl:variable name="pfx_class">
        <ixsl:choose>
          <ixsl:when test="not($pfx_scode)"><xsl:value-of select="@class"/></ixsl:when>
          <ixsl:when test="not($pfx_level)"><xsl:value-of select="@class"/> PfxError</ixsl:when>
          <ixsl:otherwise><xsl:value-of select="@class"/> PfxError PfxErrorLevel_<ixsl:value-of select="string($pfx_level)"/></ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:attribute name="class"><ixsl:value-of select="string($pfx_class)"/></ixsl:attribute>
      <xsl:variable name="pos">
        <xsl:choose>
          <xsl:when test="@position"><xsl:value-of select="@position"/></xsl:when>
          <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <ixsl:choose>
        <ixsl:when test="not($__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}])"><xsl:apply-templates/></ixsl:when>
        <ixsl:otherwise><ixsl:value-of select="$__root/formresult/formvalues/param[@name=string($pfx_name)][position() = {$pos}]"/></ixsl:otherwise>
      </ixsl:choose>
    </textarea>
    </ixsl:if>
  </xsl:template>


  <!-- ****************** Helper templates ******************************* -->

<!--  <xsl:template name="pfx:formwarn">-->
<!--    <xsl:param name="type"/>-->
<!--    <xsl:param name="data"/>-->
<!--    <xsl:variable name="style_err">position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;</xsl:variable>-->
<!--    <xsl:variable name="theform" select="ancestor::pfx:forminput[position()=1]"/>-->
<!--    <xsl:variable name="targetpage">-->
<!--      <xsl:choose>-->
<!--        <xsl:when test="$theform/@send-to-page">-->
<!--          <xsl:value-of select="$theform/@send-to-page"/>-->
<!--        </xsl:when>-->
<!--        <xsl:otherwise>-->
<!--          <xsl:value-of select="$page"/>-->
<!--        </xsl:otherwise>-->
<!--      </xsl:choose> -->
<!--    </xsl:variable>-->
<!--    <xsl:if test="$prohibitEdit = 'no'">-->
<!--      <xsl:choose>-->
<!--        <xsl:when test="$type = 'unknown'">-->
<!--           <xsl:variable name="fullname"><xsl:value-of select="$data/@name"/></xsl:variable>-->
<!--           <xsl:if test="contains($fullname, '.')">-->
<!--            <xsl:variable name="prefix" select="substring-before($fullname, '.')"/>-->
<!--            <xsl:variable name="tmp" select="substring-after($fullname, '.')"/>-->
<!--            <xsl:variable name="name">-->
<!--              <xsl:choose>-->
<!--                <xsl:when test="contains($tmp, '.')"><xsl:value-of select="substring-before($tmp, '.')"/></xsl:when>-->
<!--                <xsl:otherwise><xsl:value-of select="$tmp"/></xsl:otherwise>-->
<!--              </xsl:choose>-->
<!--            </xsl:variable>-->
<!--            <xsl:variable name="index" select="substring-after($tmp, '.')"/>-->
<!--            <ixsl:choose>-->
<!--              <ixsl:when test="not(pfx:getIWrapperInfo('{$targetpage}','{$prefix}'))">-->
<!--                <div style="{$style_err}" onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">-->
<!--                  Warning: Unknown wrapper <b><xsl:value-of select="$prefix"/></b>-->
<!--                </div>-->
<!--              </ixsl:when>-->
<!--              <ixsl:when test="not(pfx:getIWrapperInfo('{$targetpage}','{$prefix}')/iwrapper/param[@name = '{$name}'])">-->
<!--                <div style="{$style_err}" onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">-->
<!--                  Warning: Unknown parameter <b><xsl:value-of select="$name"/></b> in wrapper <b><xsl:value-of select="$prefix"/></b>-->
<!--                </div>-->
<!--              </ixsl:when>-->
<!--              <xsl:choose>-->
<!--                <xsl:when test="$index">-->
<!--                  <ixsl:when test="not(pfx:getIWrapperInfo('{$targetpage}','{$prefix}')/iwrapper/param[@name = '{$name}' and @occurrence = 'indexed'])">-->
<!--                    <div style="{$style_err}" onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">-->
<!--                      Warning: No indexed parameter <b><xsl:value-of select="$name"/></b> in wrapper <b><xsl:value-of select="$prefix"/></b>-->
<!--                    </div>-->
<!--                  </ixsl:when>-->
<!--                </xsl:when>-->
<!--                <xsl:otherwise>-->
<!--                  <ixsl:when test="not(pfx:getIWrapperInfo('{$targetpage}','{$prefix}')/iwrapper/param[@name = '{$name}' and @occurrence != 'indexed'])">-->
<!--                    <div style="{$style_err}" onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">-->
<!--                      Warning: Parameter <b><xsl:value-of select="$name"/></b> in wrapper <b><xsl:value-of select="$prefix"/> must be indexed</b>-->
<!--                    </div>-->
<!--                  </ixsl:when>-->
<!--              </xsl:otherwise>-->
<!--              </xsl:choose>-->
<!--            </ixsl:choose>-->
<!--          </xsl:if>-->
<!--        </xsl:when> -->
<!--        <xsl:otherwise>-->
<!--          <div onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">-->
<!--             <xsl:apply-templates/>-->
<!--          </div>-->
<!--        </xsl:otherwise>  -->
<!--      </xsl:choose> -->
<!--    </xsl:if>-->
<!--  </xsl:template>-->

  <xsl:template match="pfx:token" name="createToken">
    <xsl:param name="tokenName">
      <xsl:choose>
          <xsl:when test="@name">
            <xsl:value-of select="@name"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page"><xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="concat('#',generate-id())"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <input type="hidden" name="__token">
      <ixsl:attribute name="value">
        <xsl:value-of select="$tokenName"/>:<xsl:value-of select="@errorpage"/>:<ixsl:value-of select="pfx:getToken('{$tokenName}')"/>
      </ixsl:attribute>
    </input>
  </xsl:template>
  
  <xsl:template match="pfx:externalform">
  <form method="post">
    <ixsl:attribute name="action"><ixsl:value-of select="$__contextpath"/>/xml/deref</ixsl:attribute>
    <xsl:variable name="link">addallparams:<xsl:value-of select="@href"/></xsl:variable>
    <input type="hidden" name="link">
      <ixsl:attribute name="value"><xsl:value-of select="$link"/></ixsl:attribute>  
    </input>
    <ixsl:variable name="ts">
      <ixsl:value-of select="deref:getTimeStamp()"/>
    </ixsl:variable>
    <input type="hidden" name="__sign">
      <ixsl:attribute name="value">
        <ixsl:call-template name="__sign">
          <ixsl:with-param name="in"><xsl:value-of select="$link"/></ixsl:with-param>
          <ixsl:with-param name="ts" select="$ts"/>
        </ixsl:call-template>
      </ixsl:attribute>
    </input>
    <input type="hidden" name="__ts">
      <ixsl:attribute name="value">
        <ixsl:value-of select="$ts"/>
      </ixsl:attribute>
    </input>
    <xsl:apply-templates/>
  </form>
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xsl
End:
-->
