<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
		xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <!-- ************************************************************************************************************* -->
  <!-- ****************************   NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE ************************************** -->
  <!-- ************************************************************************************************************* -->
  <!-- All the following templates are DEPRECATED and may be removed any time in the future without -->
  <!-- further warning. Please don't use them in new code -->

  <xsl:import href="core/xsl/deprecated/deprecated_metatags.xsl"/>
  
  <!-- From MASTER.XSL -->

  <xsl:template match="pfx:head">
    <head>
      <script language="JavaScript" src="/core/script/baselib.js" type="text/javascript"/>
      <xsl:apply-templates select="./node()"/>
    </head>
  </xsl:template>
  
  <!-- From NAVIGATION.XSL -->

  <xsl:template match="pfx:jsbutton">
    <xsl:param name="act_src">
      <xsl:choose>
        <xsl:when test="@active">
          <xsl:value-of select="@active"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@omover"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="inv_src">
      <xsl:choose>
        <xsl:when test="@invisible">
          <xsl:value-of select="@invisible"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@omout"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <script language="JavaScript">
      <ixsl:comment>
        top.__js_allButtons["<xsl:value-of select="@name"/>"] =
        new top.__js_button("<xsl:value-of select="@omout"/>","<xsl:value-of select="@omover"/>");
        //</ixsl:comment>
    </script>
    <xsl:call-template name="pfx:button_impl">
      <xsl:with-param name="normal">
        <img border="0">
          <xsl:attribute name="src"><xsl:value-of select="@omout"/></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
          <xsl:call-template name="pfx:image_geom_impl">
            <xsl:with-param name="src"><xsl:value-of select="@omout"/></xsl:with-param>
          </xsl:call-template>
        </img>
      </xsl:with-param>
      <xsl:with-param name="active">
        <img border="0" alt="{@alt}">
          <xsl:attribute name="src"><xsl:value-of select="$act_src"/></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
          <xsl:call-template name="pfx:image_geom_impl">
            <xsl:with-param name="src"><xsl:value-of select="$act_src"/></xsl:with-param>
          </xsl:call-template>
        </img>
      </xsl:with-param>
      <xsl:with-param name="invisible">
        <img border="0" alt="{@alt}">
          <xsl:attribute name="src"><xsl:value-of select="$inv_src"/></xsl:attribute>
          <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
          <xsl:call-template name="pfx:image_geom_impl">
            <xsl:with-param name="src"><xsl:value-of select="$inv_src"/></xsl:with-param>
          </xsl:call-template>
        </img>
      </xsl:with-param>
      <xsl:with-param name="accesskey" select="@accesskey"/>
      <xsl:with-param name="normalclass"><xsl:value-of select="@normalclass"/></xsl:with-param>
      <xsl:with-param name="activeclass"><xsl:value-of select="@activeclass"/></xsl:with-param>
      <xsl:with-param name="invisibleclass"><xsl:value-of select="@invisibleclass"/></xsl:with-param>
      <xsl:with-param name="omover">top.__js_moveover(document,'<xsl:value-of select="@name"/>'); return true</xsl:with-param>
      <xsl:with-param name="omout">top.__js_moveout(document,'<xsl:value-of select="@name"/>')</xsl:with-param>
      <xsl:with-param name="args" select="./pfx:argument"/>
      <xsl:with-param name="cmds" select="./pfx:command"/>
      <xsl:with-param name="anchors" select="./pfx:anchor"/>
      <xsl:with-param name="nodata" select="@nodata"/>
      <xsl:with-param name="buttpage" select="@page"/>
      <xsl:with-param name="frame" select="@frame"/>
      <xsl:with-param name="target" select="@target"/>
      <xsl:with-param name="pageflow" select="@pageflow"/>
      <xsl:with-param name="jumptopage" select="@jumptopage"/>
      <xsl:with-param name="jumptopageflow" select="@jumptopageflow"/>
      <xsl:with-param name="startwithflow" select="@startwithflow"/>
      <xsl:with-param name="mode" select="@mode"/>
      <xsl:with-param name="forcestop" select="@forcestop"/>
      <xsl:with-param name="popup" select="@popup"/>
      <xsl:with-param name="popupwidth" select="@popupwidth"/>
      <xsl:with-param name="popupheight" select="@popupheight"/>
      <xsl:with-param name="popupfeatures" select="@popupfeatures"/>
      <xsl:with-param name="popupid" select="@popupid"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:wizardnavigation">
    <ixsl:choose>
      <ixsl:when test="/formresult/iwrappergroups/@lastindex = 0">
        <xsl:apply-templates select="./pfx:flat/node()"/>
      </ixsl:when>
      <ixsl:otherwise>
        <xsl:apply-templates select="./pfx:group/node()"/>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>

  <xsl:template match="pfx:wizardprevious">
    <xsl:variable name="frame_impl">
      <xsl:choose>
        <xsl:when test="ancestor-or-self::pfx:frame[position()=1]/@name">
          <xsl:value-of select="ancestor-or-self::pfx:frame[position()=1]/@name"/>
        </xsl:when>
        <xsl:otherwise>_top</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thehandler" select="$navitree//page[@name=$page]/@handler"/>
    <ixsl:choose>
      <ixsl:when test="/formresult/iwrappergroups/@currentindex = 0">
        <span>
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="not(@wizprev_inactive)">core_wizprev_inactive</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@wizprev_inactive"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:apply-templates select="./pfx:inactive/node()"/>
        </span>
      </ixsl:when>
      <ixsl:otherwise>
        <span>
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="not(@wizprev_active)">core_wizprev_active</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@wizprev_active"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <a><ixsl:attribute name="href"><ixsl:value-of select="$__contextpath"/><xsl:value-of select="concat($thehandler, '/', $page)"/>;<ixsl:value-of select="$__sessid"/>?__currentindex[<xsl:value-of select="$page"/>]=<ixsl:value-of select="/formresult/iwrappergroups/@currentindex - 1"/>&amp;__frame=<xsl:value-of select="$frame_impl"/></ixsl:attribute><xsl:apply-templates select="./pfx:active/node()"/></a>
        </span>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>

  <xsl:template match="pfx:wizardnext">
    <ixsl:choose>
      <ixsl:when test="/formresult/iwrappergroups/@currentindex = /formresult/iwrappergroups/@lastindex">
        <xsl:apply-templates select="./pfx:inactive/node()"/>
      </ixsl:when>
      <ixsl:otherwise>
        <xsl:apply-templates select="./pfx:active/node()"/>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>

  <xsl:template match="pfx:wizardfinish">
    <ixsl:choose>
      <ixsl:when test="/formresult/iwrappergroups/@currentindex = /formresult/iwrappergroups/@lastindex">
        <xsl:apply-templates select="./pfx:active/node()"/>
      </ixsl:when>
      <ixsl:otherwise>
        <xsl:apply-templates select="./pfx:inactive/node()"/>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>


  <!-- From FORMINPUT.XSL -->

  <xsl:template match="pfx:label" name="pfx:label">
    <xsl:param name="name"><xsl:value-of select="@name"/></xsl:param>
    <ixsl:if test="1 = 1"> <!-- make sure to have a local scope --> 
      <ixsl:variable name="pfx_name"><xsl:choose>
        <xsl:when test="$name"><xsl:value-of select="$name"/></xsl:when>
        <xsl:when test="./pfx:name"><xsl:apply-templates select="./pfx:name/node()"/></xsl:when>
      </xsl:choose></ixsl:variable>
      <ixsl:choose>
        <ixsl:when test="/formresult/formerrors/error[@name=string($pfx_name)]">
          <xsl:if test="@showimage != 'false'">
            <xsl:call-template name="pfx:include">
              <xsl:with-param name="href">common/txt/general.xml</xsl:with-param>
              <xsl:with-param name="part">error_icon</xsl:with-param>
            </xsl:call-template>
          </xsl:if>
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
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:dynoptions">
    <!-- $pfx_name will be set in the parent select -->
    <ixsl:for-each select="{@optionpath}/option">
      <ixsl:variable name="pfx_value">
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
        <ixsl:copy-of select="./@*[name()!= 'default']"/>
        <ixsl:if test="/formresult/formvalues/param[@name=string($pfx_name)]/text() = string($pfx_value) or
          (@default = 'true' and not(/formresult/formvalues/param[@name=string($pfx_name)]))">
          <ixsl:attribute name="selected">selected</ixsl:attribute>
        </ixsl:if>
        <ixsl:apply-templates select="./node()"/>
      </option>
    </ixsl:for-each>
  </xsl:template>

  <xsl:template match="pfx:xoutp">
    <ixsl:value-of select="/formresult/formvalues/param[@name='{@name}']"/>
  </xsl:template>
  
  <xsl:template match="pfx:xoutperror">
    <ixsl:apply-templates select="/formresult/formerrors/error[@name='{@name}']"/>
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

  <xsl:template match="pfx:paramref">
  </xsl:template>
  
  <xsl:template match="pfx:pagemessages">
    <ixsl:if test="/formresult/pagemessages/message">
      <table id="core_pagemessages_box" border="0" cellpadding="0" cellspacing="0">
        <ixsl:for-each select="/formresult/pagemessages/message">
          <ixsl:sort select="@level"/>
          <tr>
            <td>
              <ixsl:attribute name="class">core_pagemessage_header_<ixsl:value-of select="@level"/></ixsl:attribute>
              <ixsl:call-template name="pfx:include">
                <ixsl:with-param name="href" select="'common/dyntxt/statusmessages-core-merged.xml'" />
                <ixsl:with-param name="part" select="concat('pfixcore.pagemessages.level.', @level)"/>
              </ixsl:call-template>
            </td>
            <td>
              <ixsl:attribute name="class">core_pagemessage_<ixsl:value-of select="@level"/></ixsl:attribute>
              <ixsl:apply-templates/>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:errorbox">
    <xsl:param name="paramref" select="./pfx:paramref"></xsl:param>
    <ixsl:if test="/formresult/formerrors/node()">
      <table class="core_errorbox_table">
        <xsl:if test="@width"><xsl:copy-of select="@width"/></xsl:if>
	<xsl:choose>
	  <xsl:when test="not($paramref)">
	    <xsl:for-each select="//pfx:label">
	      <xsl:variable name="name"  select="./@name"/>
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

</xsl:stylesheet>