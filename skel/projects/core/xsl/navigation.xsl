<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" xmlns:cus="http://www.schlund.de/pustefix/customize" version="1.0"> 

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
  
  
  <xsl:template match="pfx:invisible"/>
  <xsl:template match="pfx:argument"/>
  <xsl:template match="pfx:command"/>
  <xsl:template match="pfx:anchor"/>
  <xsl:template match="pfx:host">
    
  </xsl:template>
  
  <xsl:template match="pfx:button">
    
    
    <xsl:call-template name="pfx:button_impl">
      <xsl:with-param name="normal">
        <xsl:choose>
          <xsl:when test="./pfx:normal">
            <xsl:apply-templates select="./pfx:normal/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="active">
        <xsl:choose>
          <xsl:when test="./pfx:active">
            <xsl:apply-templates select="./pfx:active/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="invisible">
        <xsl:choose>
          <xsl:when test="./pfx:invisible">
            <xsl:apply-templates select="./pfx:invisible/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="normalclass"><xsl:value-of select="@normalclass"/></xsl:with-param>
      <xsl:with-param name="activeclass"><xsl:value-of select="@activeclass"/></xsl:with-param>
      <xsl:with-param name="invisibleclass"><xsl:value-of select="@invisibleclass"/></xsl:with-param>
      <xsl:with-param name="args" select="./pfx:argument"/>
      <xsl:with-param name="cmds" select="./pfx:command"/>
      <xsl:with-param name="anchors" select="./pfx:anchor"/>
      <xsl:with-param name="nodata" select="@nodata"/>
      <xsl:with-param name="buttpage" select="@page"/>
      <xsl:with-param name="frame" select="@frame"/>
      <xsl:with-param name="target" select="@target"/>
      <xsl:with-param name="mode" select="@mode"/>
      <xsl:with-param name="popupwidth" select="@popupwidth"/>
      <xsl:with-param name="popupheight" select="@popupheight"/>
      <xsl:with-param name="popupid" select="@popupid"/>
    </xsl:call-template>
  </xsl:template>

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
        top.__js_but["<xsl:value-of select="@name"/>"] =
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
      <xsl:with-param name="mode" select="@mode"/>
      <xsl:with-param name="popupwidth" select="@popupwidth"/>
      <xsl:with-param name="popupheight" select="@popupheight"/>
      <xsl:with-param name="popupid" select="@popupid"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="pfx:button_impl">
    
    <xsl:param name="omover"/>
    <xsl:param name="omout"/>
    <xsl:param name="buttpage"/>
    <xsl:param name="normal"/>
    <xsl:param name="active"/>
    <xsl:param name="invisible"/>
    <xsl:param name="normalclass"/>
    <xsl:param name="activeclass"/>
    <xsl:param name="invisibleclass"/>
    <xsl:param name="frame"/>
    <xsl:param name="target"/>
    <xsl:param name="mode"/>
    <xsl:param name="popupwidth"/>
    <xsl:param name="popupheight"/>
    <xsl:param name="popupid"/>
    <xsl:param name="nodata"/>
    <xsl:param name="args"/>
    <xsl:param name="cmds"/>
    <xsl:param name="anchors"/>
    <xsl:param name="buttpage_impl">
      <xsl:choose>
        <xsl:when test="not(string($buttpage) = '')">
          <xsl:value-of select="$buttpage"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$page"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="frame_impl">
      <xsl:choose>
        <xsl:when test="$frame">
          <xsl:value-of select="$frame"/>
        </xsl:when>  
        <xsl:when test="ancestor-or-self::pfx:frame[position()=2]/@name">
          <xsl:value-of select="ancestor-or-self::pfx:frame[position()=2]/@name"/>
        </xsl:when>
        <xsl:otherwise>_top</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="target_impl">
      <xsl:choose>
        <xsl:when test="$target">
          <xsl:value-of select="$target"/>
        </xsl:when>
        <xsl:when test="$target = '_popup'">_blank</xsl:when>
        <xsl:otherwise>_parent</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:variable name="thebuttpage" select="$navitree//page[@name = $buttpage_impl]"/>
    <ixsl:choose>
      
      <ixsl:when>
        <xsl:attribute name="test">/formresult/navigation<xsl:for-each select="$thebuttpage/ancestor-or-self::page">/page[@name='<xsl:value-of select="./@name"/>']</xsl:for-each>/@visible = '0'</xsl:attribute>
        <span>
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="$invisibleclass = ''">core_button_invisible</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$invisibleclass"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:copy-of select="$invisible"/>
        </span>
      </ixsl:when>
      <ixsl:otherwise>
        <xsl:choose>
          <xsl:when test="not($mode = 'force')                     and (($mode = 'desc' and $thebuttpage//page[@name=$page])                     or ($page = $buttpage_impl))">
            <span>
              <xsl:attribute name="class">
                <xsl:choose>
                  <xsl:when test="$activeclass = ''">core_button_active</xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$activeclass"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:copy-of select="$active"/>
            </span>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="thehandler" select="$thebuttpage/@handler"/>
            <xsl:variable name="thequery">__frame=<xsl:value-of select="$frame_impl"/></xsl:variable>
            <xsl:variable name="prelink">
              <xsl:value-of select="$thehandler"/>/<xsl:value-of select="$buttpage_impl"/>
            </xsl:variable>
            <xsl:variable name="postlink">
              <xsl:text>?</xsl:text><xsl:value-of select="$thequery"/>
            </xsl:variable>
            <a target="{$target_impl}">
              <xsl:attribute name="class">
                <xsl:choose>
                  <xsl:when test="$normalclass = ''">core_button_normal</xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$normalclass"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:if test="$omover">
                <xsl:attribute name="onmouseover"><xsl:value-of select="$omover"/></xsl:attribute></xsl:if>
              <xsl:if test="$omout">
                <xsl:attribute name="onmouseout"><xsl:value-of select="$omout"/></xsl:attribute></xsl:if>
              <xsl:if test="$target = '_popup'">
                <ixsl:attribute name="onclick">javascript:top.__js_popup('<xsl:value-of select="$prelink"/>;<ixsl:value-of select="$__sessid"/><xsl:value-of select="$postlink"/><xsl:if test="$args and not($nodata)">&amp;__sendingdata=1</xsl:if><xsl:for-each select="$args">&amp;<xsl:value-of select="./@name"/>=<xsl:apply-templates select="./node()"/></xsl:for-each><xsl:for-each select="$cmds">&amp;__CMD[<xsl:choose><xsl:when test="./@page"><xsl:value-of select="./@page"/></xsl:when><xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise></xsl:choose>]:<xsl:value-of select="./@name"/>:<xsl:apply-templates select="./node()"/>:=_</xsl:for-each><xsl:for-each select="$anchors">&amp;__anchor=<xsl:value-of select="@frame"/>|<xsl:apply-templates select="./node()"/></xsl:for-each>','<xsl:value-of select="$popupid"/>','<xsl:value-of select="$popupwidth"/>','<xsl:value-of select="$popupheight"/>');return(false);</ixsl:attribute>
              </xsl:if>
              <ixsl:attribute name="href"><xsl:value-of select="$prelink"/>;<ixsl:value-of select="$__sessid"/><xsl:value-of select="$postlink"/><xsl:if test="$args and not($nodata)">&amp;__sendingdata=1</xsl:if><xsl:for-each select="$args">&amp;<xsl:value-of select="./@name"/>=<xsl:apply-templates select="./node()"/></xsl:for-each><xsl:for-each select="$cmds">&amp;__CMD[<xsl:choose><xsl:when test="./@page"><xsl:value-of select="./@page"/></xsl:when><xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise></xsl:choose>]:<xsl:value-of select="./@name"/>:<xsl:apply-templates select="./node()"/>:=_</xsl:for-each><xsl:for-each select="$anchors">&amp;__anchor=<xsl:value-of select="@frame"/>|<xsl:apply-templates select="./node()"/></xsl:for-each></ixsl:attribute>
              <xsl:copy-of select="$normal"/>
            </a>
          </xsl:otherwise> 
        </xsl:choose>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>

  <xsl:template match="pfx:elink"> 
    
    
    <xsl:choose>
      <xsl:when test="not(@href and ./pfx:host)">
        <xsl:choose>
          <xsl:when test="@href or ./pfx:host">
            <xsl:choose>
              <xsl:when test="not(contains(@href, '?'))">               
                <xsl:variable name="href">
                  <xsl:if test="@href">
                    <xsl:value-of select="concat('/xml/deref?link=',@href)"/>    
                  </xsl:if>            
                  <xsl:if test="./pfx:host">
                    <xsl:text>/xml/deref?link=</xsl:text>
                    <xsl:apply-templates select="./pfx:host/node()"/>   
                  </xsl:if>
                  <xsl:if test="./pfx:argument">
                    <xsl:text>%3f</xsl:text>
                    <xsl:for-each select="pfx:argument">
                      <xsl:value-of select="@name"/>%3d<xsl:apply-templates select="./node()"/>
                      <xsl:if test="following-sibling::pfx:argument">
                        <xsl:text>%26</xsl:text>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:if>
                </xsl:variable>               
                <a>
                  <xsl:copy-of select="@*"/>
                  <xsl:if test="@target = '_popup'">
                    <xsl:attribute name="target">_blank</xsl:attribute>
                    <ixsl:attribute name="onclick">
                      <xsl:text>javascript:top.__js_popup('</xsl:text>
                      <xsl:copy-of select="$href"/>
                      <xsl:text>','</xsl:text>
                      <xsl:value-of select="@popupid"/>
                      <xsl:text>','</xsl:text>
                      <xsl:value-of select="@popupwidth"/>
                      <xsl:text>','</xsl:text>
                      <xsl:value-of select="@popupheight"/>
                      <xsl:text>');return(false);</xsl:text>
                    </ixsl:attribute>
                  </xsl:if>
                  <ixsl:attribute name="href">
                    <xsl:copy-of select="$href"/>
                  </ixsl:attribute>
                  <xsl:apply-templates/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <img src="/core/img/error.gif"/><span class="core_xml_errorbox">Error: Do not use a questionmark in the href attribute !</span>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <img src="/core/img/error.gif"/><span class="core_xml_errorbox">Error: Need either @href or ./pfx:host child node</span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <img src="/core/img/error.gif"/><span class="core_xml_errorbox">Error: Need just one of @href or ./pfx:host child node - both were used</span>
      </xsl:otherwise>
    </xsl:choose>
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
          <a><ixsl:attribute name="href"><xsl:value-of select="concat($thehandler, '/', $page)"/>;<ixsl:value-of select="$__sessid"/>?__currentindex[<xsl:value-of select="$page"/>]=<ixsl:value-of select="/formresult/iwrappergroups/@currentindex - 1"/>&amp;__frame=<xsl:value-of select="$frame_impl"/></ixsl:attribute><xsl:apply-templates select="./pfx:active/node()"/></a>
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
</xsl:stylesheet>