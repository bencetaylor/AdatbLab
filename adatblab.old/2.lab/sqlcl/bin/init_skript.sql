DROP TABLE shipments CASCADE CONSTRAINTS PURGE;
DROP TABLE orders CASCADE CONSTRAINTS PURGE;
DROP TABLE vehicles CASCADE CONSTRAINTS PURGE;


CREATE TABLE vehicles (
vehicle_id NUMBER(6) NOT NULL,
numberplate NCHAR(6) NOT NULL,
vehicle_type NCHAR(1) DEFAULT 'C' NOT NULL,
purchase_date DATE DEFAULT SYSDATE NOT NULL,
maintenance_date DATE,
capacity NUMBER(5) NOT NULL,
CONSTRAINT
    pk_vehicle_id PRIMARY KEY (vehicle_id),
CONSTRAINT
    uniq_vehicle_numberplate UNIQUE (numberplate),
CONSTRAINT
    chk_vehicle_numberplate_upper CHECK (numberplate=UPPER(numberplate)),
CONSTRAINT
    chk_vehicle_vehicle_type CHECK ( UPPER(vehicle_type) IN ('D', 'C', 'T', 'L', 'A') ),
CONSTRAINT
    chk_vehicle_year_of_purchase CHECK (purchase_date >= TO_DATE('2000.01.01.', 'YYYY.MM.DD.') ),
CONSTRAINT
    chk_vehicle_capacity CHECK ( capacity > 0 )
);

CREATE TABLE orders (
order_id NVARCHAR2(11) NOT NULL,
description NVARCHAR2(48) NOT NULL,
vehicle_type NCHAR(1) NOT NULL,
quantity NUMBER(5) NOT NULL,
origin NVARCHAR2(100) NOT NULL,
destination NVARCHAR2(100) NOT NULL,
order_date DATE DEFAULT SYSDATE NOT NULL,
deadline_date DATE NOT NULL,
comment_text NVARCHAR2(500),
CONSTRAINT
    pk_order_id PRIMARY KEY (order_id),
CONSTRAINT
    chk_order_id CHECK ( REGEXP_LIKE(order_id, '[0-9]{4}\/[0-9]{6}') ),
CONSTRAINT
    chk_order_quantity CHECK ( quantity > 0 ),
-- Darabáru: (D)ryfreight
-- Konténerszállító: (C)ontainertruck
-- Üzemanyag-szállító: (T)anker truck
-- Állatszállító: (L)ivestocktruck
-- Gépkocsiszállító: (A)utotransporter
CONSTRAINT
    chk_order_vehicle_type CHECK ( UPPER(vehicle_type) IN ('D', 'C', 'T', 'L', 'A') ),
CONSTRAINT
    chk_order_deadline_relation CHECK (order_date <= deadline_date)
);

CREATE TABLE shipments (
shipment_id NUMBER(6) NOT NULL,
order_id NVARCHAR2(11) NOT NULL,
vehicle_id NUMBER(6) NOT NULL,
quantity NUMBER(5),
origin NVARCHAR2(100) NOT NULL,
destination NVARCHAR2(100) NOT NULL,
departure_date DATE,
arrival_date DATE,
state NCHAR(1) NOT NULL,
CONSTRAINT
    pk_delivery_id PRIMARY KEY (shipment_id),
CONSTRAINT
    fk_shipment_order FOREIGN KEY (order_id) REFERENCES orders ON DELETE CASCADE,
CONSTRAINT
    fk_shipment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles ON DELETE CASCADE,
CONSTRAINT
    chk_shipment_dates CHECK (
        departure_date IS NULL AND arrival_date IS NULL OR
        departure_date IS NOT NULL AND arrival_date IS NULL OR
        departure_date IS NOT NULL AND arrival_date IS NOT NULL AND departure_date < arrival_date
    ),
CONSTRAINT
    chk_shipment_quantity CHECK ( quantity > 0 ),
-- Tervezett: (D)raft
-- Futó: (R)unning
-- Teljesített: (A)ccomplished
CONSTRAINT
    chk_shipment_state_type CHECK ( UPPER(state) IN ('D', 'R', 'A') )
);

