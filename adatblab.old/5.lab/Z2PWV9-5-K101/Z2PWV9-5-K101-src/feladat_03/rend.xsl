<?xml version="1.0" encoding="UTF-8"?>
<!--Ez a fájl a harmadik feladat megoldásához tartozik-->
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
        <xsl:choose>
            <xsl:when test="//xsql-error">
                <p>Adatbázishiba:
                    <xsl:value-of select="//xsql-error/message[1]"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <table>
                    <xsl:for-each select="page/ROWSET/ROW">
                    <tr>
                        <td>
                        <!--Itt kezdődik a feltételes elágazás, amely kiértékeli, hogy 10 évnél idősebb-e
                            az adott eszköz-->
                        <xsl:choose>
                            <!--A teszt annyit csinál, hogy megnézi, hogy:
                                    - ha a vásárlás dátumának éve kisebb, mint a jelenlegi év minusz 10
                                    - vagy, ha a vásárlás dátumának éve egyenlő a jelenlegi év minusz 10-zel és
                                        a vásárlás dátumának hónapja kisebb, mint a jelenlegi hónap
                                    - vagy, ha a vásárlás dátumának éve egyenlő a jelenlegi év minusz 10-zel és
                                        a vásárlás dátumának hónapja egyenlő a jelenlegi hónappal és
                                        a vásárlás napja kisebb, mint a jelenlegi nap
                                és ha igen, akkor az adott eszköz idősebb 10 évnél-->
                            <xsl:when test="(number(substring(VASARLAS,1,4)) &lt; (year-from-date(current-date())-10)) or
                                            (number(substring(VASARLAS,1,4)) = (year-from-date(current-date())-10) and
                                                number(substring(VASARLAS,5,2)) &lt; month-from-date(current-date())) or
                                            (number(substring(VASARLAS,1,4)) = (year-from-date(current-date())-10)
                                                and number(substring(VASARLAS,5,2)) = month-from-date(current-date())
                                                and number(substring(VASARLAS,9,2)) &lt; day-from-date(current-date()))">
                                <font class="uppercase"><xsl:value-of select="MARKA"/>&nbsp;<xsl:value-of select="NEV"/></font>
                                (Eszköz kód: <xsl:value-of select="ESZK_AZON"/>)<br/>
                                Típus: <xsl:value-of select="TIPUS"/><br/>
                                <!--Ilyenkor a napi költségnek a 0.9 szeresét mutatjuk a felhasználónak
                                    illetve az eredeti napi költséget, áthúzva-->
                                Napi költség: <strike><xsl:value-of select="NAPI_KSG"/> Ft</strike><br/>
                                <!--A változást piros színnel ki is emeltem-->
                                <font color="ff0000" size="5">
                                Akciós ár: <xsl:value-of select="round(NAPI_KSG * 0.9)"/> Ft<br/>
                                Megtakarítás: <xsl:value-of select="round(NAPI_KSG - NAPI_KSG *0.9)"/> Ft<br/>
                                </font>
                                Vásárlás ideje: <xsl:value-of select="VASARLAS"/>
                            </xsl:when>
                            <!--Ha a teszt feltétele nem teljesült, 0.95-tel szorozzuk be a z eszköz napi költségét-->
                            <xsl:otherwise>
                                <font class="uppercase"><xsl:value-of select="MARKA"/>&nbsp;<xsl:value-of select="NEV"/></font>
                                (Eszköz kód: <xsl:value-of select="ESZK_AZON"/>)<br/>
                                Típus: <xsl:value-of select="TIPUS"/><br/>
                                Napi költség: <strike><xsl:value-of select="NAPI_KSG"/> Ft</strike><br/>
                                <font color="ff0000" size="5">
                                Akciós ár: <xsl:value-of select="round(NAPI_KSG * 0.95)"/> Ft<br/>
                                Megtakarítás: <xsl:value-of select="round(NAPI_KSG - NAPI_KSG * 0.95)"/> Ft<br/>
                                </font>
                                Vásárlás ideje: <xsl:value-of select="VASARLAS"/>
                            </xsl:otherwise>
                        </xsl:choose>
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