<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <xsl:template match="/vasut">
    <xsl:copy>
      <xsl:attribute name="element-type" >
        <xsl:value-of select="@element-type" />
      </xsl:attribute>
      
      <xsl:apply-templates select="allomas" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="allomas">
    <xsl:copy>
      <xsl:attribute name="element-type" >
        <xsl:value-of select="@element-type" />
      </xsl:attribute>
      <xsl:copy-of select="record[varos='Budapest']" />
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

