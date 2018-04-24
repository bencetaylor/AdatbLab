<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="text" />

  <xsl:template match="/">
    <xsl:apply-templates select="ship/ships" />
  </xsl:template>

  <xsl:template match="ships[@element-type='recordset']"
>"Name"
<xsl:apply-templates select="*"
/></xsl:template>

  <xsl:template match="ships[@element-type='recordset']/record"
><xsl:apply-templates select="name" /><xsl:text>&#10;</xsl:text>
</xsl:template>

  <xsl:template match="record/*" priority="0.4"
><xsl:value-of select="."
/></xsl:template>
</xsl:stylesheet>
