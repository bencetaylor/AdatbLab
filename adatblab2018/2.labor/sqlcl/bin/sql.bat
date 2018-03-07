@echo off
REM ######################################################################## 
REM #  (@)sql.bat
REM #
REM #  Copyright 2014 by Oracle Corporation,
REM #  500 Oracle Parkway, Redwood Shores, California, 94065, U.S.A.
REM #  All rights reserved.
REM #
REM #  This software is the confidential and proprietary information
REM #  of Oracle Corporation.
REM # 
REM # NAME	sdsql
REM #
REM # DESC 	This script starts SqlCli.
REM #
REM # AUTHOR bamcgill
REM #
REM # MODIFIED	
REM #	bamcgill	21/03/2014	Created
REM #   bamcgill    17/07/2014  Simplified classpaths	
REM #   bamcgill    11/12/2014  Renamed script and contents 
REM #   bamcgill    16/01/2015  Renamed script and contents 
REM #   bamcgill    05/02/2015  Added headless to STD_OPTS to allow use of internal X server.
REM #   totierne    16/10/2015  Put classpath on the end - to allow timesten jars  
REM ########################################################################
REM # SQL_HOME=.

SET SQL_HOME=%~dp0..
REM SET DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000

REM Use internal simple X for awt in 
SET STD_ARGS=-Djava.awt.headless=true -Xss10M
if "%ORACLE_HOME%" == "" (SET CPFILE=%SQL_HOME%\lib\oracle.sqldeveloper.sqlcl.jar;%CLASSPATH%) ELSE (SET CPFILE=%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%ORACLE_HOME%\ojdbc6.jar;%SQL_HOME%\lib\oracle.sqldeveloper.sqlcl.jar;%CLASSPATH%)

REM SET CPFILE=%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%CPFILE%
REM SET MYCLASSPATH=%MYCLASSPATH%;C:\drivers\3rdparty\sqlserver\jtds-1.2.jar
REM SET MYCLASSPATH=%MYCLASSPATH%;C:\drivers\mysql\mysql-connector-java-5.1.24-bin.jar

java %JAVA_OPTS% %STD_ARGS% %DEBUG% -cp "%CPFILE%" oracle.dbtools.raptor.scriptrunner.cmdline.SqlCli %*
REM java -jar %JARFILE% %*

