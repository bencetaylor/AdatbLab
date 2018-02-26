#!/bin/bash

# WARNING!
#
# This is an _experimental_ shell script which can be used to automatically modify
# the name, title, description of the sample Java Web Start application in the
# appropriate files. In addition, student name, neptun code and
# name of ojdbc driver can also be specified.
#
# Use it on your own risk!

# include local config, if exists, exit otherwise.
LOCAL_CONFIGFILE=`dirname $0`/../conf/preproc.config.sh
if [ -f ${LOCAL_CONFIGFILE} ]; then
  . ${LOCAL_CONFIGFILE}
else
  cat <<HERE

!!! ${LOCAL_CONFIGFILE} not found. !!!

Perhaps you forgot to create it by copying ${LOCAL_CONFIGFILE}.sample

Aborting preprocessing.

HERE

  exit 2
fi

#download custom files for given exercise
DOWNLOAD_URL=http://db.bme.hu/r/jdbc/${exercise}.zip;
TEMPFILE=$( mktemp -t lab5jdbc.XXXXXXXXX )
wget ${DOWNLOAD_URL} -O $TEMPFILE --no-check-certificate
if [ "$?" != "0" ] ; then
  echo
  echo !!! Failed to download files !!!
  echo
  exit 1
fi
unzip $TEMPFILE
rm $TEMPFILE

# Sed patterns
declare -a SED_PATTERNS;
SED_PATTERNS=('\$\$student_name\$\$' '\$\$neptun\$\$' '\$\$title\$\$' '\$\$initpage_title\$\$' '\$\$desc\$\$' '\$\$app_name\$\$' '\$\$ojdbc_driver\$\$');

# User-specified params
declare -a PARAMS;
PARAMS=("$student_name" "$neptun" "$title" "$initpage_title" "$desc" "$app_name" "$ojdbc_driver");

# File paths
declare -a FILEPATHS;
FILEPATHS=('./web/index.html' './conf/MANIFEST.MF' './web/application.jnlp');

# Replace placeholders in files with the appropriate parameters
for FILEPATH in "${FILEPATHS[@]}"
do
    SED_PATTERN="";

    for i in {0..7}
    do
        SED_PATTERN+="s/"${SED_PATTERNS[$i]}"/"${PARAMS[$i]}"/g;";
    done

    #echo -e $SED_PATTERN'\n';
    sed -i -e "$SED_PATTERN" "${FILEPATH}";
done

echo "Preprocessing done."