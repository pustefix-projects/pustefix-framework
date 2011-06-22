<?xml version="1.0" encoding="UTF-8"?>

<!-- This is the master stylesheet. It is responsible for generating
     the final stylesheet that drives the page generation. It should
     be fairly general, i.e. the only modifications needed here
     besides bugfixes are the inclusion of other stylesheet to handle
     a specific "skinning" of elements. This is done via a
     customization xsl file that reads this file as xml and produces a
     specific version of the master.xsl (more below).
     
     Note that you can include additional stylesheets in two places
     here: in the "xsl:" section, where it applies to the generation
     of the final stylesheet, and in the "ixsl:" section, where it
     applies to the final html generation. It most often does NOT make
     sense to include more stylesheets in the final stylesheet, as all
     skinning and layout should happened in stages before. No skinning
     should be needed in the online version.

     NOTE: THIS IS NOT A WORKING MASTER STYLESHEET!  You need to
     process it with a customization stylesheet that handles the tags
     <cus:product/>, <cus:lang/> and <cus:custom_xsl/>.

     Let me repeat once again: NEVER EVER change anything here that
     doesn't apply to the most general case. If the need arises, do
     these changes by adding customizations to the custom xsl file
     mentioned above. Stay away from including skinning in the final
     generated stylesheet.
     
     The functionality contained in this file is for the most part the
     framehandling. Other than that it serves - as mentioned above -
     as the skeleton code for producing the final stylesheet.

     It includes include.xsl for the handling of the <pfx:include/> tag
     into both itself and the final stylesheet. This allows for servlets
     to use responses that are coded as <pfx:include/> tags.

     Besides that it includes forminput.xsl (Handling of html forms)
     and navigation.xsl (handling of automated navigations). Note:
     these are _not_ included into the final stylesheet.
     See these files for more info. -->

