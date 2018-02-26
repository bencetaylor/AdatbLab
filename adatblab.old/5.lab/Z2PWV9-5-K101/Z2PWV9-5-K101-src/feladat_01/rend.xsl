<?xml version="1.0" encoding="UTF-8"?>
<!--Ez a fájl az első feladat megoldásához tartozik-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <!--Ennél a feladatnál elég egy sablont használni-->
    <xsl:template match="/">
	<html>
	    <head>
        <!--Az igényesség kedvéért ezt a fejlécet adtam az oldalnak-->
		<title>Eszközök</title>
        <!--Nagybetűsre változtatja a szöveget-->
        <style>
            <!--Forrás: http://www.w3schools.com/css/tryit.asp?filename=trycss_text-transform -->
            font.uppercase {text-transform: uppercase}
        </style>
	    </head>
	    <body>
        <font color="red" size="30">Eszközök</font><hr/>
        <!--Ebben a táblázatban tárolódnak az adatbázisból kinyert adatok-->
		<table>
            <!--Itt iterálunk végig az adathalmazon-->
		    <xsl:for-each select="page/ROWSET/ROW">
			<tr>
                <!--A tábla sorai egy-egy mezőből állnak, amely mezők tartalmazzák az összes
                    kapcsolódó adatot-->
			    <td>
                <!--Először a márka, amely nagybetűs, majd space-szel
                    elválasztva jöhet az eszköz neve-->
                <font class="uppercase"><xsl:value-of select="MARKA"/>&nbsp;<xsl:value-of select="NEV"/></font>
                <!--Az eszköz azonosítója zárójelek között-->
                (Eszköz kód: <xsl:value-of select="ESZK_AZON"/>)<br/>
                <!--Típus, vessző, és a napi költség, forintban-->
			    Típus: <xsl:value-of select="TIPUS"/><br/> 
			    Napi költség: <xsl:value-of select="NAPI_KSG"/> Ft<br/>
                <!--A sort a vásárlás dátuma zárja, amelynek formátuma szintén 
                    az SQL lekérdezésben már adva volt-->
			    Vásárlás ideje: <xsl:value-of select="VASARLAS"/>
                <hr/>
			    </td>
			</tr>
		    </xsl:for-each>
		</table>
	    </body>
	</html>
    </xsl:template>
</xsl:stylesheet>