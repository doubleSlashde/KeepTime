#!/bin/bash
H2_VERSION=1.4.197
DB_URL=jdbc:h2:file:./db/$H2_VERSION/keeptime-h2-db;DB_CLOSE_ON_EXIT=FALSE
DB_USER=sa
DB_PASSWD=
SQL_BACKUP_FILENAME=KeepTime_database-export_H2-version-$H2_VERSION.sql

echo "Script to export the content of H2 databse from KeepTime version 1.2.0-SNAPSHOT to .sql file."
echo "The script should be placed next to the .jar file."
read -p "Press enter to continue"

echo -e "\nExtracting H2 driver from KeepTime .jar file."
DRIVER_JAR_FROM_KEEPTIME=BOOT-INF/lib/h2-$H2_VERSION.jar
jar xfv keeptime-1.2.0-SNAPSHOT.jar $DRIVER_JAR_FROM_KEEPTIME

echo -e "\nTriggering database backup."
java -cp $DRIVER_JAR_FROM_KEEPTIME org.h2.tools.Script -url $DB_URL -user $DB_USER -script $SQL_BACKUP_FILENAME -options DROP

echo -e "\nCleanup of extracted .jar file."
rm -rv BOOT-INF

echo -e "\nIf no error occured the export has succeeded."
echo "The exported file was created at '$(pwd)/$SQL_BACKUP_FILENAME'."
echo -e "\nYou can now import this file in a new version of KeepTime.\n"

read -p "All set. Press enter to exit script."