<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
       xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" 
       xmlns:pfx="http://www.schlund.de/pustefix/core" 
       version="1.0">

  <!-- insert templates for the first stage of the transformation here -->

  <!-- example template showing how to attach error message to form field -->
  <xsl:template match="show-error">
    <pfx:checkfield name="{@field}">
      <pfx:error>
        <span class="error"><pfx:scode/></span>
      </pfx:error>
    </pfx:checkfield>
  </xsl:template>
  
  <xsl:template match="buttons">
    <pfx:forminput>
    <xsl:call-template name="button">
      <xsl:with-param name="letters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:with-param>
    </xsl:call-template>
    </pfx:forminput>
  </xsl:template>
  
  <xsl:template name="button">
    <xsl:param name="letters"/>
    <xsl:variable name="letter" select="substring($letters,1,1)"/>
    <pfx:xinp type="submit" value="{$letter}">
      <pfx:argument name="play.letter"><xsl:value-of select="$letter"/></pfx:argument>
    </pfx:xinp>
    <xsl:if test="string-length($letters) > 1">
    <xsl:call-template name="button">
      <xsl:with-param name="letters" select="substring-after($letters, $letter)"/>
    </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="show-messages">
    <pfx:checkmessage>
      <pfx:messageloop>
        <div>
          <xsl:attribute name="class">{$pfx_class}</xsl:attribute>
          <pfx:scode/>
        </div>
      </pfx:messageloop>
    </pfx:checkmessage>
  </xsl:template>
  
</xsl:stylesheet>
