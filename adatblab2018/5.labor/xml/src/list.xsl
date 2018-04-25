<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="html" indent="yes" version="5.0" />

  <xsl:template match="/">
    <xsl:apply-templates select="vasut/allomas" />
  </xsl:template>
  
  <xsl:template name="my_template" match="allomas[@element-type='recordset']">
    <table>
      <thead>
        <tr>
          <th>Név</th>
          <th>Város</th>
          <th>Átlagos utasszám</th>
          <th>Kapacitás sztrájk esetén</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates />
      </tbody>
    </table>
  </xsl:template>
  
  <xsl:template match="allomas[@element-type='recordset']/record">
    <tr>
      <xsl:apply-templates select="nev" />
      <xsl:apply-templates select="varos" />
      <xsl:apply-templates select="atlagutas" />
      <xsl:apply-templates select="sztrajkutas" />
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
