#!/bin/bash
H2_VERSION=1.4.197
DB_URL=jdbc:h2:file:./db/$H2_VERSION/keeptime-h2-db;DB_CLOSE_ON_EXIT=FALSE
DB_USER=sa
DB_PASSWD=
SQL_BACKUP_FILENAME=KeepTime_database-export_H2-version-$H2_VERSION.sql

echo Get H2 jar from KeepTime jar ....
jar xf keeptime-1.2.0-SNAPSHOT.jar
rm -r org 
rm -r META-INF
echo 

DRIVER_JAR_FROM_KEEPTIME=BOOT-INF/lib/h2-$H2_VERSION.jar

echo Filename : $SQL_BACKUP_FILENAME
echo 
echo Location : 
pwd  
echo 

     
	java -cp $DRIVER_JAR_FROM_KEEPTIME org.h2.tools.Script -url $DB_URL -user $DB_USER -script $SQL_BACKUP_FILENAME -options DROP
rm -r BOOT-INF

echo delete Files from unzipping KeepTime
echo Export succeeded 