<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:functx="http://www.functx.com"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:import href="list.xsl" />

  <xsl:output method="html" indent="yes" version="5.0" />

  <!-- override root template from list.xsl -->
  <xsl:template match="//orders">
    <html>
      <head>
        <title>Order register</title>
      </head>
      <body>
        <h1>RENDELÉSEK</h1>
          <table>
            <tr>
              <th>Megnevezés</th>
              <th>Mennyiség</th>
              <th>Határidő</th>
            </tr>
            <xsl:apply-templates />
          </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