INSERT ALL
INTO vehicles VALUES (0,'BEO860','D',TO_DATE('2000.07.01.', 'YYYY.MM.DD.'),TO_DATE('2005.09.07.', 'YYYY.MM.DD.'),10000)
INTO vehicles VALUES (1,'FKQ010','D',TO_DATE('2001.07.16.', 'YYYY.MM.DD.'),TO_DATE('2007.01.29.', 'YYYY.MM.DD.'),12000)
INTO vehicles VALUES (2,'TWQ766','D',TO_DATE('2002.08.06.', 'YYYY.MM.DD.'),TO_DATE('2010.02.02.', 'YYYY.MM.DD.'),20000)
INTO vehicles VALUES (3,'DZD729','D',TO_DATE('2013.01.31.', 'YYYY.MM.DD.'),TO_DATE('2015.01.30.', 'YYYY.MM.DD.'),16000)
INTO vehicles VALUES (10,'JHE848','C',TO_DATE('2013.10.31.', 'YYYY.MM.DD.'),NULL,44000)
INTO vehicles VALUES (11,'ECH117','C',TO_DATE('2006.06.11.', 'YYYY.MM.DD.'),TO_DATE('2009.09.06.', 'YYYY.MM.DD.'),48000)
INTO vehicles VALUES (12,'QKQ609','C',TO_DATE('2008.12.13.', 'YYYY.MM.DD.'),NULL,64000)
INTO vehicles VALUES (13,'ZMW794','C',TO_DATE('2008.10.21.', 'YYYY.MM.DD.'),NULL,36000)
INTO vehicles VALUES (14,'NCZ551','C',TO_DATE('2007.07.08.', 'YYYY.MM.DD.'),TO_DATE('2008.12.12.', 'YYYY.MM.DD.'),48000)
INTO vehicles VALUES (20,'AVQ374','T',TO_DATE('2007.09.26.', 'YYYY.MM.DD.'),NULL,12000)
INTO vehicles VALUES (21,'FAR836','T',TO_DATE('2006.06.12.', 'YYYY.MM.DD.'),TO_DATE('2009.08.13.', 'YYYY.MM.DD.'),16000)
INTO vehicles VALUES (30,'AJJ538','L',TO_DATE('2007.05.19.', 'YYYY.MM.DD.'),TO_DATE('2007.07.05.', 'YYYY.MM.DD.'),24000)
INTO vehicles VALUES (40,'XTZ033','A',TO_DATE('2000.04.07.', 'YYYY.MM.DD.'),TO_DATE('2007.06.03.', 'YYYY.MM.DD.'),8)
INTO vehicles VALUES (41,'FFE749','A',TO_DATE('2013.10.31.', 'YYYY.MM.DD.'),NULL,10)
SELECT * FROM DUAL;

