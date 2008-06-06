<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:include="xalan://de.schlund.pfixxml.IncludeDocumentExtensionSaxon2"
                xmlns:image="xalan://de.schlund.pfixxml.ImageThemedSrcSaxon2"
                xmlns:geometry="xalan://de.schlund.pfixxml.ImageGeometry"
                xmlns:func="http://exslt.org/functions"
                exclude-result-prefixes="include image geometry">

  <!-- The needed parameters must be set in the including stylesheet! -->

  <!-- 
    ##########################################
    # WARNING:                               #
    # All changes within this file also have #
    # to be applied to include.xsl           #
    ##########################################
  -->

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>

  <xsl:param name="__editmode"/>
  <xsl:param name="__sessid"/>
  <xsl:param name="__target_gen"/>
  <xsl:param name="__target_key"/>
  <xsl:param name="__editor_url"/>
  <xsl:param name="themes"/>
  <xsl:param name="prohibitEdit">no</xsl:param>

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
                        <img src="{{$__contextpath}}/core/img/warning2.png"/><br/>
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
                <img src="{$__contextpath}/core/img/warning2.png"/><br/>
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
            <img src="{{$__contextpath}}/core/img/warning2.png">
              <xsl:if test="$__target_key = '__NONE__'">
                <xsl:attribute name="src"><xsl:value-of select="$__contextpath"/>/core/img/warning2.png</xsl:attribute>
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
    <xsl:variable name="thetext">Missing include: '<xsl:value-of select="$part"/>' in file '<xsl:value-of select="$href"/>'</xsl:variable>
    <img src="{{$__contextpath}}/core/img/warning.gif">
      <xsl:if test="$__target_key = '__NONE__'">
        <xsl:attribute name="src"><xsl:value-of select="$__contextpath"/>/core/img/warning.gif</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="alt"><xsl:value-of select="$thetext"/></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="$thetext"/></xsl:attribute>
    </img>
    <xsl:message>*** Include not found:
      Document = <xsl:value-of select="$href"/>
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
          <xsl:value-of select="include:makeSystemIdRelative()"/>
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
                              string($__target_gen), string($__target_key),
                              string($parent_part), string($parent_theme), $computed_inc)"/>
        <!-- Start image of edited region -->
        <xsl:choose>
          <xsl:when test="$noedit = 'true'"/> <!-- Do NOTHING! -->
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode='admin'">
              <img border="0" alt="[" src="{{$__contextpath}}/core/img/edit_start.gif"/>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key = '__NONE__' and $__editmode = 'admin'">
            <img border="0" alt="[" src="{$__contextpath}/core/img/edit_start.gif"/>
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
              <xsl:with-param name="href" select="$realpath"/>
              <xsl:with-param name="part" select="$part"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        <!-- ===================================================== -->
        <xsl:choose>
          <xsl:when test="$noedit = 'true'"/> <!-- Do NOTHING! -->
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode = 'admin'">
              <a href="#">
                <ixsl:attribute name="onclick">window.open('<ixsl:value-of select="$__editor_url"/>/xml/edit?__scriptedflow=selectinclude&amp;theme=<xsl:value-of select="string($used_theme)"/>&amp;path=<xsl:value-of select="string($realpath)"/>&amp;part=<xsl:value-of select="$part"/>&amp;tgen=<xsl:value-of select="$product"/>&amp;type=include&amp;__anchor=left_navi|<xsl:value-of select="$realpath"/>','PustefixEditor','menubar=yes,status=yes,resizable=yes');return(false);</ixsl:attribute>
                <img border="0" src="{{$__contextpath}}/core/img/edit.gif"
                     alt="] Edit include: '{$part}' in file '{$realpath}'"
                     title="Edit include: '{$part}' in file '{$realpath}'"/>
              </a>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key='__NONE__' and $__editmode = 'admin'">
            <a href="#">
              <xsl:attribute name="onClick">window.open('<ixsl:value-of select="$__editor_url"/>/xml/edit?__scriptedflow=selectinclude&amp;theme=<xsl:value-of select="string($used_theme)"/>&amp;path=<xsl:value-of select="$realpath"/>&amp;part=<xsl:value-of select="$part"/>&amp;tgen=<xsl:value-of select="$product"/>&amp;type=dyninclude&amp;__anchor=left_navi|{$realpath}','PustefixEditor','menubar=yes,status=yes,resizable=yes');return(false);</xsl:attribute>
              <img border="0" src="{$__contextpath}/core/img/edit.gif" alt="] Edit include: '{$part}' in file '{$realpath}'" title="Edit include: '{$part}' in file '{$realpath}'"/>
            </a>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="pfx:image_register_src">
    <xsl:param name="src"/>
    <xsl:param name="themed-path"/>
    <xsl:param name="themed-img"/>
    <xsl:choose>
      <xsl:when test="($src and not($src = '') and (not($themed-path) or $themed-path = '') and (not($themed-img) or $themed-img = '')) or
                      ((not($src) or $src = '') and $themed-path and not($themed-path = '') and $themed-img and not($themed-img = ''))">
        <xsl:variable name="parent_part"><xsl:value-of select="ancestor::part[position() = 1]/@name"/></xsl:variable>
        <xsl:variable name="parent_theme"><xsl:value-of select="ancestor::theme[position() = 1]/@name"/></xsl:variable>
        <xsl:value-of select="image:getSrc(string($src),string($themed-path),string($themed-img),
                              string($parent_part),string($parent_theme),
                              string($__target_gen),string($__target_key))"/>          
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
    <xsl:variable name="always-exclude-attributes" select="'src|alt|themed-path|themed-img'"/>
    <xsl:variable name="real_src">
      <xsl:call-template name="pfx:image_register_src">
        <xsl:with-param name="src" select="$src"/>
        <xsl:with-param name="themed-path" select="$themed-path"/>
        <xsl:with-param name="themed-img" select="$themed-img"/>
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
    <func:result select="include:makeSystemIdRelative()"/>
  </func:function>

</xsl:stylesheet>
