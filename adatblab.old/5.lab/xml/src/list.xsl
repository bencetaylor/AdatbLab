<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <xsl:template match="/">
    <xsl:apply-templates select="szall/orders" />
  </xsl:template>

  <xsl:template match="orders[@element-type='recordset']">
    <table>
        <tr>
          <th>Megnevezés</th>
          <th>Mennyiség</th>
		  <th>Határidő</th>
        </tr>
        <xsl:apply-templates />
    </table>
  </xsl:template>

  <xsl:template match="orders[@element-type='recordset']/record">
    <tr>
      <xsl:apply-templates select="description" />
      <xsl:apply-templates select="quantity" />
	  <td>
        <xsl:apply-templates select="deadline_date/@date" />
      </td>
    </tr>
  </xsl:template>

  <!-- process an empty field -->
  <xsl:template match="record/*[@is-null='True']" priority="0.8">
    <td/>
  </xsl:template>

  <!-- process a non-empty field -->
  <xsl:template match="record/*" priority="0.4">
    <td>
      <xsl:value-of select="." />
    </td>
  </xsl:template>
</xsl:stylesheet>
