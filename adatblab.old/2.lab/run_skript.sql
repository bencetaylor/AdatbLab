set echo off
set verify off
alter session set NLS_DATE_FORMAT='YYYY-MM-DD';
set serveroutput on
set feedback off
DROP TABLE comments;
prompt <tasks>

prompt <task n="1.1">
prompt <![CDATA[
ALTER TABLE vehicles
ADD amort NUMBER(3) DEFAULT 100 NOT NULL;
prompt ]]>
prompt </task>

prompt <task n="1.2">
prompt <![CDATA[
ALTER TABLE orders
DROP COLUMN comment_text;

CREATE TABLE comments (
  comment_id NUMBER(6) NOT NULL,
  order_id NVARCHAR2(11) NOT NULL,
  comment_text NVARCHAR2(500),
  importance NUMBER(1) NOT NULL);

ALTER TABLE comments
ADD CONSTRAINT orders_PK
PRIMARY KEY (comment_id);

ALTER TABLE comments
ADD CONSTRAINT orders_FK
FOREIGN KEY (order_id)
REFERENCES orders(order_id);
prompt ]]>
prompt </task>

prompt <task n="2.1">
prompt <![CDATA[
SELECT * FROM orders;
prompt ]]>
prompt </task>

prompt <task n="2.2">
prompt <![CDATA[
SELECT *
FROM vehicles
ORDER BY NUMBERPLATE;
prompt ]]>
prompt </task>

prompt <task n="2.3">
prompt <![CDATA[
SELECT NUMBERPLATE, DECODE(MAINTENANCE_DATE, NULL, 'SOHA', MAINTENANCE_DATE) AS maintenance_date
FROM vehicles;
prompt ]]>
prompt </task>

prompt <task n="2.4">
prompt <![CDATA[
SELECT SHIPMENT_ID
FROM shipments
WHERE (ARRIVAL_DATE - DEPARTURE_DATE) > 2
ORDER BY DEPARTURE_DATE;
prompt ]]>
prompt </task>

prompt <task n="2.5">
prompt <![CDATA[
select order_id
from ORDERS
where TO_CHAR(DEADLINE_DATE,'DY','nls_date_language=english') in ('SAT', 'SUN')
order by order_id;
prompt ]]>
prompt </task>

prompt <task n="2.6">
prompt <![CDATA[
SELECT orders.ORDER_ID, ROUND((TO_DATE(ARRIVAL_DATE, 'YYYY.MM.DD.')-TO_DATE(DEADLINE_DATE, 'YYYY.MM.DD.')), 2) AS days
FROM orders
INNER JOIN shipments
ON orders.ORDER_ID = shipments.ORDER_ID
WHERE (STATE = 'D') AND (DEADLINE_DATE < ARRIVAL_DATE);
prompt ]]>
prompt </task>

prompt <task n="2.7">
prompt <![CDATA[
SELECT NUMBERPLATE, COUNT(*) AS c,SUM(24 * (TO_DATE(ARRIVAL_DATE, 'YYYY.MM.DD.HH24.MI')-TO_DATE(DEPARTURE_DATE, 'YYYY.MM.DD.HH24.MI'))) AS hours
FROM vehicles
INNER JOIN shipments
ON vehicles.VEHICLE_ID = shipments.VEHICLE_ID
GROUP BY NUMBERPLATE
ORDER BY hours;
prompt ]]>
prompt </task>

prompt <task n="2.8">
prompt <![CDATA[
SELECT MAX(PURCHASE_DATE) AS oldest, MIN(PURCHASE_DATE) AS newest
FROM shipments sh
INNER JOIN orders o ON o.ORDER_ID = sh.ORDER_ID
INNER JOIN vehicles ve ON ve.VEHICLE_ID = sh.VEHICLE_ID
WHERE o.ORIGIN LIKE '%(HU)' or o.DESTINATION LIKE '%(HU)';
prompt ]]>
prompt </task>

prompt <task n="3.1">
prompt <![CDATA[
SELECT ORDER_ID
FROM orders
WHERE DEADLINE_DATE = (
SELECT  MIN(DEADLINE_DATE)
FROM orders
WHERE DESTINATION LIKE '%(HU)%');
prompt ]]>
prompt </task>

prompt <task n="3.2">
prompt <![CDATA[
SELECT VEHICLE_ID
FROM
(SELECT VEHICLE_ID, COUNT(SHIPMENT_ID) AS c
FROM shipments
GROUP BY VEHICLE_ID
ORDER BY c DESC)
WHERE ROWNUM = 1;
prompt ]]>
prompt </task>

prompt <task n="3.3">
prompt <![CDATA[

prompt ]]>
prompt </task>

prompt <task n="3.4">
prompt <![CDATA[
SELECT orders.ORDER_ID, QUANTITY, NVL(avr, 0) AS AVG
FROM orders,
(SELECT ORDER_ID, AVG(QUANTITY) AS avr
FROM shipments
GROUP BY ORDER_ID) t
WHERE orders.ORDER_ID = t.ORDER_ID(+)
ORDER BY orders.ORDER_ID;
prompt ]]>
prompt </task>
set feedback on

prompt <task n="4.1">
prompt <![CDATA[
INSERT INTO COMMENTS
VALUES(000001, '2017/000001', 'Erkezes elott telefonaljon', 5);
prompt ]]>
prompt </task>

prompt <task n="4.2">
prompt <![CDATA[
UPDATE VEHICLES
SET AMORT = 50
WHERE PURCHASE_DATE < TO_DATE('2006-01-01', 'YYYY.MM.DD');
prompt ]]>
prompt </task>

prompt <task n="4.3">
prompt <![CDATA[
DELETE FROM SHIPMENTS
WHERE SHIPMENT_ID IN (
SELECT SHIPMENT_ID
FROM SHIPMENTS
WHERE TO_DATE(ARRIVAL_DATE, 'YYYY.MM.DD')-TO_DATE(DEPARTURE_DATE, 'YYYY.MM.DD') >= 7);
prompt ]]>
prompt </task>
set feedback off
prompt </tasks>