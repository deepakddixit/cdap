#!/usr/bin/env bash

# Copyright © 2015 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

# Parcel name vars
# Version can be overridden here, otherwise it uses the detected CDAP version
PARCEL_BASE=${PARCEL_BASE:-CDAP}
PARCEL_SUFFIX=${PARCEL_SUFFIX:-el6}
PARCEL_VERSION=${PARCEL_VERSION}
PARCEL_ITERATION=${PARCEL_ITERATION:-1}

# Components should map to top-level directories: "cdap-${COMPONENT}"
COMPONENTS="cli gateway hbase-compat-0.96 hbase-compat-0.98 hbase-compat-1.0 hbase-compat-1.0-cdh hbase-compat-1.0-cdh5.5.0 kafka master security ui"

# Find our location and base repo directory
# Resolve links: $0 may be a link
PRG=${0}
# Need this for relative symlinks.
while [ -h ${PRG} ]; do
    ls=`ls -ld ${PRG}`
    link=`expr ${ls} : '.*-> \(.*\)$'`
    if expr ${link} : '/.*' > /dev/null; then
        PRG=${link}
    else
        PRG=`dirname ${PRG}`/${link}
    fi
done
cd `dirname ${PRG}`/.. >&-
DISTRIBUTIONS_HOME=`pwd -P`
cd `dirname ${DISTRIBUTIONS_HOME}` >&-
REPO_HOME=`pwd -P`

TARGET_DIR=${DISTRIBUTIONS_HOME}/target
STAGE_DIR=${TARGET_DIR}/parcel

echo "REPO_HOME: ${REPO_HOME}"

CDAP_HOME=${CDAP_HOME:-${REPO_HOME}}

function die {
  echo "ERROR: ${1}" 1>&2
  exit 1
}

# Ensure environment is as expected, with maven-produced staging directories
function validate_env {
  # First ensure we are in clean state and no partial parcel build exists
  if [ -d ${STAGE_DIR} ]; then
    die "Staging directory ${STAGE_DIR} already exists."
  fi

  # Check that all components have been built by maven with the same version
  local __component
  for __component in ${COMPONENTS}; do
    set_and_check_version ${__component}
  done
}

# Determine version by looking at $COMPONENT_HOME/target/stage-packaging/opt/cdap/$COMPONENT/VERSION
# and ensuring that all component versions match
function set_and_check_version {
  local __component=$1
  local __component_home="${CDAP_HOME}/cdap-${__component}"
  local __version_file="${__component_home}/target/stage-packaging/opt/cdap/${__component}/VERSION"
  if [ -f ${__version_file} ]; then
    local __component_version=`cat ${__version_file}`
    # Check that the VERSION file had some content
    if [ -z "${__component_version}" ]; then
      die "Component ${__component} has an undefined version, expected in ${__version_file}"
    fi
    # If this is the first iteration, set the expected ${VERSION}
    if [ -z "$VERSION" ]; then
      VERSION=${__component_version}
    fi 
    # Ensure that each component has the same version
    if [ "${VERSION}" != "${__component_version}" ] ; then
      die "Mismatched versions found. Expecting ${VERSION}, found component ${__component} version: ${__component_version}"
    fi
  else
    die "No version file found for component ${__component}, expecting ${__version_file}"
  fi
}

# Copy the CDAP component builds into our staging directory
function stage_artifacts {
  # Create staging directory
  mkdir -p ${STAGE_DIR}/${PARCEL_ROOT_DIR}

  # Copy each built component
  local __component
  for __component in ${COMPONENTS}; do
    local __component_home="${CDAP_HOME}/cdap-${__component}"
    cp -fpPR ${__component_home}/target/stage-packaging/opt/cdap/* ${STAGE_DIR}/${PARCEL_ROOT_DIR}/.
  done
}

# Copy the parcel metadata from the repo into our staging directory
function stage_parcel_bits {

  # Copy the shared default /etc/conf.dist dir
  mkdir -p ${STAGE_DIR}/${PARCEL_ROOT_DIR}/etc/cdap/conf.dist
  cp -fpPR ${DISTRIBUTIONS_HOME}/src/etc/cdap/conf.dist/* ${STAGE_DIR}/${PARCEL_ROOT_DIR}/etc/cdap/conf.dist/.

  # Copy the parcel-specific meta dir
  local __meta_dir=${DISTRIBUTIONS_HOME}/src/parcel/meta
  cp -fpPR ${__meta_dir} ${STAGE_DIR}/${PARCEL_ROOT_DIR}/.

  # Substitute our version
  sed -i -e "s#{{VERSION}}#${PARCEL_VERSION_ITERATION}#" ${STAGE_DIR}/${PARCEL_ROOT_DIR}/meta/parcel.json
}

# Create the parcel via tar
function generate_parcel {
  # detect bsdtar or not
  if tar --version | grep --quiet bsdtar ; then
    # supress extended attribute metadata
    export COPYFILE_DISABLE=1
    # bsdtar version prevalent on Mac OS X does not support --uname or --gname
    cmd="tar czf ${TARGET_DIR}/${PARCEL_NAME} -C ${STAGE_DIR} ${PARCEL_ROOT_DIR}/"
  else
    cmd="tar czf ${TARGET_DIR}/${PARCEL_NAME} -C ${STAGE_DIR} ${PARCEL_ROOT_DIR}/ --owner=root --group=root"
  fi
  $cmd
  local __ret=$?
  if [ $__ret -ne 0 ]; then
    die "Tar generation unsuccessful"
  else
    echo "Generated ${TARGET_DIR}/${PARCEL_NAME}"
  fi
}

# Scp the parcel somewhere, optional
function scp_parcel {
  PARCEL_SCP_OPTIONS="-oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null -oLogLevel=ERROR"
  echo "Creating remote directory"
  ssh ${PARCEL_SCP_OPTIONS} ${PARCEL_SCP_USER}@${PARCEL_SCP_HOST} mkdir -p ${PARCEL_SCP_BASE_PATH}/cdap/${VERSION}
  echo "Copying ${TARGET_DIR}/${PARCEL_NAME} to remote host"
  scp ${PARCEL_SCP_OPTIONS} ${TARGET_DIR}/${PARCEL_NAME} ${PARCEL_SCP_USER}@${PARCEL_SCP_HOST}:${PARCEL_SCP_BASE_PATH}/cdap/${VERSION}
  local __ret=$?
  if [ $__ret -ne 0 ]; then
    die "SCP unsuccessful"
  else
    echo "SCP successful"
  fi
}

# Do stuff

echo "Starting Parcel Build"

validate_env

PARCEL_VERSION_ITERATION="${PARCEL_VERSION:-${VERSION}}-${PARCEL_ITERATION}"
echo "Using version: ${PARCEL_VERSION_ITERATION}"
PARCEL_ROOT_DIR="${PARCEL_BASE}-${PARCEL_VERSION_ITERATION}"
PARCEL_NAME="${PARCEL_BASE}-${PARCEL_VERSION_ITERATION}-${PARCEL_SUFFIX}.parcel"

stage_artifacts

stage_parcel_bits

generate_parcel

# Optional SCP
if [ -z "${PARCEL_SCP_USER}" ] || [ -z "${PARCEL_SCP_HOST}" ] || [ -z "${PARCEL_SCP_BASE_PATH}" ]; then
  echo "Skipping SCP.  Set the following env vars to enable parcel SCP: PARCEL_SCP_USER, PARCEL_SCP_HOST, PARCEL_SCP_BASE_PATH"
else
  scp_parcel
fi
