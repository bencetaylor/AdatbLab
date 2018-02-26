<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <xsl:template match="/ship">
    <ship-registry>
      <xsl:apply-templates select="ships/record" />
    </ship-registry>
  </xsl:template>

  <xsl:template match="ships[@element-type='recordset']/record">
    <ship>
      <xsl:attribute name="id">
        <xsl:value-of select="@ship_id" />
      </xsl:attribute>
      <journeys>
        <xsl:variable name="l_shipid" select="@ship_id" />
	<!-- find and include journeys for this ship here -->
      </journeys>
    </ship>
  </xsl:template>

  <!-- gives details of a journey -->
  <xsl:template match="journeys[@element-type='recordset']/record">
    <journey bar="drink">
      <foo>Foo</foo>
    </journey>
  </xsl:template>
</xsl:stylesheet>
