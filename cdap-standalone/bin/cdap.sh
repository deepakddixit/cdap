#!/usr/bin/env bash

#
# Copyright © 2014-2016 Cask Data, Inc.
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
#


# We need a larger PermSize for SparkProgramRunner to call SparkSubmit
if [ -d /opt/cdap ]; then
 CDAP_HOME=/opt/cdap; export CDAP_HOME
 DEFAULT_JVM_OPTS="-Xmx3072m -XX:MaxPermSize=256m"
else
 DEFAULT_JVM_OPTS="-Xmx2048m -XX:MaxPermSize=256m"
fi

# Add default JVM options here. You can also use JAVA_OPTS and CDAP_OPTS to pass JVM options to this script.
CDAP_OPTS="-XX:+UseConcMarkSweepGC -Djava.security.krb5.realm= -Djava.security.krb5.kdc= -Djava.awt.headless=true"

# Specifies Web App Path
UI_PATH=${UI_PATH:-"ui/server.js"}

APP_NAME="cask-cdap"
APP_BASE_NAME=`basename "$0"`


function program_is_installed {
  # set to 0 initially
  local return_=0
  # set to 0 if not found
  type $1 >/dev/null 2>&1 || { local return_=1; }
  # return value
  echo "$return_"
}

warn ( ) {
    echo "$*"
}

die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
cd "`dirname \"$PRG\"`/.." >&-
APP_HOME="`pwd -P`"

# In order to ensure that we can do hacks, need to make sure classpath is sorted
# so that cdap jars are placed earlier in the classpath than twill or hadoop jars
CLASSPATH=$(find "${APP_HOME}/lib" -type f | sort | tr '\n' ':')
CLASSPATH="${CLASSPATH}:${APP_HOME}/conf/"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# java version check
JAVA_VERSION=`$JAVACMD -version 2>&1 | grep "java version" | awk '{print $3}' | awk -F '.' '{print $2}'`
if [ $JAVA_VERSION -ne 7 ] && [ $JAVA_VERSION -ne 8 ]; then
  die "ERROR: Java version not supported
Please install Java 7 or 8 - other versions of Java are not supported."
fi

# Check Node.js installation
NODE_VERSION_MINIMUM="v0.10.36"
NODE_INSTALL_STATUS=$(program_is_installed node)
if [ "x$NODE_INSTALL_STATUS" == "x1" ]; then
  die "Node.js is not installed
Please install Node.js: we recommend any version of Node.js starting with $NODE_VERSION_MINIMUM."
fi

# Check Node.js version
NODE_VERSION=`node -v 2>&1`

NODE_VERSION_MAJOR=`echo $NODE_VERSION | awk -F'[\\\.v]*' ' { print $2 } '`
NODE_VERSION_MINOR=`echo $NODE_VERSION | awk -F'[\\\.v]*' ' { print $3 } '`
NODE_VERSION_PATCH=`echo $NODE_VERSION | awk -F'[\\\.v]*' ' { print $4 } '`
if [ "$NODE_VERSION_MAJOR" -lt 1 ] && [ "$NODE_VERSION_MINOR" -lt 11 ] && [ "$NODE_VERSION_PATCH" -lt 36 ]; then
  die "ERROR: Node.js $NODE_VERSION is not supported. The minimum version supported is $NODE_VERSION_MINIMUM."
fi

# Split up the JVM_OPTS And CDAP_OPTS values into an array, following the shell quoting and substitution rules
function splitJvmOpts() {
    JVM_OPTS=("$@")
}

CDAP_HOME=${CDAP_HOME:-/opt/cdap}; export CDAP_HOME
COMPONENT_HOME=${CDAP_HOME}; export COMPONENT_HOME