INSERT ALL
INTO orders VALUES ('2017/000001','bútor','D',10000, 'Berlin (DE)', 'Budapest (HU)',TO_DATE('2017.01.02.','YYYY.MM.DD.'),TO_DATE('2017.05.10. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000002','építőanyag','D',8000, 'Kolozsvár (RO)', 'Budapest (HU)',TO_DATE('2017.01.04.','YYYY.MM.DD.'),TO_DATE('2017.04.11. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000003','építőanyag','D',20000, 'Milánó (IT)', 'Szeged (HU)',TO_DATE('2017.01.14.','YYYY.MM.DD.'),TO_DATE('2017.03.21. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000004','élelmiszer <tartós>','D',15000, 'Belgrád (RS)', 'Pozsony (SK)',TO_DATE('2017.01.23.','YYYY.MM.DD.'),TO_DATE('2017.04.12. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000005','bútoralapanyag','D',30000, 'Varsó (PL)', 'Debrecen (HU)',TO_DATE('2017.01.31.','YYYY.MM.DD.'),TO_DATE('2017.02.22. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000006','élelmiszer <ital>','D',20000, 'Prága (CZ)', 'Zágráb (HR)',TO_DATE('2017.01.31.','YYYY.MM.DD.'),TO_DATE('2017.05.01. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000011','műszaki cikk','C',20000, 'Bécs (AT)', 'Kassa (SK)',TO_DATE('2017.01.05.','YYYY.MM.DD.'),TO_DATE('2017.02.10. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000012','papíráru, <nyers>','C',34000, 'Prága (CZ)', 'Belgrád (RS)',TO_DATE('2017.01.21.','YYYY.MM.DD.'),TO_DATE('2017.03.08. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000013','vegyi áru (festék'|| chr(38) ||'higító)','C',12000, 'Budapest (HU)', 'München (DE)',TO_DATE('2017.01.25.','YYYY.MM.DD.'),TO_DATE('2017.04.10. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000021','üzemanyag','T',16000, 'Zágráb (HR)', 'Varsó (PL)',TO_DATE('2017.01.15.','YYYY.MM.DD.'),TO_DATE('2017.03.16. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000022','üzemanyag','T',42000, 'Kolozsvár (RO)', 'Milánó (IT)',TO_DATE('2017.01.18.','YYYY.MM.DD.'),TO_DATE('2017.03.12. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000031','baromfi','L',16000, 'Krakkó (PL)', 'Szeged (HU)',TO_DATE('2017.02.15.','YYYY.MM.DD.'),TO_DATE('2017.04.12. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000032','versenyló','L',4000, 'Debrecen (HU)', 'Milánó (IT)',TO_DATE('2017.02.16.','YYYY.MM.DD.'),TO_DATE('2017.02.28. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000033','sertés','L',20000, 'Szeged (HU)', 'Berlin (DE)',TO_DATE('2017.02.28.','YYYY.MM.DD.'),TO_DATE('2017.04.13. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000041','használt autó','A',10, 'München (DE)', 'Budapest (HU)',TO_DATE('2017.01.25.','YYYY.MM.DD.'),TO_DATE('2017.04.24. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
INTO orders VALUES ('2017/000042','hatóságilag lefoglalt járművek','A',15, 'Belgrád (RS)', 'Berlin (DE)',TO_DATE('2017.01.28.','YYYY.MM.DD.'),TO_DATE('2017.03.12. 12:00','YYYY.MM.DD. HH24:MI'),NULL)
SELECT * FROM DUAL;

INSERT ALL
INTO shipments VALUES (10,'2017/000001',0,10000, 'Berlin (DE)', 'Bécs (AT)',TO_DATE('2017.01.05. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.06. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (11,'2017/000001',0,10000, 'Bécs (AT)', 'Budapest (HU)',TO_DATE('2017.01.06. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.07. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (20,'2017/000002',0,8000, 'Kolozsvár (RO)', 'Budapest (HU)',TO_DATE('2017.01.08. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.09. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (30,'2017/000003',2,20000, 'Milánó (IT)', 'Zágráb (HR)',TO_DATE('2017.01.14. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.15. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (31,'2017/000003',2,20000, 'Zágráb (HR)', 'Szeged (HU)',TO_DATE('2017.01.15. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.16. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (40,'2017/000004',1,15000, 'Belgrád (RS)', 'Budapest (HU)',TO_DATE('2017.02.10. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.11. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (41,'2017/000004',1,15000, 'Belgrád (RS)', 'Budapest (HU)',TO_DATE('2017.04.11. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.04.12. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (50,'2017/000005',0,20000, 'Varsó (PL)', 'Szeged (HU)',TO_DATE('2017.02.03. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.04. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (51,'2017/000005',0,10000, 'Varsó (PL)', 'Szeged (HU)',TO_DATE('2017.02.05. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.06. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (52,'2017/000005',0,20000, 'Szeged (HU)', 'Debrecen (HU)',TO_DATE('2017.02.20. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.21. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (53,'2017/000005',0,10000, 'Szeged (HU)', 'Debrecen (HU)',TO_DATE('2017.02.21. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.22. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (60,'2017/000006',2,20000, 'Prága (CZ)', 'Zágráb (HR)',TO_DATE('2017.03.31. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.04.30. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (110,'2017/000011',10,20000, 'Bécs (AT)', 'Kassa (SK)',TO_DATE('2017.01.06. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.08. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (120,'2017/000012',10,34000, 'Prága (CZ)', 'Budapest (HU)',TO_DATE('2017.01.22. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.23. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (121,'2017/000012',10,34000, 'Budapest (HU)', 'Belgrád (RS)',TO_DATE('2017.03.07. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.03.08. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (130,'2017/000013',10,12000, 'Budapest (HU)', 'München (DE)',TO_DATE('2017.01.26. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.27. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (210,'2017/000021',21,16000, 'Zágráb (HR)', 'Varsó (PL)',TO_DATE('2017.03.14. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.03.16. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (220,'2017/000022',20,12000, 'Kolozsvár (RO)', 'Milánó (IT)',TO_DATE('2017.01.19. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.22. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (221,'2017/000022',20,12000, 'Kolozsvár (RO)', 'Milánó (IT)',TO_DATE('2017.01.24. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.01.26. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (222,'2017/000022',20,12000, 'Kolozsvár (RO)', 'Milánó (IT)',TO_DATE('2017.03.05. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.03.07. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (223,'2017/000022',20,6000, 'Kolozsvár (RO)', 'Milánó (IT)',TO_DATE('2017.03.10. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.03.12. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (310,'2017/000031',30,16000, 'Krakkó (PL)', 'Szeged (HU)',TO_DATE('2017.04.10. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.04.12. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (320,'2017/000032',30,30000, 'Szeged (HU)', 'Berlin (DE)',TO_DATE('2017.04.11. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.04.13. 08:00','YYYY.MM.DD. HH24:MI'),'D')
INTO shipments VALUES (410,'2017/000041',40,8, 'München (DE)', 'Budapest (HU)',TO_DATE('2017.02.22. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.26. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (411,'2017/000041',40,2, 'München (DE)', 'Budapest (HU)',TO_DATE('2017.02.22. 08:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.26. 16:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (420,'2017/000042',40,8, 'Belgrád (RS)', 'Budapest (HU)',TO_DATE('2017.02.04. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.05. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (421,'2017/000042',40,7, 'Belgrád (RS)', 'Budapest (HU)',TO_DATE('2017.02.07. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.08. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (422,'2017/000042',41,8, 'Budapest (HU)', 'Berlin (DE)',TO_DATE('2017.02.06. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.08. 08:00','YYYY.MM.DD. HH24:MI'),'R')
INTO shipments VALUES (423,'2017/000042',41,7, 'Budapest (HU)', 'Berlin (DE)',TO_DATE('2017.02.12. 16:00','YYYY.MM.DD. HH24:MI'),TO_DATE('2017.02.14. 08:00','YYYY.MM.DD. HH24:MI'),'R')
SELECT * FROM DUAL;

COMMIT;