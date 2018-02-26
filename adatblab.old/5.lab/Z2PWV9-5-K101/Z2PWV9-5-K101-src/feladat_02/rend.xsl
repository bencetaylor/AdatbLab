<?xml version="1.0" encoding="UTF-8"?>
<!--Ez a fájl a második feladat megoldásához tartozik-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <!--Ennél a feladatnál is elég egy sablont használni-->
    <xsl:template match="/">
	<html>
	    <head>
		<title>Eszközök</title>
        <style>
            <!--Forrás: http://www.w3schools.com/css/tryit.asp?filename=trycss_text-transform -->
            font.uppercase {text-transform: uppercase}
        </style>
	    </head>
	    <body>
        <font color="red" size="30">Eszközök</font><hr/>
        <!--Itt kezdődik a feltételes elágazás, amely meghatározza, hogy mi történjen hiba,
            illetve helyes működés esetén-->
        <xsl:choose>
            <!--Hiba esetén: -->
            <xsl:when test="//xsql-error">
                <!--Jelezzük a felhasználónak, hogy hiba történt, illetve annak szövegét-->
                <p>Adatbázishiba:
                    <xsl:value-of select="//xsql-error/message[1]"/>
                </p>
            </xsl:when>
            <!--Ha nem volt hiba, akkor csak kiírjuk a táblázatot-->
            <xsl:otherwise>
                <table>
                    <xsl:for-each select="page/ROWSET/ROW">
                    <tr>
                        <td>
                        <font class="uppercase"><xsl:value-of select="MARKA"/>&nbsp;<xsl:value-of select="NEV"/></font>
                        (Eszköz kód: <xsl:value-of select="ESZK_AZON"/>)<br/>
                        Típus: <xsl:value-of select="TIPUS"/><br/>
                        Napi költség: <xsl:value-of select="NAPI_KSG"/> Ft<br/> 
                        Vásárlás ideje: <xsl:value-of select="VASARLAS"/>
                        <hr/>
                        </td>
                    </tr>
                    </xsl:for-each>
                </table>
            </xsl:otherwise>
        </xsl:choose>
	    </body>
	</html>
    </xsl:template>
    
    
</xsl:stylesheet>