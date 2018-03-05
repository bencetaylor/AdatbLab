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

prompt <task n="3.1">
prompt <![CDATA[

prompt ]]>
prompt </task>
set feedback on

prompt <task n="4.1">
prompt <![CDATA[

prompt ]]>
prompt </task>
set feedback off

prompt <task n="5.1">
prompt <![CDATA[

prompt ]]>
prompt </task>
prompt </tasks>