<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
				xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:import href="module://pustefix-core/xsl/default_copy.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/include.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/navigation.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/utils.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/forminput.xsl"/>

  <cus:custom_xsl/>

  <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:param name="outputmethod">html</xsl:param>
  <xsl:param name="outputencoding">UTF-8</xsl:param>
  <xsl:param name="outputdoctype-public"/>
  <xsl:param name="outputdoctype-system"/>
  
  <!-- <xsl:param name="additional_custom_namespaces"/> -->
  <!-- <xsl:param name="exclude_custom_ns_prefixes"/> -->
  
  <!-- Needed for includes to work. Remember to include this in the resulting stylesheet, too! -->
  <xsl:param name="app"/>
  <xsl:param name="lang"><cus:lang/></xsl:param>
  <xsl:param name="product"><cus:product/></xsl:param>

  <xsl:param name="page"/>
  <xsl:param name="__navitree"/>
  <xsl:param name="navitree" select="$__navitree"/>

  <!--
    Define __contextpath despite it's only evaluated/needed at runtime, cause Saxon2
    doesn't allow the usage of undefined variables/params, even if they aren't evaluated
  -->
  <xsl:param name="__contextpath">$__contextpath</xsl:param>

  <xsl:param name="stylesheets_to_include"/>

  <xsl:param name="compress-inline-javascript"/>

  <xsl:key name="frameset_key" match="pfx:frameset" use="'fset'"/>
  <xsl:key name="frame_key"    match="pfx:frame"    use="'frame'"/>

  <xsl:template match="/">
    <ixsl:stylesheet version="1.1"
                     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                     xmlns:cus="http://www.schlund.de/pustefix/customize"
                     xmlns:pfx="http://www.schlund.de/pustefix/core"
                     xmlns:func="http://exslt.org/functions"
                     xmlns:url="xalan://java.net.URLEncoder"
                     xmlns:deref="xalan://org.pustefixframework.http.dereferer.SignUtil"
                     xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback"
                     xmlns:compress="xalan://org.pustefixframework.util.javascript.CompressorCallback"
                     xmlns:rfh="java:org.pustefixframework.http.AbstractPustefixXMLRequestHandler$RegisterFrameHelper" 
                     exclude-result-prefixes="pfx cus xsl url deref callback compress func rfh">

      <ixsl:import href="module://pustefix-core/xsl/default_copy.xsl"/>
      <ixsl:import href="module://pustefix-core/xsl/include.xsl"/>
      <ixsl:import href="module://pustefix-core/xsl/functions.xsl"/>
      <ixsl:import href="module://pustefix-core/xsl/render.xsl"/>

      <!-- generate user defined imports -->
      <xsl:call-template name="gen_ixsl_import">
        <xsl:with-param name="ssheets"><xsl:value-of select="normalize-space($stylesheets_to_include)"/></xsl:with-param>
      </xsl:call-template>

      <ixsl:output indent="no">
        <xsl:if test="not($outputmethod = '')">
          <xsl:attribute name="method"><xsl:value-of select="$outputmethod"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="not($outputencoding = '')">
          <xsl:attribute name="encoding"><xsl:value-of select="$outputencoding"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="not($outputdoctype-system = '')">
          <xsl:attribute name="doctype-system"><xsl:value-of select="$outputdoctype-system"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="not($outputdoctype-public = '')">
          <xsl:attribute name="doctype-public"><xsl:value-of select="$outputdoctype-public"/></xsl:attribute>
        </xsl:if>
      </ixsl:output>

      <ixsl:param name="__navitree"/>
      <ixsl:param name="navitree" select="$__navitree"/>
      
      <!-- The next three parameters are opaque Java objects. Use them only to pass them to extension functions! -->
      <ixsl:param name="__context__"/>
      <ixsl:param name="__spdoc__"/>
      <ixsl:param name="__register_frame_helper__"/>
      
      <!-- these parameters will always be passed in by the servlet -->
      <ixsl:param name="__uri"/>

      <!-- e.g. 1E668C65F42697962A31177EB5319D8B.foo -->
      <!--
          This is used whenever a session id has to be passed to the
          outside for being able to jump back into the
          application. When not running under SSL, this is the usual
          session id.  Whenever running under it's the (now invalid)
          parent session id, that will still allow to jump back into
          the running SSL session if the client can be identified by a
          also supplied secure cookie. (See ServletManager for
          initimidating details)
      -->
      <ixsl:param name="__external_session_ref"/>
      
      <!-- e.g. /context -->
      <ixsl:param name="__contextpath"/>

      <ixsl:param name="__derefkey"/>
      <ixsl:param name="__querystring"/>
      <ixsl:param name="__remote_addr"/>
      <ixsl:param name="__server_name"/>
      <ixsl:param name="__request_scheme"/>
      <ixsl:param name="__frame">
        <xsl:if test="/pfx:document/pfx:frameset or /pfx:document/html/pfx:frameset">_top</xsl:if>
      </ixsl:param>
      
      <ixsl:param name="__reusestamp">-1</ixsl:param>
      
      <ixsl:param name="__lf"/>
      <ixsl:param name="pageflow"/>
      
      <ixsl:param name="app"><xsl:value-of select="$app"/></ixsl:param>
      <ixsl:param name="lang"><xsl:value-of select="$lang"/></ixsl:param>
      <ixsl:param name="page"><xsl:value-of select="$page"/></ixsl:param>
      <ixsl:variable name="product"><xsl:value-of select="$product"/></ixsl:variable>
      <ixsl:variable name="__root" select="/"/>
      
      <ixsl:template name="__enc">
        <ixsl:param name="in"/>
        <ixsl:param name="enc">
          <xsl:if test="not($outputencoding = '')">
            <xsl:value-of select="$outputencoding"/>
          </xsl:if>
        </ixsl:param>
        <ixsl:if test="$in">
          <ixsl:value-of select="url:encode(string($in),$enc)"/>
        </ixsl:if>
      </ixsl:template>

      <ixsl:template name="__sign">
        <ixsl:param name="in"/>
        <ixsl:param name="ts"/>
        <ixsl:value-of select="deref:getSignature($in, $ts)"/>
      </ixsl:template>
      
      <ixsl:template name="__fake_session_id_argument">
        <ixsl:value-of select="deref:getFakeSessionIdArgument($__sessionIdPath)"/>
      </ixsl:template>
      
      <ixsl:template name="__deref">
        <ixsl:param name="link"/>
        <ixsl:variable name="enclink">
          <ixsl:call-template name="__enc">
            <ixsl:with-param name="in" select="$link"/>
          </ixsl:call-template>
        </ixsl:variable>
        <ixsl:variable name="ts">
          <ixsl:value-of select="deref:getTimeStamp()"/>
        </ixsl:variable>
        <ixsl:variable name="sign">
          <ixsl:call-template name="__sign">
            <ixsl:with-param name="in" select="$link"/>
            <ixsl:with-param name="ts" select="$ts"/>
          </ixsl:call-template>
        </ixsl:variable>
        <ixsl:value-of select="$__contextpath"/>
        <ixsl:text>/deref</ixsl:text>
        <ixsl:call-template name="__fake_session_id_argument"/>
        <ixsl:text>?link=</ixsl:text>
        <ixsl:value-of select="$enclink"/>&amp;__sign=<ixsl:value-of select="$sign"/>&amp;__ts=<ixsl:value-of select="$ts"/>
      </ixsl:template>

      <ixsl:template name="__formwarn">
        <xsl:choose>
          <xsl:when test="$prohibitEdit = 'no'">
           <ixsl:param name="fullname"/>
           <ixsl:param name="targetpage"/>