# PID Location
PID_DIR=/var/tmp
BASENAME=${PRG##*/}
pid=$PID_DIR/$BASENAME.pid

# checks if there exists a PID that is already running. Alert user but still return success
check_before_start() {
  if [ ! -d "$PID_DIR" ]; then
    mkdir -p "$PID_DIR"
  fi

  # Checks if nodejs is available before it starts Cask Local DAP.
  command -v node >/dev/null 2>&1 || \
    { echo >&2 "CDAP requires Node.js but it's either not installed or not in path. Exiting."; exit 1; }

  if [ -f $pid ]; then
    if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo "$0 running as process `cat $pid`. Stop it first or use the restart function."
      exit 0
    fi
  else
    nodejs_pid=`ps | grep ui/ | grep -v grep | awk ' { print $1 } '`
    if [[ "x{nodejs_pid}" != "x" ]]; then
      kill -9 $nodejs_pid 2>/dev/null >/dev/null
    fi
  fi
}

compare_versions () {
  if [[ $1 == $2 ]]
  then
    return 0
  fi
  local IFS=.
  local i ver1=($1) ver2=($2)
  # fill empty fields in ver1 with zeros
  for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
  do
    ver1[i]=0
  done
  for ((i=0; i<${#ver1[@]}; i++))
  do
    if [[ -z ${ver2[i]} ]]
    then
      # fill empty fields in ver2 with zeros
      ver2[i]=0
    fi
    if ((10#${ver1[i]} > 10#${ver2[i]}))
    then
      return 1
    fi
    if ((10#${ver1[i]} < 10#${ver2[i]}))
    then
      return 2
    fi
  done
  return 0
}

# Rotates the basic start/stop logs
rotate_log () {
    log=$1;
    num=5;
    if [ -n "$2" ]; then
    num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
    while [ $num -gt 1 ]; do
        prev=`expr $num - 1`
        [ -f "$log.$prev" ] && mv -f "$log.$prev" "$log.$num"
        num=$prev
    done
    mv -f "$log" "$log.$num";
    fi
}

start() {
    debug=$1; shift
    port=$1; shift

    eval splitJvmOpts $DEFAULT_JVM_OPTS $JAVA_OPTS $CDAP_OPTS
    check_before_start
    mkdir -p "$APP_HOME/logs"
    rotate_log "$APP_HOME/logs/cdap.log"
    rotate_log "$APP_HOME/logs/cdap-debug.log"

    if test -e /proc/1/cgroup && grep docker /proc/1/cgroup 2>&1 >/dev/null; then
        ROUTER_OPTS="-Drouter.address=`hostname -i`"
    fi

    nohup nice -1 "$JAVACMD" "${JVM_OPTS[@]}" ${ROUTER_OPTS} -classpath "$CLASSPATH" co.cask.cdap.StandaloneMain >> \
        "$APP_HOME/logs/cdap.log" 2>&1 < /dev/null &
    echo $! > $pid

    echo -n "Starting Standalone CDAP ..."

    background_process=$!
    while kill -0 $background_process >/dev/null 2>/dev/null ; do
      if grep '..* started successfully' "$APP_HOME/logs/cdap.log" > /dev/null 2>&1; then
        if $debug ; then
          echo; echo "Remote debugger agent started on port $port."
        else
          echo
        fi
        grep -A 1 '..* started successfully' "$APP_HOME/logs/cdap.log"
        break
      elif grep 'Failed to start server' "$APP_HOME/logs/cdap.log" > /dev/null 2>&1; then
        echo; echo "Failed to start server"
        stop
        break
      else
        echo -n "."
        sleep 1;
      fi
    done
    echo
    if ! kill -s 0 $background_process 2>/dev/null >/dev/null; then
      echo "Failed to start, please check logs for more information."
    fi
}

stop() {
    echo -n "Stopping Standalone CDAP ..."
    if [ -f $pid ]; then
      pidToKill=`cat $pid`
      # kill -0 == see if the PID exists
      if kill -0 $pidToKill > /dev/null 2>&1; then
        kill $pidToKill > /dev/null 2>&1
        while kill -0 $pidToKill > /dev/null 2>&1;
        do
          echo -n "."
          sleep 1;
        done
        rm $pid
      else
        retval=$?
      fi
      rm -f $pid
      echo ""
      echo "Standalone CDAP stopped successfully."
    fi
    echo
}

restart() {
    stop
    start $1 $2
}

status() {
    if [ -f $pid ]; then
      pidToCheck=`cat $pid`
      # kill -0 == see if the PID exists
      if kill -0 $pidToCheck > /dev/null 2>&1; then
        echo "$0 running as process $pidToCheck"
        exit 0
      else
        echo "pidfile exists, but process does not appear to be running"
        exit 3
      fi
    else
      echo "$0 is not running"
      exit 3
    fi
}

case "$1" in
  start|restart)
    command=$1; shift
    debug=false
    while [ $# -gt 0 ]
    do
      case "$1" in
        --enable-debug) shift; debug=true; port=$1; shift;;
        *) shift; break;;
      esac
    done
    if $debug ; then
      shopt -s extglob
      if [ -z "$port" ]; then
        port=5005
      elif [ -n "${port##+([0-9])}" ]; then
        die "port number must be an integer.";
      elif [ $port -lt 1024 ] || [ $port -gt 65535 ]; then
        die "port number must be between 1024 and 65535.";
      fi
      CDAP_OPTS="${CDAP_OPTS} -agentlib:jdwp=transport=dt_socket,address=localhost:$port,server=y,suspend=n"
    fi
    $command $debug $port
  ;;

  stop)
    $1
  ;;

  status)
    $1
  ;;

  update)
    check_for_updates
  ;;
  
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    echo "Additional options with start, restart:"
    echo "--enable-debug [ <port> ] to connect to a debug port for Standalone CDAP (default port is 5005)"
    exit 1
  ;;

esac
exit $?
