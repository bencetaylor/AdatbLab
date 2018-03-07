set echo off
set verify off
alter session set NLS_DATE_FORMAT='YYYY-MM-DD';
set serveroutput on
set feedback off
DROP TABLE varos;
prompt <tasks>

prompt <task n="1.1">
prompt <![CDATA[
ALTER TABLE allomas
ADD teher NUMBER(1) DEFAULT 0
ADD CONSTRAINT TEHER_E CHECK
(REGEXP_LIKE(teher, '[0-1]{1}'))
ENABLE;
prompt ]]>
prompt </task>

prompt <task n="1.2">
prompt <![CDATA[
ALTER TABLE allomas
ADD varos_id number(5);

CREATE TABLE varos (
id NUMBER(5) not null,
nev NVARCHAR2(40) not null);

ALTER TABLE varos
ADD CONSTRAINT varos_PK
PRIMARY KEY (id);

ALTER TABLE allomas
ADD CONSTRAINT varos_FK
FOREIGN KEY (varos_id)
REFERENCES varos(id);
prompt ]]>
prompt </task>

prompt <task n="2.1">
prompt <![CDATA[
SELECT * FROM jarat;
prompt ]]>
prompt </task>

prompt <task n="2.2">
prompt <![CDATA[
SELECT VONATSZAM, TIPUS FROM jarat;
prompt ]]>
prompt </task>

prompt <task n="2.3">
prompt <![CDATA[
SELECT * 
FROM jarat
WHERE TIPUS='IC';
prompt ]]>
prompt </task>

prompt <task n="2.4">
prompt <![CDATA[
SELECT vonatszam, tipus, megjegyzes
FROM jarat
WHERE (nap LIKE '1%') and (megjegyzes IS NOT NULL) and (NVL(kezd, TO_DATE('2012-01-01', 'YYYY-MM-DD')) <= TO_DATE('2012-01-01', 'YYYY-MM-DD')) and (NVL(vege, TO_DATE('2012-06-15', 'YYYY-MM-DD')) >= TO_DATE('2012-06-15', 'YYYY-MM-DD'))
ORDER BY vonatszam;
prompt ]]>
prompt </task>

prompt <task n="2.5">
prompt <![CDATA[
SELECT vonatszam, tipus, DECODE(megjegyzes, NULL, 'nincs megjegyzés', megjegyzes) AS MEGJEGYZES
FROM jarat
ORDER BY vonatszam;
prompt ]]>
prompt </task>

prompt <task n="2.6">
prompt <![CDATA[
SELECT nev, varos FROM (SELECT nev, atlagutas/DECODE(SZTRAJKUTAS, 0, 1, SZTRAJKUTAS) as atlag, varos
FROM allomas
WHERE atlagutas/DECODE(SZTRAJKUTAS, 0, 1, SZTRAJKUTAS) > 0
ORDER BY atlag)
WHERE ROWNUM=1;
prompt ]]>
prompt </task>

prompt <task n="2.7">
prompt <![CDATA[
SELECT  DISTINCT allomas.varos as varos, DECODE(megall.vonatszam, NULL, 0, megall.vonatszam) as vonatszam
FROM allomas
LEFT OUTER JOIN megall
ON allomas.id = megall.allomas_id
WHERE allomas.varos IS NOT NULL
ORDER BY varos, vonatszam;
prompt ]]>
prompt </task>

prompt <task n="3.1">
prompt <![CDATA[
SELECT AVG(napok) AS atlag
FROM
(SELECT vonatszam, TRUNC(SYSDATE+1) - NVL(kezd, TO_DATE('2006-01-01', 'YYYY-MM-DD')) AS napok
FROM jarat
WHERE (vege IS NULL or vege > SYSDATE));
prompt ]]>
prompt </task>

prompt <task n="3.2">
prompt <![CDATA[
SELECT vonatszam, SUM((FLOOR(ind/100)*60 + MOD(ind, 100)) - (FLOOR(erk/100)*60 + MOD(erk, 100))) as eltoltott_ido
FROM megall
WHERE ind IS NOT NULL and erk IS NOT NULL
GROUP BY vonatszam
ORDER BY eltoltott_ido;
prompt ]]>
prompt </task>

prompt <task n="3.3">
prompt <![CDATA[
SELECT nev FROM
(SELECT nev, COUNT(eltoltott_ido)*2 as megallt, SUM(eltoltott_ido) as ido
FROM
(SELECT allomas.nev as nev, ((FLOOR(ind/100)*60 + MOD(ind, 100)) - (FLOOR(erk/100)*60 + MOD(erk, 100))) as eltoltott_ido
FROM allomas, megall
WHERE allomas.id = megall.allomas_id and ind IS NOT NULL and erk IS NOT NULL)
GROUP BY nev)
WHERE megallt = ido;
prompt ]]>
prompt </task>

prompt <task n="3.4">
prompt <![CDATA[
SELECT DISTINCT nev FROM megall, allomas
WHERE allomas.id = megall.allomas_id and nev != 'Szombathely' and vonatszam in
(SELECT vonatszam FROM megall, allomas
WHERE allomas.id = megall.allomas_id and allomas.nev = 'Szombathely');
prompt ]]>
prompt </task>
set feedback on

prompt <task n="4.1">
prompt <![CDATA[
INSERT INTO allomas VALUES (42,'Kecskemét','Kecskemét',4000,1500, 1, null);
prompt ]]>
prompt </task>

prompt <task n="4.2">
prompt <![CDATA[
UPDATE jarat SET megjegyzes = 'Minden nap'
WHERE vonatszam = 164;
prompt ]]>
prompt </task>

prompt <task n="4.3">
prompt <![CDATA[
INSERT INTO varos
(id, nev)
SELECT vid, nev FROM
(SELECT ROWNUM  as vid, varos as nev
  FROM 
  (SELECT DISTINCT varos FROM allomas)
  WHERE varos IS NOT NULL);
prompt ]]>
prompt </task>
set feedback off

prompt <task n="5.1">
prompt <![CDATA[
SELECT nev FROM
  (SELECT nev, ROUND(SUM(eltoltott_ido) / COUNT(eltoltott_ido), 1) as atlag_ido
  FROM
    (SELECT allomas.nev as nev, ((FLOOR(ind/100)*60 + MOD(ind, 100)) - (FLOOR(erk/100)*60 + MOD(erk, 100))) as eltoltott_ido
    FROM allomas, megall
    WHERE allomas.id = megall.allomas_id and ind IS NOT NULL and erk IS NOT NULL)
  GROUP BY nev) x,
  (SELECT (SUM((FLOOR(ind/100)*60 + MOD(ind, 100)) - (FLOOR(erk/100)*60 + MOD(erk, 100))) / COUNT(nev)) as ossz_atlag_ido
  FROM allomas INNER JOIN megall ON allomas.id = megall.allomas_id) y
WHERE x.atlag_ido > y.ossz_atlag_ido;
prompt ]]>
prompt </task>
prompt </tasks>