<!--           <ixsl:message>***IN*** <ixsl:value-of select="$fullname"/>@<ixsl:value-of select="$targetpage"/></ixsl:message>-->
             <ixsl:if test="contains($fullname, '.')">
              <ixsl:variable name="prefix" select="substring-before($fullname, '.')"/>
              <ixsl:variable name="tmp" select="substring-after($fullname, '.')"/>
              <ixsl:variable name="name">
                <ixsl:choose>
                  <ixsl:when test="contains($tmp, '.')"><ixsl:value-of select="substring-before($tmp, '.')"/></ixsl:when>
                  <ixsl:otherwise><ixsl:value-of select="$tmp"/></ixsl:otherwise>
                </ixsl:choose>
              </ixsl:variable>
              <ixsl:variable name="index" select="substring-after($tmp, '.')"/>
              <ixsl:choose>
                <ixsl:when test="not(pfx:getIWrapperInfo($targetpage,$prefix))">
                  <div style="position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;" 
                  onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">
                    Warning: Unknown wrapper <b><ixsl:value-of select="$prefix"/></b> on page <b><ixsl:value-of select="$targetpage"/></b>
                  </div>
                </ixsl:when>
                <ixsl:when test="not(pfx:getIWrapperInfo($targetpage,$prefix)/iwrapper/param[@name = $name])">
                  <div style="position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;" 
                  onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">
                    Warning: Unknown parameter <b><ixsl:value-of select="$name"/></b> in wrapper <b><ixsl:value-of select="$prefix"/></b> on page <b><ixsl:value-of select="$targetpage"/></b>
                  </div>
                </ixsl:when>
                <ixsl:when test="$index and not(pfx:getIWrapperInfo($targetpage,$prefix)/iwrapper/param[@name = $name and @occurrence = 'indexed'])">
                  <div style="position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;" 
                  onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">
                    Warning: No indexed parameter <b><ixsl:value-of select="$name"/></b> in wrapper <b><ixsl:value-of select="$prefix"/></b> on page <b><ixsl:value-of select="$targetpage"/></b>
                  </div>
                </ixsl:when>
                <ixsl:when test="not($index) and not(pfx:getIWrapperInfo($targetpage,$prefix)/iwrapper/param[@name = $name and @occurrence != 'indexed'])">
                  <div style="position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;" 
                  onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';">
                    Warning: Parameter <b><ixsl:value-of select="$name"/></b> in wrapper <b><ixsl:value-of select="$prefix"/></b> on page <b><ixsl:value-of select="$targetpage"/> must be indexed</b>
                  </div>
                </ixsl:when>
              </ixsl:choose>
            </ixsl:if>
          </xsl:when>
       </xsl:choose>
    </ixsl:template>

  <ixsl:template name="__formwarn_command">
    <xsl:choose>
      <xsl:when test="$prohibitEdit = 'no'">
        <ixsl:param name="fullname" />
        <ixsl:param name="targetpage" />

        <ixsl:if test="not(contains($fullname, '.'))">
          <ixsl:choose>
            <ixsl:when test="not(pfx:getIWrapperInfo($targetpage,$fullname))">
              <div
                style="position: absolute; color: #000000; background-color: #eeaaaa; border: solid 1px #aa8888; font-family: sans-serif; font-size:9px; font-weight: normal;"
                onclick="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true; this.style.display='none';return false;">
                Warning: Unknown wrapper
                <b>
                  <ixsl:value-of select="$fullname" />
                </b>
                on page
                <b>
                  <ixsl:value-of select="$targetpage" />
                </b>
              </div>
            </ixsl:when>
          </ixsl:choose>
        </ixsl:if>
      </xsl:when>
    </xsl:choose>
  </ixsl:template>    

      <ixsl:template match="/">
        <ixsl:call-template name="__render_start__"/>
        <xsl:choose>
          <!-- <xsl:when test="//frameset"> -->
          <xsl:when test="key('frameset_key','fset')">
            <ixsl:choose>
              <ixsl:when test="$__frame = '_top'"> 
                <html>
                  <!-- Unregister SPDocument for top frame -->
                <ixsl:value-of select="rfh:unregisterFrame($__register_frame_helper__, '_top')"/>
                  <xsl:apply-templates select="/pfx:document/node()"/>
                </html>
              </ixsl:when>
              <xsl:for-each select="key('frame_key','frame')"> 
                <xsl:choose>
                  <xsl:when test="not(./pfx:frameset)">
                    <ixsl:when test="$__frame = '{./@name}'">
                      <!-- Unregister SPDocument for this frame -->
                      <ixsl:value-of select="rfh:unregisterFrame($__register_frame_helper__, $__frame)"/>
                      <xsl:apply-templates select="./node()"/>
                    </ixsl:when>
                  </xsl:when>
                  <xsl:otherwise>
                    <ixsl:when test="$__frame = '{./@name}'">
                      <!-- Unregister SPDocument for this frame -->
                      <ixsl:value-of select="rfh:unregisterFrame($__register_frame_helper__, $__frame)"/>
                      <html>
                        <head/>
						<xsl:apply-templates select="./pfx:frameset"/>
                      </html>
                    </ixsl:when>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </ixsl:choose>
          </xsl:when>
          <xsl:otherwise><!-- no frames defined! -->
