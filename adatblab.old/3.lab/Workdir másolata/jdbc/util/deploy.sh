#!/bin/bash

HOMEDIR=${HOME}
REPOSITORY_NAME=jdbc-demo
BRANCH_NAME=master
DOWNLOAD_BASE=https://github.com/adatlabor

DOWNLOAD_URL=${DOWNLOAD_BASE}/${REPOSITORY_NAME}/archive/${BRANCH_NAME}.zip
ARCHIVE_DIRNAME=${REPOSITORY_NAME}-${BRANCH_NAME}
TARGET_DIRNAME=jdbc
WEB_DIRNAME=jdbc

OJDBC7_PATH=/oracle/product/12.1.0/dbhome_1/jdbc/lib/ojdbc7.jar

if [ -e "${TARGET_DIRNAME}" ]; then
  cat <<HERE
ERROR: ${TARGET_DIRNAME} directory already exists.

It seems that you have already deployed this demo application.
If you really need to re-deploy, please backup and do cleanup first!

Deploy aborted. Exiting.
HERE
  exit 2
fi

TEMPFILE=$( mktemp -t lab5jdbc.XXXXXXXXX )
wget ${DOWNLOAD_URL} -O $TEMPFILE
if [ "$?" != "0" ] ; then
  echo
  echo !!! Failed to download files !!!
  echo
  exit 1
fi
unzip $TEMPFILE
mv ${ARCHIVE_DIRNAME} ${TARGET_DIRNAME}
chmod 701 ${TARGET_DIRNAME}
chmod 705 ${TARGET_DIRNAME}/web
rm $TEMPFILE

# Symlink ojdbc7.jar driver
if [ -f ${OJDBC7_PATH} ]; then
  ln -s ${OJDBC7_PATH} ${TARGET_DIRNAME}/lib/
  echo "JDBC driver linked to the lib/ directory."
else
  echo "WARNING: You need to manually add ojdbc7.jar to the lib/ directory."
fi

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
* ensure that Oracle JDBC driver is in the lib/ subdirectory.
* create and edit conf/preproc.config.sh (by copying preproc.config.sh.sample)
* run ./util/preproc.sh
HERE
