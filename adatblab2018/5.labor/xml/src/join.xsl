<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <xsl:template match="/vasut">
    <allomas-lista>
      <xsl:apply-templates select="allomas/record" />
    </allomas-lista>
  </xsl:template>

  <xsl:template match="allomas[@element-type='recordset']/record">
    <allomas>
      <xsl:attribute name="id">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <nev>
        <xsl:value-of select="nev" />
      </nev>
      <varos>
        <xsl:value-of select="varos" />
      </varos>
      <atlagutas>
        <xsl:value-of select="atlagutas" />
      </atlagutas>
      <vonatok>
        <xsl:variable name="all_id" select="@id" />
        <xsl:apply-templates select="/vasut/megall[@element-type='recordset']/record[allomas_id=$all_id]"/>
      </vonatok>
    </allomas>
  </xsl:template>

<!-- gives details of a journey -->
  <xsl:template match="megall[@element-type='recordset']/record">
	  <vonat >
      <xsl:attribute name="vonatszam">
        <xsl:value-of select="vonatszam" />
      </xsl:attribute>
      <xsl:apply-templates select="erk" />
      <xsl:apply-templates select="ind" />
    </vonat>
  </xsl:template>
  
  <xsl:template match="record/erk[@is-null='True']" priority="0.8"></xsl:template>

  <xsl:template match="record/erk" priority="0.4">
    <xsl:attribute name="erk">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="record/ind[@is-null='True']" priority="0.8"></xsl:template>

  <xsl:template match="record/ind" priority="0.4">
    <xsl:attribute name="ind">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>