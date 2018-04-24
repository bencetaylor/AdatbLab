#!/bin/bash

HOMEDIR=${HOME}
REPOSITORY_NAME=xml-xslt-demo
BRANCH_NAME=master
DOWNLOAD_BASE=https://github.com/adatlabor

if [ $INSECURE ]; then
  echo '/!\ Using insecure mode, be careful what you wish for!'
  WGET_OPTS=--no-check-certificate
fi


DOWNLOAD_URL=${DOWNLOAD_BASE}/${REPOSITORY_NAME}/archive/${BRANCH_NAME}.zip
ARCHIVE_DIRNAME=${REPOSITORY_NAME}-${BRANCH_NAME}
TARGET_DIRNAME=xml
WEB_DIRNAME=xml

XML_DOWNLOAD_BASE=https://db.bme.hu/r/xml

SAXON_PATH=/opt/saxon9he/saxon9he.jar
SAXON_DL_URL=https://sourceforge.net/projects/saxon/files/Saxon-HE/9.7/SaxonHE9-7-0-15J.zip/download

if [ -e "${TARGET_DIRNAME}" ]; then
  cat <<HERE
ERROR: ${TARGET_DIRNAME} directory already exists.

It seems that you have already deployed this demo application.
If you really need to re-deploy, please backup and do cleanup first!

Deploy aborted. Exiting.
HERE
  exit 2
fi

if [ "${EXERCISE_CATEGORY_NAME}" = "" ] ; then
  read -e -p "Enter exercise name, e.g. SHIPS: " EXERCISE_CATEGORY_NAME
fi

EXERCISE_CATEGORY_NAME_UC=$( echo "${EXERCISE_CATEGORY_NAME}" | tr a-z A-Z )

_ECN_CHECK=$( echo "${EXERCISE_CATEGORY_NAME_UC}" | grep -c '^[A-Z0-9]\+$' )
if [ "${_ECN_CHECK}x" != "1x" ] ; then
  cat <<HERE
ERROR: Exercise name seems invalid. You entered ${EXERCISE_CATEGORY_NAME}

Deploy aborted. Exiting.
HERE
  exit 4
fi

TEMPFILE=$( mktemp -t lab5xml.XXXXXXXXX )
wget ${WGET_OPTS} ${DOWNLOAD_URL} -O $TEMPFILE
if [ "$?" != "0" ] ; then
  echo
  echo !!! Failed to download files !!!
  echo
  exit 1
fi
unzip $TEMPFILE
mv ${ARCHIVE_DIRNAME} ${TARGET_DIRNAME}
chmod 701 ${TARGET_DIRNAME}
chmod 700 ${TARGET_DIRNAME}/src
chmod 705 ${TARGET_DIRNAME}/web
rm $TEMPFILE

# Symlink saxon9he.jar file, if it is there, otherwise download
if [ -f ${SAXON_PATH} ]; then
  ln -s ${SAXON_PATH} ${TARGET_DIRNAME}/lib/
  echo "Saxon XSLT library linked to the lib/ directory."
else
  TEMPFILE=$( mktemp -t lab5saxon.XXXXXXXXX )
  wget ${WGET_OPTS} ${SAXON_DL_URL} -O $TEMPFILE
  if [ "$?" != "0" ] ; then
    echo
    echo !!! Failed to download files !!!
    echo
    exit 1
  fi
  unzip $TEMPFILE saxon9he.jar -d ${TARGET_DIRNAME}/lib
  rm $TEMPFILE
fi

wget ${WGET_OPTS} ${XML_DOWNLOAD_BASE}/${EXERCISE_CATEGORY_NAME_UC} -O ${TARGET_DIRNAME}/${EXERCISE_CATEGORY_NAME_UC}.xml

# Preparing public_html
chmod 701 ${HOMEDIR}
mkdir -p ${HOMEDIR}/public_html
chmod 705 ${HOMEDIR}/public_html

WEBDIR=$( readlink -m ./${TARGET_DIRNAME}/web )
LINKNAME=$( readlink -m ${HOMEDIR}/public_html)/${WEB_DIRNAME}
if [ ! -d "${WEBDIR}" ]; then
  echo "ERROR: ${WEBDIR} not found. Deploy aborted. No cleanup done."
  exit 3
elif [ -e "${LINKNAME}" ] && [ ! -L "${LINKNAME}" ]; then
  echo "ERROR: ${LINKNAME} found but is not a symbolic link. Deploy aborted. No cleanup done."
  exit 3
elif [ ! -L "${LINKNAME}" ]; then
  ln -s ${WEBDIR} ${LINKNAME}
  L_RETVAL=$?
  if [ "${L_RETVAL}x" != "0x" ]; then
    echo "WARNING: something strange happened when creating ${LINKNAME} See the lines above."
  fi
fi

cat <<HERE
Working environment was successfully set!

Do not forget to:
* review possible warning messages above.
* change dir to workdir (cd ${TARGET_DIRNAME})
* ensure that Saxon JAR package is in the lib/ subdirectory.
* solve tasks under src/
* build using scons DS=${EXERCISE_CATEGORY_NAME_UC}
HERE
