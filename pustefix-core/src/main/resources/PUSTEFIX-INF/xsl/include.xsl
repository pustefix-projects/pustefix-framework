<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:include="xalan://de.schlund.pfixxml.IncludeDocumentExtensionSaxon1"
                xmlns:image="xalan://de.schlund.pfixxml.ImageThemedSrcSaxon1"
                xmlns:geometry="xalan://de.schlund.pfixxml.ImageGeometry"
                xmlns:func="http://exslt.org/functions"
                exclude-result-prefixes="include image geometry">

  <!-- The needed parameters must be set in the including stylesheet! -->

  <!-- 
    ##########################################
    # WARNING:                               #
    # All changes within this file also have #
    # to be applied to include_xslt2.xsl     #
    ##########################################
  -->

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>

  <xsl:param name="__editmode"/>
  <xsl:param name="__sessionId"/>
  <xsl:param name="__sessionIdPath"/>
  <xsl:param name="__target_gen"/>
  <xsl:param name="__target_key"/>
  <xsl:param name="__editor_url"/>
  <xsl:param name="__editor_include_parts_editable_by_default"/>
  <xsl:param name="__application_url"/>
  <xsl:param name="themes"/>
  <xsl:param name="prohibitEdit">no</xsl:param>
  <xsl:param name="__defining_module">WEBAPP</xsl:param>

  <xsl:template match="pfx:langselect">
    <xsl:param name="__env"/>
    <xsl:choose>
      <xsl:when test="not($__target_key = '__NONE__')">
        <xsl:choose>
          <xsl:when test="not(./pfx:lang[not(@name = 'default')]) and ./pfx:lang[@name = 'default']">
            <xsl:apply-templates select="./pfx:lang[@name = 'default']/node()">
              <xsl:with-param name="__env" select="$__env"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <ixsl:choose>
              <xsl:for-each select="./pfx:lang[not(@name = 'default') and string-length(@name) > 0 and
                                    not(substring(@name, string-length(@name)) = '*')]">
                <ixsl:when test="$lang = '{./@name}'">
                  <xsl:apply-templates select="./node()">
                    <xsl:with-param name="__env" select="$__env"/>
                  </xsl:apply-templates>
                </ixsl:when>
              </xsl:for-each>
              <xsl:for-each select="./pfx:lang[not(@name = 'default') and string-length(@name) > 1 and
                                    substring(@name, string-length(@name)) = '*']">
                <ixsl:when test="starts-with($lang, '{substring(@name, 0, string-length(@name))}')">
                  <xsl:apply-templates select="./node()">
                    <xsl:with-param name="__env" select="$__env"/>
                  </xsl:apply-templates>
                </ixsl:when>
              </xsl:for-each>
              <xsl:choose>
                <xsl:when test="./pfx:lang[@name = 'default']">
                  <ixsl:otherwise>
                    <xsl:apply-templates select="./pfx:lang[@name = 'default']/node()">
                      <xsl:with-param name="__env" select="$__env"/>
                    </xsl:apply-templates>
                  </ixsl:otherwise>
                </xsl:when>
                <xsl:otherwise>
                  <ixsl:otherwise>
                    <span>
                      <div style="width: 100px; align: center; color:white; background-color:black;">
                        <img src="{{$__contextpath}}/modules/pustefix-core/img/warning2.png"/><br/>
                        <span style="font-size: 8px; font-family: verdana,arial,helvetica,sans;"> No content for [<xsl:value-of select="$lang"/>]</span>
                      </div>
                    </span>
                  </ixsl:otherwise>
                </xsl:otherwise>
              </xsl:choose>
            </ixsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="langnodes" select="./pfx:lang[@name = $lang]"/>
        <xsl:variable name="deflangnodes" select="./pfx:lang[@name = 'default']"/>
        <xsl:variable name="commonlangnodes"
                      select="./pfx:lang[string-length(@name) > 1 and
                              substring(@name, string-length(@name)) = '*' and
                              starts-with($lang, substring(@name, 0, string-length(@name)))]"/>
        <xsl:choose>
          <xsl:when test="$langnodes">
            <xsl:apply-templates select="$langnodes/node()">
              <xsl:with-param name="__env" select="$__env"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$commonlangnodes">
            <xsl:apply-templates select="$commonlangnodes/node()">
              <xsl:with-param name="__env" select="$__env"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$deflangnodes">
            <xsl:apply-templates select="$deflangnodes/node()">
              <xsl:with-param name="__env" select="$__env"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <span>
              <div style="width: 100px; align: center; color:white; background-color:black;">
                <img src="{$__contextpath}/modules/pustefix-core/img/warning2.png"/><br/>
                <span style="font-size: 8px; font-family: verdana,arial,helvetica,sans;"> No content for [<xsl:value-of select="$lang"/>]</span>
              </div>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="pfx:themeselect" name="recurse_themeselect">
    <xsl:param name="themestr" select="$themes"/>
    <xsl:param name="allthemes" select="./pfx:theme"/>
    <xsl:variable name="curr">
      <xsl:choose>
        <xsl:when test="not(contains($themestr, ' '))"><xsl:value-of select="$themestr"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="substring-before($themestr, ' ')"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="rest" select="substring-after($themestr, ' ')"/>
    <xsl:choose>
      <xsl:when test="$allthemes[@name = $curr]">
        <xsl:apply-templates select="$allthemes[@name = $curr]/node()"/>
      </xsl:when>
      <xsl:when test="not($rest = '')">
        <xsl:call-template name="recurse_themeselect">
          <xsl:with-param name="allthemes" select="$allthemes"/>
          <xsl:with-param name="themestr" select="$rest"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <span>
          <div style="width: 100px; align: center; color:white; background-color:black;">
            <img src="{{$__contextpath}}/modules/pustefix-core/img/warning2.png">
              <xsl:if test="$__target_key = '__NONE__'">
                <xsl:attribute name="src"><xsl:value-of select="$__contextpath"/>/modules/pustefix-core/img/warning2.png</xsl:attribute>
              </xsl:if>
            </img>
            <br/>
            <span style="font-size: 8px; font-family: verdana,arial,helvetica,sans;"> No content for [<xsl:value-of select="$themes"/>]</span>
          </div>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="pfx:attribute">
    <ixsl:attribute name="{@name}">
      <!-- this stupid construct makes sure that only text is copied into the attribute -->
      <ixsl:variable name="tmp"><xsl:apply-templates/></ixsl:variable>
      <ixsl:value-of select="$tmp"/>
    </ixsl:attribute>
  </xsl:template>

  <xsl:template match="pfx:argref">
    <xsl:param name="__env"/>
    <xsl:variable name="pos">
      <xsl:choose>
        <xsl:when test="@pos"><xsl:value-of select="@pos"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="args" select="$__env/pfx:arg[position() = $pos]/@value"/>
    <xsl:choose>
      <xsl:when test="$args"><xsl:value-of select="$args"/></xsl:when>
      <xsl:otherwise><b>[?]</b></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="pfx:missinc">
    <xsl:param name="part"/>
    <xsl:param name="href"/>
    <xsl:param name="module"/>
    <xsl:variable name="thetext">Missing include: '<xsl:value-of select="$part"/>' in resource '<xsl:value-of select="$href"/>'</xsl:variable>
    <img src="{{$__contextpath}}/modules/pustefix-core/img/warning.gif">
      <xsl:if test="$__target_key = '__NONE__'">
        <xsl:attribute name="src"><xsl:value-of select="$__contextpath"/>/modules/pustefix-core/img/warning.gif</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="alt"><xsl:value-of select="$thetext"/></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="$thetext"/></xsl:attribute>
    </img>
    <xsl:message>*** Include not found:
      TargetKey = <xsl:value-of select="$__target_key"/> 
      Resource = <xsl:value-of select="$href"/>
      Part = <xsl:value-of select="$part"/> ***</xsl:message>
  </xsl:template>

  <xsl:template match="pfx:include" name="pfx:include">
    <xsl:param name="__env"/>
    <xsl:param name="computed_inc">false</xsl:param>
    <xsl:param name="parent_part"><xsl:value-of select="ancestor::part[position() = 1]/@name"/></xsl:param>
    <xsl:param name="parent_theme"><xsl:value-of select="ancestor::theme[position() = 1]/@name"/></xsl:param>
    <xsl:param name="noerror"><xsl:value-of select="@noerror"/></xsl:param>
    <xsl:param name="noedit"><xsl:value-of select="@noedit"/></xsl:param>
    <xsl:param name="part"><xsl:value-of select="@part"/></xsl:param>
    <xsl:param name="href"><xsl:value-of select="@href"/></xsl:param>
    <xsl:param name="module"><xsl:value-of select="@module"/></xsl:param>
    <xsl:param name="search"><xsl:value-of select="@search"/></xsl:param>
    <xsl:variable name="module_name">
      <xsl:choose>
        <xsl:when test="@module='PAGEDEF' or @module='pagedef'">
          <xsl:value-of select="$__defining_module"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$module"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="href_int">
      <xsl:if test="$href">
        <xsl:choose>
          <xsl:when test="starts-with($href, '/')">
            <xsl:value-of select="substring-after($href, '/')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="realpath">
      <xsl:choose>
        <xsl:when test="not(string($href_int) = '')">
          <xsl:value-of select="string($href_int)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="include:getRelativePathFromSystemId()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($part) = ''">
        <b>[Error: &lt;pfx:include&gt; needs "part" attribute]</b>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="incnodes"
                      select="include:get(string($realpath), string($part),
                              $__target_gen, string($__target_key),
                              string($parent_part), string($parent_theme), $computed_inc, $module_name, $search, $tenant, $lang)"/>
        <xsl:variable name="__resolveduri"><xsl:value-of select="include:getResolvedURI()"/></xsl:variable>
        <!-- Start image of edited region -->
        <xsl:choose>
          <xsl:when test="$noedit = 'true'"/> <!-- Do NOTHING! -->
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode='admin'">
              <span class="pfx_inc_start"/>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key = '__NONE__' and $__editmode = 'admin'">
            <span class="pfx_inc_start"/>
          </xsl:when>
        </xsl:choose>        
        <!-- -->
        <xsl:variable name="used_theme">
          <xsl:choose>
            <xsl:when test="$incnodes and $incnodes[name() = 'theme' or name() = 'missing']">
              <xsl:value-of select="$incnodes/@name"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:message terminate="yes">
                Error when calling extension function 'include:get' => Didn't get a valid nodeset.
              </xsl:message>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$incnodes and $incnodes[name() = 'theme']">
            <xsl:apply-templates select="$incnodes/node()">
              <xsl:with-param name="__env" select="."/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="not($noerror = 'true')">
            <xsl:call-template name="pfx:missinc">
              <xsl:with-param name="href" select="include:getResolvedURI()"/>
              <xsl:with-param name="module" select="$module_name"/>
              <xsl:with-param name="part" select="$part"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        <!-- ===================================================== -->
        <xsl:choose>
          <xsl:when test="$noedit = 'true'"/> <!-- Do NOTHING! -->
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode = 'admin'">
              <xsl:choose>
                <xsl:when test="$incnodes/parent::part/@editable='true'">
                  <xsl:call-template name="pfx:include_internal_render_edit">
                    <xsl:with-param name="part" select="$part"/>
                    <xsl:with-param name="theme" select="$used_theme"/>
                    <xsl:with-param name="path" select="$realpath"/>
                    <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                    <xsl:with-param name="search" select="$search"/>
                    <xsl:with-param name="module" select="$module_name"/>
                    <xsl:with-param name="incnodes" select="$incnodes"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:when test="$incnodes/parent::part/@editable='false'">
                  <xsl:call-template name="pfx:include_internal_render_edit">
                    <xsl:with-param name="part" select="$part"/>
                    <xsl:with-param name="theme" select="$used_theme"/>
                    <xsl:with-param name="path" select="$realpath"/>
                    <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                    <xsl:with-param name="search" select="$search"/>
                    <xsl:with-param name="module" select="$module_name"/>
                    <xsl:with-param name="editable">false</xsl:with-param>
                    <xsl:with-param name="incnodes" select="$incnodes"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <ixsl:choose>
                    <ixsl:when test="$__editor_include_parts_editable_by_default='true'">
                      <xsl:call-template name="pfx:include_internal_render_edit">
                        <xsl:with-param name="part" select="$part"/>
                        <xsl:with-param name="theme" select="$used_theme"/>
                        <xsl:with-param name="path" select="$realpath"/>
                        <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                        <xsl:with-param name="search" select="$search"/>
                        <xsl:with-param name="module" select="$module_name"/>
                        <xsl:with-param name="incnodes" select="$incnodes"/>
                      </xsl:call-template>
                    </ixsl:when>
                    <ixsl:otherwise>
                      <xsl:call-template name="pfx:include_internal_render_edit">
                        <xsl:with-param name="part" select="$part"/>
                        <xsl:with-param name="theme" select="$used_theme"/>
                        <xsl:with-param name="path" select="$realpath"/>
                        <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                        <xsl:with-param name="search" select="$search"/>
                        <xsl:with-param name="module" select="$module_name"/>
                        <xsl:with-param name="editable">false</xsl:with-param>
                        <xsl:with-param name="incnodes" select="$incnodes"/>
                      </xsl:call-template>
                    </ixsl:otherwise>
                  </ixsl:choose>
                </xsl:otherwise>
              </xsl:choose>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key='__NONE__' and $__editmode = 'admin'">
            <xsl:choose>
              <xsl:when test="$incnodes/parent::part/@editable='true' or (not($incnodes/parent::part/@editable='false') and $__editor_include_parts_editable_by_default='true')">
                <xsl:call-template name="pfx:include_internal_render_edit">
                  <xsl:with-param name="part" select="$part"/>
                  <xsl:with-param name="theme" select="$used_theme"/>
                  <xsl:with-param name="path" select="$realpath"/>
                  <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                  <xsl:with-param name="search" select="$search"/>
                  <xsl:with-param name="module" select="$module_name"/>
                  <xsl:with-param name="incnodes" select="$incnodes"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="pfx:include_internal_render_edit">
                  <xsl:with-param name="part" select="$part"/>
                  <xsl:with-param name="theme" select="$used_theme"/>
                  <xsl:with-param name="path" select="$realpath"/>
                  <xsl:with-param name="resolved_uri" select="$__resolveduri"/>
                  <xsl:with-param name="search" select="$search"/>
                  <xsl:with-param name="module" select="$module_name"/>
                  <xsl:with-param name="editable">false</xsl:with-param>
                  <xsl:with-param name="incnodes" select="$incnodes"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pfx:include_internal_render_edit">
    <xsl:param name="part"/>
    <xsl:param name="theme"/>
    <xsl:param name="path"/>
    <xsl:param name="resolved_uri"/>
    <xsl:param name="search"/>
    <xsl:param name="module"/>
    <xsl:param name="editable">true</xsl:param>
    <xsl:param name="incnodes"/>
    <xsl:variable name="resolved_module">
      <xsl:choose>
        <xsl:when test="starts-with($resolved_uri,'module://')"><xsl:value-of select="substring-before(substring-after($resolved_uri,'module://'),'/')"/></xsl:when>
        <xsl:otherwise>webapp</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="requested_module">
      <xsl:choose>
        <xsl:when test="not($module = '')">
          <xsl:choose>
            <xsl:when test="$module='WEBAPP' or $module='webapp'"></xsl:when>
            <xsl:otherwise><xsl:value-of select="$module"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="starts-with(include:getSystemId(),'module://')">
          <xsl:value-of select="substring-before(substring-after(include:getSystemId(),'//'),'/')"/>  
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="classes">
      <xsl:choose>
        <xsl:when test="$editable='true'">pfx_inc_end</xsl:when>
        <xsl:otherwise>pfx_inc_end pfx_inc_ro</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="nonempty" select="number(boolean($incnodes/* or $incnodes/text()[normalize-space(.)]))"/> 
    <xsl:variable name="othermodule">
      <xsl:choose>
        <xsl:when test="starts-with(include:getSystemId(),concat('module://',$resolved_module,'/'))">0</xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$search='dynamic'">
        <xsl:choose>
          <xsl:when test="$__target_key='__NONE__'">
            <span class="{$classes}" title="{pfx:getDynIncInfo($part,$theme,$path,$resolved_module,$requested_module,$tenant,$lang)}|{$nonempty}|{$othermodule}"/>
          </xsl:when>
          <xsl:otherwise>
            <span class="{$classes}" title="{{pfx:getDynIncInfo('{$part}','{$theme}','{$path}','{$resolved_module}','{$requested_module}','{$tenant}','{$lang}')}}|{$nonempty}|{$othermodule}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <span class="{$classes}" title="{$part}|{$theme}|{$path}|{$resolved_module}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="pfx:include[@level='runtime']">
    <ixsl:call-template name="pfx:include">
      <xsl:if test="@noerror">
        <ixsl:with-param name="noerror"><xsl:value-of select="@noerror"/></ixsl:with-param>
      </xsl:if>
      <xsl:if test="@noedit">
        <ixsl:with-param name="noedit"><xsl:value-of select="@noedit"/></ixsl:with-param>
      </xsl:if>
      <ixsl:with-param name="part">
        <xsl:choose>
          <xsl:when test="pfx:part">
            <xsl:apply-templates select="pfx:part/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@part"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="href">
        <xsl:choose>
          <xsl:when test="pfx:href">
            <xsl:apply-templates select="pfx:href/node()"/>
          </xsl:when>
          <xsl:when test="@href">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getRelativePathFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="module">
        <xsl:choose>
          <xsl:when test="@module">
            <xsl:value-of select="@module"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getModuleFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <xsl:if test="@search">
        <ixsl:with-param name="search"><xsl:value-of select="@search"/></ixsl:with-param>
      </xsl:if>
    </ixsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:checkinclude">
    <xsl:param name="part"><xsl:value-of select="@part"/></xsl:param>
    <xsl:param name="href"><xsl:value-of select="@href"/></xsl:param>
    <xsl:param name="module"><xsl:value-of select="@module"/></xsl:param>
    <xsl:param name="search"><xsl:value-of select="@search"/></xsl:param>
    <xsl:variable name="module_name">
      <xsl:choose>
        <xsl:when test="@module='PAGEDEF' or @module='pagedef'">
          <xsl:value-of select="$__defining_module"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$module"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="realpath">
      <xsl:choose>
        <xsl:when test="not($href='')">
          <xsl:value-of select="$href"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="include:getRelativePathFromSystemId()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="include:exists($realpath, $part, $__target_gen, $__target_key, $module, $search, $tenant, $lang)">
      <xsl:apply-templates/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:checkinclude[@level='runtime']">
    <ixsl:if test="true()">
      <ixsl:variable name="_href_">
        <xsl:choose>
          <xsl:when test="pfx:href">
            <xsl:apply-templates select="pfx:href/node()"/>
          </xsl:when>
          <xsl:when test="@href">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getRelativePathFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:variable name="_part_">
        <xsl:choose>
          <xsl:when test="pfx:part">
            <xsl:apply-templates select="pfx:part/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@part"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:variable name="_module_">
        <xsl:choose>
          <xsl:when test="@module">
            <xsl:value-of select="module"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getModuleFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:if test="pfx:checkInclude($_href_, $_part_, $_module_, '{@search}')">
        <xsl:apply-templates/>
      </ixsl:if>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="pfx:checknoinclude">
    <xsl:param name="part"><xsl:value-of select="@part"/></xsl:param>
    <xsl:param name="href"><xsl:value-of select="@href"/></xsl:param>
    <xsl:param name="module"><xsl:value-of select="@module"/></xsl:param>
    <xsl:param name="search"><xsl:value-of select="@search"/></xsl:param>
    <xsl:variable name="module_name">
      <xsl:choose>
        <xsl:when test="@module='PAGEDEF' or @module='pagedef'">
          <xsl:value-of select="$__defining_module"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$module"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="realpath">
      <xsl:choose>
        <xsl:when test="not($href='')">
          <xsl:value-of select="$href"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="include:getRelativePathFromSystemId()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="not(include:exists($realpath, $part, $__target_gen, $__target_key, $module, $search, $tenant, $lang))">
      <xsl:apply-templates/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:checknoinclude[@level='runtime']">
    <ixsl:if test="true()">
      <ixsl:variable name="_href_">
        <xsl:choose>
          <xsl:when test="pfx:href">
            <xsl:apply-templates select="pfx:href/node()"/>
          </xsl:when>
          <xsl:when test="@href">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getRelativePathFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:variable name="_part_">
        <xsl:choose>
          <xsl:when test="pfx:part">
            <xsl:apply-templates select="pfx:part/node()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@part"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:variable name="_module_">
        <xsl:choose>
          <xsl:when test="@module">
            <xsl:value-of select="module"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="include:getModuleFromSystemId()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:variable>
      <ixsl:if test="not(pfx:checkInclude($_href_, $_part_, $_module_, '{@search}'))">
        <xsl:apply-templates/>
      </ixsl:if>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="pfx:href"/>
  <xsl:template match="pfx:part"/>

  <xsl:template name="pfx:image_register_src">
    <xsl:param name="src"/>
    <xsl:param name="themed-path"/>
    <xsl:param name="themed-img"/>
    <xsl:param name="module"/>
    <xsl:param name="search"/>
    <xsl:variable name="module_name">
      <xsl:choose>
        <xsl:when test="$module='PAGEDEF' or $module='pagedef'">
          <xsl:value-of select="$__defining_module"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$module"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($src and not($src = '') and (not($themed-path) or $themed-path = '') and (not($themed-img) or $themed-img = '')) or
                      ((not($src) or $src = '') and $themed-path and not($themed-path = '') and $themed-img and not($themed-img = ''))">
        <xsl:variable name="parent_part"><xsl:value-of select="ancestor::part[position() = 1]/@name"/></xsl:variable>
        <xsl:variable name="parent_theme"><xsl:value-of select="ancestor::theme[position() = 1]/@name"/></xsl:variable>
        <xsl:value-of select="image:getSrc(string($src),string($themed-path),string($themed-img),
                              string($parent_part),string($parent_theme),
                              $__target_gen,string($__target_key),string($module_name),string($search),$tenant,$lang)"/>          
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="no">
          *** Need either the 'src' attribute XOR both of 'themed-img' and 'themed-path' given. ***
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="pfx:image" name="pfx:image">
    <xsl:param name="src"   select="@src"/>
    <xsl:param name="alt"   select="@alt"/>
    <xsl:param name="themed-path" select="@themed-path"/> 
    <xsl:param name="themed-img"  select="@themed-img"/>
    <xsl:param name="exclude-attributes"/>
    <xsl:param name="module" select="@module"/>
    <xsl:param name="search" select="@search"/>
    <xsl:variable name="always-exclude-attributes" select="'src|alt|themed-path|themed-img|module|search'"/>
    <xsl:variable name="real_src">
      <xsl:call-template name="pfx:image_register_src">
        <xsl:with-param name="src" select="$src"/>
        <xsl:with-param name="themed-path" select="$themed-path"/>
        <xsl:with-param name="themed-img" select="$themed-img"/>
        <xsl:with-param name="module" select="$module"/>
        <xsl:with-param name="search" select="$search"/>
      </xsl:call-template>
    </xsl:variable>
    <img src="{{$__contextpath}}/{$real_src}" alt="{$alt}">
      <xsl:if test="$__target_key='__NONE__'"><xsl:attribute name="src"><xsl:value-of select="concat($__contextpath,'/',$real_src)"/></xsl:attribute></xsl:if>      
      <xsl:copy-of select="@*[not(contains(concat('|',$always-exclude-attributes,'|',$exclude-attributes,'|') , concat('|',name(),'|')))]"/>
      <xsl:call-template name="pfx:image_geom_impl">
        <xsl:with-param name="src" select="$real_src"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </img>
    
  </xsl:template>
  
  <xsl:template match="pfx:image[@level='runtime']">
    <img>
      <xsl:variable name="exclude-attributes">|src|alt|themed-path|themed-img|module|search|level|</xsl:variable>
      <xsl:copy-of select="@*[not(contains($exclude-attributes,concat('|',name(),'|')))]"/>
      <ixsl:variable name="real_src">
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
      <ixsl:attribute name="src"><ixsl:value-of select="concat($__contextpath,'/',$real_src)"/></ixsl:attribute>
      <ixsl:attribute name="alt">
        <xsl:choose>
          <xsl:when test="pfx:alt"><xsl:apply-templates select="pfx:alt/node()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="@alt"/></xsl:otherwise>
        </xsl:choose>
      </ixsl:attribute>      
      <ixsl:call-template name="pfx:image_geom_impl">
        <ixsl:with-param name="src" select="$real_src"/>
      </ixsl:call-template>
    </img>
  </xsl:template>
  
  <xsl:template name="pfx:image_geom_impl">
    <xsl:param name="src">
      <xsl:value-of select="./@src"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="string($src) = ''">
        <xsl:message terminate="no">**** Caution:      Error calling pfx:image_geom_impl: no src specified ****</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="path">
          <xsl:choose>
            <xsl:when test="starts-with($src, '/')">
              <xsl:value-of select="substring-after($src, '/')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$src"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="./@width">
              <xsl:value-of select="./@width"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="geometry:getWidth(string($path))"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="./@height">
              <xsl:value-of select="./@height"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="geometry:getHeight(string($path))"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="not($width = -1)">
          <xsl:attribute name="width">
            <xsl:value-of select="$width"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="not($height = -1)">
          <xsl:attribute name="height">
            <xsl:value-of select="$height"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pfx:image_geom_impl_new">
    <xsl:param name="src">
      <xsl:value-of select="./@src"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="string($src) = ''">
        <xsl:message terminate="no">**** Caution:      Error calling pfx:image_geom_impl_new: no src specified ****</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="path">
          <xsl:choose>
            <xsl:when test="starts-with($src, '/')">
              <xsl:value-of select="substring-after($src, '/')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$src"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="style">
          <xsl:value-of select="geometry:getStyleStringForImage(string($path), string(./@style), string(./@width), string(./@height))"/>
        </xsl:variable>
        <xsl:if test="not($style = '')">
          <xsl:attribute name="style">
            <xsl:value-of select="$style"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <func:function name="pfx:getIncludePath">
    <func:result select="include:getRelativePathFromSystemId()"/>
  </func:function>
  
  <func:function name="pfx:getDynIncInfo">
    <xsl:param name="part"/>
    <xsl:param name="theme"/>
    <xsl:param name="path"/>
    <xsl:param name="resolved_module"/>
    <xsl:param name="requested_module"/>
    <xsl:param name="tenant"/>
    <xsl:param name="lang"/>
    <func:result select="include:getDynIncInfo($part, $theme, $path, $resolved_module, $requested_module, $tenant, $lang)"/>
  </func:function>
 
  <func:function name="pfx:checkInclude">
    <xsl:param name="href"/>
    <xsl:param name="part"/>
    <xsl:param name="module"/>
    <xsl:param name="search"/>
    <func:result select="include:exists($href, $part, $__target_gen, $__target_key, $module, $search, $tenant, $lang)"/>
  </func:function>
 
</xsl:stylesheet>
