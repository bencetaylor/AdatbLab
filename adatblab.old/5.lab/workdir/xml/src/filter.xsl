<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <!-- Kezdő csomópont a szall, innen kiválasztjuk az orders 
       elemeit, majd meghívjuk a következő sablont 
  -->
  <xsl:template match="/szall">
    <xsl:copy>
      <xsl:attribute name="element-type" >
        <xsl:value-of select="@element-type" />
      </xsl:attribute>
      <xsl:apply-templates select="orders" />
    </xsl:copy>
  </xsl:template>

  <!-- Lemásolja az orders csomópont összes gyermekét, ahol a 
       szállítandó mennyiség (quantity) nagyobb mint 19000. 
  -->
  <xsl:template match="orders">
    <xsl:copy>
      <xsl:attribute name="element-type" >
        <xsl:value-of select="@element-type" />
      </xsl:attribute>
      <xsl:copy-of select="record[quantity > 19000]" />
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
