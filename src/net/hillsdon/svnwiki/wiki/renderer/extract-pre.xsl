<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>
  <xsl:template match="/">
    <examples>
      <xsl:for-each select="//pre">
        <xsl:text>
</xsl:text>
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </examples>
  </xsl:template>
</xsl:stylesheet>

