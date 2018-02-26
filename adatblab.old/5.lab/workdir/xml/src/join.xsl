<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="xml" indent="yes" />

  <!-- A kezdő csomópontunk a szall, melyben az orders/record gyerek 
        csomópontokra alkalmazzuk a sablont.
  -->
  <xsl:template match="/szall">
    <orders>
      <xsl:apply-templates select="orders/record" />
    </orders>
  </xsl:template>

  <!-- Minden order csomópont adatait kiírjuk. Az order_id alapján kiválasztjuk
        a megrendeléshez tartozó szállítmány csomópontokat.
  -->
  <xsl:template match="orders[@element-type='recordset']/record">
    <order>
      <xsl:attribute name="id">
        <xsl:value-of select="@order_id" />
      </xsl:attribute>
      <description>
        <xsl:value-of select="description" />
      </description>
      <quantity>
        <xsl:value-of select="quantity" />
      </quantity>
      <deadline_date>
        <xsl:value-of select="deadline_date/@date" />
      </deadline_date>
      <shipments>
        <xsl:variable name="my_id" select="@order_id" />
        <xsl:apply-templates select="//shipments/record[order_id=$my_id]"/>
      </shipments>
    </order>
  </xsl:template>
  
  <!-- A kiválasztott shipment csomópont adatait írjuk ki. -->
  <xsl:template match="shipments[@element-type='recordset']/record">
	<shipment >
      <xsl:attribute name="vehicle_id">
        <xsl:value-of select="vehicle_id" />
      </xsl:attribute>
      <xsl:attribute name="arrival_date">
        <xsl:value-of select="arrival_date/@date" />
      </xsl:attribute>
    </shipment>
  </xsl:template>
</xsl:stylesheet>
