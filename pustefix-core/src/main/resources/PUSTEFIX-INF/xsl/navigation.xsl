<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>

  <!-- needed for the template below to work... ughh, bloody hack. -->
  <xsl:template match="pfx:invisible"/>
  <xsl:template match="pfx:active"/>
  <xsl:template match="pfx:normal"/>
  <xsl:template match="pfx:argument"/>
  <xsl:template match="pfx:altkey"/>
  
  <xsl:template match="pfx:command">

    <xsl:variable name="targetpage">
      <xsl:choose>
        <xsl:when test="ancestor::pfx:forminput[position()=1]/@send-to-page">
          <xsl:value-of select="ancestor::pfx:forminput[position()=1]/@send-to-page" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$page" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="fullname">
      <xsl:value-of select="text()" />
    </xsl:variable>


    <ixsl:call-template name="__formwarn">
      <ixsl:with-param name="targetpage">
        <xsl:value-of select="$targetpage" />
      </ixsl:with-param>
      <ixsl:with-param name="fullname">
        <xsl:value-of select="$fullname" />
      </ixsl:with-param>
    </ixsl:call-template>

    <ixsl:call-template name="__formwarn_command">
      <ixsl:with-param name="targetpage">
        <xsl:value-of select="$targetpage" />
      </ixsl:with-param>
      <ixsl:with-param name="fullname">
        <xsl:value-of select="$fullname" />
      </ixsl:with-param>
    </ixsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:anchor"/>
  <xsl:template match="pfx:host"/>

  <xsl:template match="pfx:visited">
    <xsl:param name="thepagename"/>
    <xsl:variable name="pagename_impl">
      <xsl:choose>
        <xsl:when test="@page">'<xsl:value-of select="@page"/>'</xsl:when>
        <xsl:when test="$thepagename">'<xsl:value-of select="$thepagename"/>'</xsl:when>
        <xsl:otherwise>$page</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <ixsl:if test="callback:isVisited($__context__, {$pagename_impl}) = 1">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:unvisited">
    <xsl:param name="thepagename"/>
    <xsl:variable name="pagename_impl">
      <xsl:choose>
        <xsl:when test="@page">'<xsl:value-of select="@page"/>'</xsl:when>
        <xsl:when test="$thepagename">'<xsl:value-of select="$thepagename"/>'</xsl:when>
        <xsl:otherwise>$page</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <ixsl:if test="callback:isVisited($__context__, {$pagename_impl}) = 0">
      <xsl:apply-templates/>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:button">
    <xsl:call-template name="pfx:button_impl">
      <xsl:with-param name="normal">
        <xsl:choose>
          <xsl:when test="./pfx:normal">
            <xsl:apply-templates select="./pfx:normal/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="active">
        <xsl:choose>
          <xsl:when test="./pfx:active">
            <xsl:apply-templates select="./pfx:active/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="./pfx:normal">
            <xsl:apply-templates select="./pfx:normal/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="invisible">
        <xsl:choose>
          <xsl:when test="./pfx:invisible">
            <xsl:apply-templates select="./pfx:invisible/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="./pfx:normal">
            <xsl:apply-templates select="./pfx:normal/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="./node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="normalclass"><xsl:value-of select="@normalclass"/></xsl:with-param>
      <xsl:with-param name="activeclass"><xsl:value-of select="@activeclass"/></xsl:with-param>
      <xsl:with-param name="invisibleclass"><xsl:value-of select="@invisibleclass"/></xsl:with-param>
      <xsl:with-param name="args" select="./pfx:argument"/>
      <xsl:with-param name="cmds" select="./pfx:command"/>
      <xsl:with-param name="anchors" select="./pfx:anchor"/>
      <xsl:with-param name="pageflow" select="@pageflow"/>
      <xsl:with-param name="startwithflow" select="@startwithflow"/>
      <xsl:with-param name="nodata" select="@nodata"/>
      <xsl:with-param name="buttpage" select="@page"/>
      <xsl:with-param name="frame" select="@frame"/>
      <xsl:with-param name="target" select="@target"/>
      <xsl:with-param name="mode" select="@mode"/>
      <xsl:with-param name="jumptopage" select="@jumptopage"/>
      <xsl:with-param name="jumptopageflow" select="@jumptopageflow"/>
      <xsl:with-param name="forcestop" select="@forcestop"/>
      <xsl:with-param name="action" select="@action"/>
      <xsl:with-param name="popup" select="@popup"/>
      <xsl:with-param name="popupwidth" select="@popupwidth"/>
      <xsl:with-param name="popupheight" select="@popupheight"/>
      <xsl:with-param name="popupfeatures" select="@popupfeatures"/>
      <xsl:with-param name="popupid" select="@popupid"/>
      <xsl:with-param name="altkey">
        <xsl:choose>
          <xsl:when test="./pfx:altkey">
            <xsl:apply-templates select="./pfx:altkey/node()">
              <xsl:with-param name="thepagename" select="@page"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="@altkey">
            <xsl:value-of select="@altkey"/>
          </xsl:when>
        </xsl:choose>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="pfx:url">
    <xsl:call-template name="pfx:button_impl">
      <xsl:with-param name="urlonly" select="'true'"/>
      <xsl:with-param name="args" select="./pfx:argument"/>
      <xsl:with-param name="cmds" select="./pfx:command"/>
      <xsl:with-param name="anchors" select="./pfx:anchor"/>
      <xsl:with-param name="pageflow" select="@pageflow"/>
      <xsl:with-param name="jumptopage" select="@jumptopage"/>
      <xsl:with-param name="jumptopageflow" select="@jumptopageflow"/>
      <xsl:with-param name="startwithflow" select="@startwithflow"/>
      <xsl:with-param name="forcestop" select="@forcestop"/>
      <xsl:with-param name="action" select="@action"/>
      <xsl:with-param name="nodata" select="@nodata"/>
      <xsl:with-param name="buttpage" select="@page"/>
      <xsl:with-param name="frame" select="@frame"/>
      <xsl:with-param name="target" select="@target"/>
      <xsl:with-param name="mode" select="@mode"/>
    </xsl:call-template>
    
  </xsl:template>

  <xsl:template name="pfx:button_impl">
    <xsl:param name="urlonly" select="'false'"/>
    <xsl:param name="omover"/>
    <xsl:param name="omout"/>
    <xsl:param name="buttpage"/>
    <xsl:param name="normal"/>
    <xsl:param name="active"/>
    <xsl:param name="invisible"/>
    <xsl:param name="normalclass"/>
    <xsl:param name="activeclass"/>
    <xsl:param name="invisibleclass"/>
    <xsl:param name="pageflow"/>
    <xsl:param name="jumptopage"/>
    <xsl:param name="jumptopageflow"/>
    <xsl:param name="startwithflow"/>
    <xsl:param name="frame"/>
    <xsl:param name="target"/>
    <xsl:param name="mode"/>
    <xsl:param name="forcestop"/>
    <xsl:param name="action"/>
    <xsl:param name="popup"/>
    <xsl:param name="popupwidth"/>
    <xsl:param name="popupheight"/>
    <xsl:param name="popupfeatures"/>
    <xsl:param name="popupid"/>
    <xsl:param name="altkey"/>
    <xsl:param name="nodata"/>
    <xsl:param name="args"/>
    <xsl:param name="cmds"/>
    <xsl:param name="anchors"/>
    <xsl:param name="frame_impl">
      <xsl:choose>
        <xsl:when test="$frame">
          <xsl:value-of select="$frame"/>
        </xsl:when>
        <xsl:when test="ancestor-or-self::pfx:frame[position()=2]/@name">
          <xsl:value-of select="ancestor-or-self::pfx:frame[position()=2]/@name"/>
        </xsl:when>
        <xsl:when test="/pfx:document/pfx:frameset or /pfx:document/html/pfx:frameset">_top</xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="target_impl">
      <xsl:choose>
        <xsl:when test="$target">
          <xsl:value-of select="$target"/>
        </xsl:when>
        <xsl:when test="$target = '_popup'">_blank</xsl:when>
        <xsl:when test="ancestor-or-self::pfx:frame[position()=2]/@name">_parent</xsl:when>
        <xsl:when test="ancestor-or-self::pfx:frame">_top</xsl:when>
      </xsl:choose>
    </xsl:param>
    <ixsl:variable name="page_{generate-id()}">
      <xsl:choose>
        <xsl:when test="string($buttpage) = '/'"><ixsl:value-of select="pfx:getHomePage()"/></xsl:when>
        <xsl:when test="not(string($buttpage) = '')"><xsl:value-of select="$buttpage"/></xsl:when>
        <xsl:when test="pfx:page"><xsl:apply-templates select="pfx:page/node()"/></xsl:when>
        <xsl:otherwise><ixsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose>
    </ixsl:variable>
    <xsl:variable name="mode_impl">
      <xsl:choose>
        <xsl:when test="not(string($mode) = '')"><xsl:value-of select="$mode"/></xsl:when>
        <xsl:when test="string($buttpage) = '' and not(pfx:page)">force</xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="fulllink">
      <ixsl:value-of select="$__contextpath"/>/<ixsl:value-of select="pfx:__omitPage($page_{generate-id()}, $lang, $tmpaltkey)"/><ixsl:value-of select="$__sessionIdPath"/>
      <ixsl:variable name="params">
      <xsl:if test="not($frame_impl='')">__frame=<xsl:value-of select="$frame_impl"/></xsl:if>
      <ixsl:if test="not($__lf = '') and pfx:__needsLastFlow($page_{generate-id()},$__lf)">&amp;__lf=<ixsl:value-of select="$__lf"/></ixsl:if>
      <xsl:if test="($args or $cmds) and not($nodata) and not($startwithflow = 'true')">&amp;__sendingdata=1</xsl:if>
      <xsl:for-each select="$args">&amp;<xsl:value-of select="./@name"/>=<ixsl:call-template name="__enc"><ixsl:with-param name="in"><xsl:apply-templates select="./node()"/></ixsl:with-param></ixsl:call-template></xsl:for-each>
      <xsl:if test="$jumptopage">&amp;__jumptopage=<xsl:value-of select="$jumptopage"/></xsl:if>
      <xsl:if test="$jumptopageflow">&amp;__jumptopageflow=<xsl:value-of select="$jumptopageflow"/></xsl:if>
      <xsl:if test="$pageflow">&amp;__pageflow=<xsl:value-of select="$pageflow"/></xsl:if>
      <xsl:if test="$startwithflow">&amp;__startwithflow=<xsl:value-of select="$startwithflow"/></xsl:if>
      <xsl:if test="$forcestop">&amp;__forcestop=<xsl:value-of select="$forcestop"/></xsl:if>
      <xsl:if test="$action">&amp;__action=<xsl:value-of select="$action"/></xsl:if>
      <xsl:for-each select="$cmds">&amp;__CMD[<xsl:choose><xsl:when test="./@page"><xsl:value-of select="./@page"/></xsl:when><xsl:otherwise><ixsl:value-of select="$page_{generate-id()}"/></xsl:otherwise></xsl:choose>]:<xsl:value-of select="./@name"/>=<xsl:apply-templates select="./node()"/></xsl:for-each>
      <xsl:for-each select="$anchors[@frame != '']">&amp;__anchor=<xsl:value-of select="@frame"/>|<xsl:apply-templates select="./node()"/></xsl:for-each>
      </ixsl:variable>
      <ixsl:value-of select="pfx:__addParams($params)"/>
      <xsl:if test="$anchors[not(@frame) or @frame = '']">#<xsl:apply-templates select="$anchors[not(@frame) or @frame = ''][1]/node()"/></xsl:if>
    </xsl:variable>
    <xsl:variable name="excluded_button_attrs">|action|activeclass|altkey|buttpage|forcestop|frame|invisibleclass|jumptopage|jumptopageflow|mode|nodata|normalclass|page|pageflow|popup|popupfeatures|popupheight|popupid|popupwidth|startwithflow|target|</xsl:variable>
    <xsl:variable name="excluded_button_span_attrs"><xsl:value-of select="$excluded_button_attrs"/>charset|type|name|href|hreflang|rel|rev|accesskey|shape|coords|tabindex|onfocus|onblur|</xsl:variable>
    <ixsl:if test="true()">
    <ixsl:variable name="tmpaltkey">
      <xsl:if test="$altkey"><xsl:copy-of select="$altkey"/></xsl:if>
    </ixsl:variable>
    <xsl:choose>
      <xsl:when test="string($urlonly) = 'true'">
        <xsl:copy-of select="$fulllink"/>
      </xsl:when>
      <xsl:otherwise>
        <ixsl:choose>
          <ixsl:when test="(callback:checkAuthorization($__context__,$page_{generate-id()}) = 3) or (callback:isAccessible($__context__, $__target_gen, $page_{generate-id()}) = 0) and not('{$mode_impl}' = 'force')">
            <span>
              <xsl:if test="name()='pfx:button'"><xsl:copy-of select="@*[not(contains($excluded_button_span_attrs,concat('|',name(),'|')))]"/></xsl:if>
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
          <xsl:if test="not($mode_impl = 'force')">
              <ixsl:when test="$page = $page_{generate-id()} and $pageAlternative = $tmpaltkey">
                <span>
                  <xsl:if test="name()='pfx:button'"><xsl:copy-of select="@*[not(contains($excluded_button_span_attrs,concat('|',name(),'|')))]"/></xsl:if>
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
              </ixsl:when>
          </xsl:if>
          <ixsl:otherwise>
                <a>
                  <xsl:if test="name()='pfx:button'"><xsl:copy-of select="@*[not(contains($excluded_button_attrs,concat('|',name(),'|')))]"/></xsl:if>
                  <xsl:if test="not($target_impl = '')">
                    <xsl:attribute name="target"><xsl:value-of select="$target_impl"/></xsl:attribute>
                  </xsl:if>
                  <xsl:attribute name="class">
                    <xsl:choose>
                      <xsl:when test="$normalclass = ''">core_button_normal</xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$normalclass" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:attribute>
                  <xsl:if test="$omover">
                    <xsl:attribute name="onmouseover"><xsl:value-of select="$omover"/></xsl:attribute></xsl:if>
                  <xsl:if test="$omout">
                      <xsl:attribute name="onmouseout"><xsl:value-of select="$omout"/></xsl:attribute></xsl:if>
                  <xsl:if test="@popup='true' or @target='_popup'">
                    <xsl:variable name="windowName">
                      <xsl:choose>
                        <xsl:when test="@popupid"><xsl:value-of select="@popupid"/></xsl:when>
                        <xsl:when test="@target"><xsl:value-of select="@target"/></xsl:when>
                        <xsl:otherwise>_blank</xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>                  
                    <xsl:variable name="windowFeatures">
                      <xsl:choose>
                        <xsl:when test="$popupfeatures">,'<xsl:value-of select="$popupfeatures"/>'</xsl:when>
                        <xsl:otherwise>
                          <xsl:text>,'toolbar=no,location=no,status=yes,menubar=no,scrollbars=yes,resizable=yes</xsl:text>
                          <xsl:if test="$popupwidth">,width=<xsl:value-of select="$popupwidth"/></xsl:if>
                          <xsl:if test="$popupheight">,height=<xsl:value-of select="$popupheight"/></xsl:if>
                          <xsl:text>,screenX=100,screenY=100'</xsl:text>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <ixsl:attribute name="onclick">return !window.open(this.href,'<xsl:value-of select="$windowName"/>'<xsl:value-of select="$windowFeatures"/>);</ixsl:attribute>
                  </xsl:if>
                  <ixsl:attribute name="href"><xsl:copy-of select="$fulllink"/></ixsl:attribute>
                  <xsl:copy-of select="$normal"/>
                </a>
          </ixsl:otherwise>
        </ixsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:page"/>

  <xsl:template match="pfx:elink">
    <xsl:choose>
      <xsl:when test="not(@href and ./pfx:host)">
        <xsl:choose>
          <xsl:when test="@href or ./pfx:host">
            <xsl:choose>
              <xsl:when test="not(contains(@href, '?'))">
                <xsl:variable name="href">
                  <xsl:if test="@href">
                    <xsl:value-of select="@href"/>
                  </xsl:if>
                  <xsl:if test="./pfx:host">
                    <xsl:apply-templates select="./pfx:host/node()"/>
                  </xsl:if>
                  <xsl:if test="./pfx:argument">
                    <xsl:text>?</xsl:text>
                    <xsl:for-each select="pfx:argument">
                      <xsl:value-of select="@name"/>=<xsl:apply-templates select="./node()"/>
                      <xsl:if test="following-sibling::pfx:argument">
                        <xsl:text>&amp;</xsl:text>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:if>
                </xsl:variable>
                <a>
                  <xsl:copy-of select="@*[not(starts-with(name(),'popup'))]"/>
                  <xsl:if test="@popup='true' or @target='_popup'">
                    <xsl:variable name="windowName">
                      <xsl:choose>
                        <xsl:when test="@popupid"><xsl:value-of select="@popupid"/></xsl:when>
                        <xsl:when test="@target"><xsl:value-of select="@target"/></xsl:when>
                        <xsl:otherwise>_blank</xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="windowFeatures">
                      <xsl:choose>
                        <xsl:when test="@popupfeatures">,'<xsl:value-of select="@popupfeatures"/>'</xsl:when>
                        <xsl:otherwise>
                          <xsl:text>,'toolbar=no,location=no,status=yes,menubar=no,scrollbars=yes,resizable=yes</xsl:text>
                          <xsl:if test="@popupwidth">,width=<xsl:value-of select="@popupwidth"/></xsl:if>
                          <xsl:if test="@popupheight">,height=<xsl:value-of select="@popupheight"/></xsl:if>
                          <xsl:text>,screenX=100,screenY=100'</xsl:text>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <xsl:attribute name="target"><xsl:value-of select="$windowName"/></xsl:attribute>
                    <ixsl:attribute name="onclick">return !window.open(this.href,'<xsl:value-of select="$windowName"/>'<xsl:value-of select="$windowFeatures"/>);</ixsl:attribute>
                  </xsl:if>
                  <ixsl:attribute name="href">
                    <ixsl:call-template name="__deref"><ixsl:with-param name="link"><xsl:copy-of select="$href"/></ixsl:with-param></ixsl:call-template>
                  </ixsl:attribute>
                  <xsl:apply-templates/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <img src="/modules/pustefix-core/img/error.gif"/><span class="core_xml_errorbox">Error: Do not use a questionmark in the href attribute !</span>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <img src="/modules/pustefix-core/img/error.gif"/><span class="core_xml_errorbox">Error: Need either @href or ./pfx:host child node</span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <img src="/modules/pustefix-core/img/error.gif"/><span class="core_xml_errorbox">Error: Need just one of @href or ./pfx:host child node - both were used</span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>

