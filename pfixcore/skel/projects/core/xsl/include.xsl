<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:cus="http://www.schlund.de/pustefix/customize" xmlns:include="xalan://de.schlund.pfixxml.IncludeDocumentExtension" xmlns:runtime="xalan://de.schlund.pfixxml.DependencyTracker" xmlns:geometry="xalan://de.schlund.pfixxml.ImageGeometry" version="1.0" exclude-result-prefixes="include runtime geometry">

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>

  <xsl:param name="__editmode"/>
  <xsl:param name="__recordmode"/>
  <xsl:param name="recordmode_allowed"/>
  <xsl:param name="__sessid"/>
  <xsl:param name="__target_gen"/>
  <xsl:param name="__target_key"/>
  <xsl:param name="prohibitEdit">no</xsl:param>
  
  <xsl:template match="pfx:editconsole">
    
    <xsl:if test="$prohibitEdit = 'no'">
      <table cellpadding="0" cellspacing="0">
        <tr>
          <td>
            <ixsl:choose>
              <ixsl:when test="$__editmode='admin'">
                <a target="_top">
                  <ixsl:attribute name="href">
                    <ixsl:value-of select="$__uri"/>?__editmode=none</ixsl:attribute>
                  <img border="0" title="Switch edit mode OFF" src="/core/img/do_noedit.gif"/>
                </a>
              </ixsl:when>
              <ixsl:otherwise>
                <a target="_top">
                  <ixsl:attribute name="href">
                    <ixsl:value-of select="$__uri"/>?__editmode=admin</ixsl:attribute>
                  <img border="0" title="Switch edit mode ON" src="/core/img/do_edit.gif"/>
                </a>
              </ixsl:otherwise>
            </ixsl:choose>
          </td>
          <td>
            <a target="__xml_source__">
              <ixsl:attribute name="href">
                <ixsl:value-of select="$__uri"/>?__reuse=<ixsl:value-of select="$__reusestamp"/>&amp;__xmlonly=1</ixsl:attribute>
              <img border="0" title="Show last XML tree" src="/core/img/show_xml.gif"/></a>
          </td>  
          <ixsl:if test="$recordmode_allowed = 'true'">
          <td nowrap="nowrap" style="border: black solid 1px; background-color: #999999;">
            <ixsl:choose>
              <ixsl:when test="$__recordmode and $__recordmode!='0'">
                <form target="_top">
                  <input style="font-family: Arial,Helvetica,Sans; margin-left:3px; margin-right:1px; border: black dotted 1px; color: #000000; background-color: #c0c0c0;" readonly="readonly" align="center" name="__recordmode" type="text" size="4">
                    <ixsl:attribute name="value"><ixsl:value-of select="$__recordmode"/></ixsl:attribute>
                  </input>
                  <pfx:xinp type="image" align="center" src="/core/img/record_stop.gif" title="Switch record mode OFF">
                    <pfx:argument name="__recordmode">0</pfx:argument>
                  </pfx:xinp>
                </form>
              </ixsl:when>
              <ixsl:otherwise>
                <form target="_top">
                  <pfx:xinp type="text" style="font-family: Arial,Helvetica,Sans; margin-left:3px; margin-right:1px; border: black solid 1px; color: #c00000;" name="__recordmode" size="4" maxlength="20"/>
                  <pfx:xinp type="image" align="center" src="/core/img/record_start.gif" title="Switch record mode ON"/>
                </form>
              </ixsl:otherwise>
            </ixsl:choose>
          </td>
          </ixsl:if>
        </tr>
      </table>
    </xsl:if>
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
    <xsl:param name="path"/>
    <xsl:param name="href"/>
    <xsl:variable name="thetext">Missing include: '<xsl:value-of select="$part"/>' in file '<xsl:value-of select="$href"/>'</xsl:variable>
    <img src="/core/img/warning.gif">
      <xsl:attribute name="alt"><xsl:value-of select="$thetext"/></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="$thetext"/></xsl:attribute>
    </img>
    <xsl:message>*** Include not found:
      Document = <xsl:value-of select="$path"/>
      Part = <xsl:value-of select="$part"/> ***</xsl:message>
  </xsl:template>

  <xsl:template match="pfx:include" name="pfx:include">
    
    <xsl:param name="__env"/>
    <xsl:param name="parent_path">
      <xsl:if test="/include_parts and not($__target_key='__NONE__')">
        <xsl:value-of select="/include_parts/@incpath"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="parent_part">
      <xsl:if test="not(string($parent_path) = '')"> 
        <xsl:value-of select="ancestor::part[position() = 1]/@name"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="parent_product">
      <xsl:if test="not(string($parent_path) = '')"> 
        <xsl:value-of select="ancestor::product[position() = 1]/@name"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="part"><xsl:value-of select="@part"/></xsl:param>
    <xsl:param name="href">
      <xsl:choose>
        <xsl:when test="@href">
          <xsl:choose>
            <xsl:when test="starts-with(@href, '/')">
              <xsl:value-of select="substring-after(@href, '/')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not(string($parent_path) = '')">
          <xsl:value-of select="substring-after($parent_path, concat($docroot,'/'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">Error. Need href specification for part</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="path">
      <xsl:if test="not(string($href) = '')">
        <xsl:value-of select="concat($docroot, '/', $href)"/>
      </xsl:if>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="string($part) = ''">
        <b>[Error: &lt;pfx:include&gt; needs "part" attribute]</b>
      </xsl:when>
      <xsl:when test="string($href) = ''">
        <b>[Error: &lt;pfx:include&gt; needs "href" attribute or a &lt;pfx:include&gt; parent]</b>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="incnodes" select="include:get(string($path), string($part), string($product),                       string($docroot), string($__target_gen), string($__target_key),                       string($parent_path), string($parent_part), string($parent_product))"/>
        
        <xsl:choose>
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode='admin'">
              <img border="0" src="/core/img/edit_start.gif"/>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key = '__NONE__' and $__editmode = 'admin'">
            <img border="0" src="/core/img/edit_start.gif"/>
          </xsl:when>
        </xsl:choose>
        
        <xsl:choose>
          
          <xsl:when test="$incnodes and $incnodes[name() = 'product']">
            <xsl:choose>
              <xsl:when test="$incnodes/lang">
                
                <xsl:choose>
                  <xsl:when test="not($__target_key = '__NONE__')">
                    <xsl:choose>
                      <xsl:when test="not($incnodes/lang[not(@name = 'default')])">
                        <xsl:choose> 
                          <xsl:when test="$incnodes/lang[@name = 'default']">
                            <xsl:apply-templates select="$incnodes/lang[@name = 'default']/node()">
                              <xsl:with-param name="__env" select="."/>
                            </xsl:apply-templates>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="pfx:missinc">
                              <xsl:with-param name="href" select="$href"/>
                              <xsl:with-param name="part" select="$part"/>
                              <xsl:with-param name="path" select="$path"/>
                            </xsl:call-template>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:when>
                      <xsl:otherwise>
                        <ixsl:choose>
                          <xsl:for-each select="$incnodes/lang[not(@name = 'default')]">
                            <ixsl:when test="$lang = '{./@name}'">
                              <xsl:apply-templates select="./node()">
                                <xsl:with-param name="__env" select="."/>
                              </xsl:apply-templates>
                            </ixsl:when>
                          </xsl:for-each>
                          <xsl:choose> 
                            <xsl:when test="$incnodes/lang[@name = 'default']">
                              <ixsl:otherwise>
                                <xsl:apply-templates select="$incnodes/lang[@name = 'default']/node()">
                                  <xsl:with-param name="__env" select="."/>
                                </xsl:apply-templates>
                              </ixsl:otherwise>
                            </xsl:when>
                            <xsl:otherwise>
                              <ixsl:otherwise>
                                <xsl:call-template name="pfx:missinc">
                                  <xsl:with-param name="href" select="$href"/>
                                  <xsl:with-param name="part" select="$part"/>
                                  <xsl:with-param name="path" select="$path"/>
                                </xsl:call-template>
                              </ixsl:otherwise>
                            </xsl:otherwise>
                          </xsl:choose>
                        </ixsl:choose>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:variable name="langnodes" select="$incnodes/lang[@name = $lang]"/>
                    <xsl:variable name="deflangnodes" select="$incnodes/lang[@name = 'default']"/>
                    <xsl:choose>
                      <xsl:when test="$langnodes">
                        <xsl:apply-templates select="$langnodes/node()">
                          <xsl:with-param name="__env" select="."/>
                        </xsl:apply-templates>
                      </xsl:when>
                      <xsl:when test="$deflangnodes">
                        <xsl:apply-templates select="$deflangnodes/node()">
                          <xsl:with-param name="__env" select="."/>
                        </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:call-template name="pfx:missinc">
                          <xsl:with-param name="href" select="$href"/>
                          <xsl:with-param name="part" select="$part"/>
                          <xsl:with-param name="path" select="$path"/>
                        </xsl:call-template>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>
                
              </xsl:when>
              <xsl:otherwise>
                
                <xsl:call-template name="pfx:missinc">
                  <xsl:with-param name="href" select="$href"/>
                  <xsl:with-param name="part" select="$part"/>
                  <xsl:with-param name="path" select="$path"/>
                </xsl:call-template>
                
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="pfx:missinc">
              <xsl:with-param name="href" select="$href"/>
              <xsl:with-param name="part" select="$part"/>
              <xsl:with-param name="path" select="$path"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        
        <xsl:choose>
          <xsl:when test="not($__target_key = '__NONE__') and $prohibitEdit = 'no'">
            <ixsl:if test="$__editmode = 'admin'">
              <a href="">
                <ixsl:attribute name="onclick">window.open('/xml/edit/includes;<ixsl:value-of select="$__sessid"/>?extinc.Path=<xsl:value-of select="concat($docroot,'/',$href)"/>&amp;extinc.Part=<xsl:value-of select="$part"/>&amp;extprod.Name=<xsl:value-of select="$product"/>&amp;__anchor=left_navi|<xsl:value-of select="concat($docroot,'/',$href)"/>','PustefixEditor','menubar=yes,status=yes,resizable=yes');return(false);</ixsl:attribute>
                <img border="0" src="/core/img/edit.gif" alt="Edit include: '{$part}' in file '{$href}'" title="Edit include: '{$part}' in file '{$href}'"/>
              </a>
            </ixsl:if>
          </xsl:when>
          <xsl:when test="$__target_key='__NONE__' and $__editmode = 'admin'">
            <a href="" onclick="window.open('/xml/edit/commons;{$__sessid}?extcom.Path={concat($docroot,'/',$href)}&amp;extcom.Part={$part}&amp;extprod.Name={$product}&amp;__anchor=left_navi|{concat($docroot,'/',$href)}','PustefixEditor','menubar=yes,status=yes,resizable=yes');return(false);">
              <img border="0" src="/core/img/edit.gif" alt="Edit include: '{$part}' in file '{$href}'" title="Edit include: '{$part}' in file '{$href}'"/>
            </a>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="pfx:image" name="pfx:image">
    
    <xsl:param name="src"><xsl:value-of select="@src"/></xsl:param>
    <img border="0" alt="">
      <xsl:copy-of select="./@*"/>
      <xsl:attribute name="src"><xsl:value-of select="$src"/></xsl:attribute>
      <xsl:call-template name="pfx:image_geom_impl">
        <xsl:with-param name="src"><xsl:value-of select="$src"/></xsl:with-param>
      </xsl:call-template>
      <xsl:apply-templates/> 
    </img>
  </xsl:template>

  <xsl:template name="pfx:image_geom_impl">
    
    <xsl:param name="src"><xsl:value-of select="./@src"/></xsl:param>
    <xsl:param name="path" select="concat($docroot, $src)"/>
    <xsl:param name="parent_path">
      <xsl:if test="/include_parts and not($__target_key='__NONE__')">
        <xsl:value-of select="/include_parts/@incpath"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="parent_part">
      <xsl:if test="not(string($parent_path) = '')"> 
        <xsl:value-of select="ancestor::part[position() = 1]/@name"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="parent_product">
      <xsl:if test="not(string($parent_path) = '')"> 
        <xsl:value-of select="ancestor::product[position() = 1]/@name"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="add_dep" select="runtime:log('image',string(concat($docroot, $src)),'','',                                          string($parent_path),string($parent_part),string($parent_product),                                          string($__target_gen),string($__target_key))"/>
    <xsl:param name="width">
      <xsl:choose>
        <xsl:when test="./@width">
          <xsl:value-of select="./@width"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="geometry:getWidth(string($path))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="height">
      <xsl:choose>
        <xsl:when test="./@height">
          <xsl:value-of select="./@height"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="geometry:getHeight(string($path))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:if test="not($add_dep = '0')">
      <xsl:message terminate="no">**** Caution:      Error calling DependencyTracker ****</xsl:message>
      <xsl:message terminate="no">|    path:         <xsl:value-of select="concat($docroot, $src)"/></xsl:message>
      <xsl:message terminate="no">|    __target_gen: <xsl:value-of select="$__target_gen"/></xsl:message>
      <xsl:message terminate="no">|    __target_key: <xsl:value-of select="$__target_key"/></xsl:message>
    </xsl:if>
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
  </xsl:template>

</xsl:stylesheet>