<!--            <ixsl:value-of select="callback:setNoStore($__spdoc__)"/>-->
            <!-- Unregister SPDocument for top frame -->
            <ixsl:value-of select="rfh:unregisterFrame($__register_frame_helper__, '_top')"/>
            <xsl:apply-templates select="/pfx:document/node()"/>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:template>
      <!-- <xsl:text disable-output-escaping="yes"> -->
      <!-- &lt;/ixsl:stylesheet> -->
      <!-- </xsl:text> -->
    </ixsl:stylesheet>
  </xsl:template>

  <!-- this is needed... it allows to "comment out" blocks without loosing them during the trafo steps -->
  <xsl:template match="pfx:rem">
  </xsl:template>

  <xsl:template match="pfx:script">
    <script>
      <xsl:attribute name="type">text/javascript</xsl:attribute>
      <xsl:copy-of select="@*[not(name()='compress' or name()='transform')]"/>
      <ixsl:comment><xsl:text>&#10;</xsl:text>
        <xsl:choose>
          <xsl:when test="$compress-inline-javascript='true' and not(@compress='false')">
            <ixsl:variable name="__script">
              <xsl:choose>
                <xsl:when test="@transform='true'"><xsl:apply-templates/></xsl:when>
                <xsl:otherwise><xsl:copy-of select="./node()"/></xsl:otherwise>
              </xsl:choose>
            </ixsl:variable>
            <ixsl:text>&#160;&#10;</ixsl:text>
            <ixsl:value-of select="compress:compressJavascript($__script)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="@transform='true'"><xsl:apply-templates/></xsl:when>
              <xsl:otherwise><xsl:copy-of select="./node()"/></xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
	//</ixsl:comment>
    </script>
  </xsl:template>
  
  <xsl:template match="pfx:frameset">
    <frameset frameborder="0" framespacing="0" border="0">
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates select="./pfx:frameset | ./pfx:frame"/>
    </frameset>
  </xsl:template>
  
  <xsl:template match="pfx:frame">
    <!-- Register SPDocument for this frame -->
    <ixsl:value-of select="rfh:registerFrame($__register_frame_helper__, '{@name}')"/>
    <frame scrolling="auto" marginwidth="1" marginheight="1">
      <xsl:copy-of select="./@*[name()!='noresize']"/>
      <xsl:if test="@noresize!='false'">
	<xsl:attribute name="noresize">1</xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="not(./pfx:frameset) and .//pfx:external">
	  <xsl:choose>
	    <xsl:when test=".//pfx:external[position()=1]/@src">
	      <xsl:copy-of select=".//pfx:external[position()=1]/@src"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:copy-of select=".//pfx:external[position()=1]/node()"/>
	    </xsl:otherwise>
	  </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <ixsl:attribute name="src">
            <ixsl:value-of select="$__uri"/>?__frame=<xsl:value-of select="@name"/>&amp;__reuse=<ixsl:value-of select="$__reusestamp"/>.<ixsl:text><xsl:value-of select="@name"/></ixsl:text>
            <ixsl:if test="/formresult/frameanchor[@frame = '{@name}']">#<ixsl:value-of select="/formresult/frameanchor[@frame = '{@name}']/@anchor"/></ixsl:if>
          </ixsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </frame>
  </xsl:template>


  <xsl:template name="gen_ixsl_import">
    <xsl:param name="ssheets"/>
    <xsl:variable name="first">
      <xsl:value-of select="normalize-space(substring-before(concat($ssheets, ' '), ' '))"/>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:value-of select="normalize-space(substring-after($ssheets, ' '))"/>
    </xsl:variable>
    <xsl:if test="$first != ''">
      <ixsl:import>
        <xsl:attribute name="href"><xsl:value-of select="$first"/></xsl:attribute>
      </ixsl:import><xsl:text>
      </xsl:text>
    </xsl:if>
    <xsl:if test="$rest != ''">
      <xsl:call-template name="gen_ixsl_import">
        <xsl:with-param name="ssheets">
          <xsl:value-of select="$rest"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template match="pfx:wsscript">
    <script type="text/javascript">
      <ixsl:attribute name="src">
        <ixsl:value-of select="concat($__contextpath,'/webservice')"/>
        <xsl:if test="@session='true'"><ixsl:value-of select="$__sessionIdPath"/></xsl:if>
        <ixsl:value-of select="concat('?wsscript&amp;name=',url:encode('{@name}','{$outputencoding}'),'&amp;type=')"/>
        <xsl:choose>
          <xsl:when test="@type"><xsl:value-of select="@type"/></xsl:when>
          <xsl:otherwise>jsonws</xsl:otherwise>
        </xsl:choose>
      </ixsl:attribute>
    </script>
  </xsl:template>

</xsl:stylesheet>
