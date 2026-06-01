#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF)
# Apache Maven Wrapper startup script, version 3.3.2
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /usr/local/etc/mavenrc ]; then . /usr/local/etc/mavenrc; fi
  if [ -f /etc/mavenrc ]; then . /etc/mavenrc; fi
  if [ -f "$HOME/.mavenrc" ]; then . "$HOME/.mavenrc"; fi
fi

cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*)  cygwin=true  ;;
  MINGW*)   mingw=true   ;;
  Darwin*)  darwin=true
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home)"
      else
        export JAVA_HOME="/Library/Java/Home"
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac)"
  if [ -n "$javaExecutable" ]; then
    if $darwin; then
      javaHome="$(dirname "$javaExecutable")"
      javaHome="$(cd "$javaHome/.." && pwd -P)"
    else
      javaExecutable="$(readlink -f "$javaExecutable")"
      javaHome="$(dirname "$javaExecutable")"
      javaHome="$(expr "$javaHome" : '\(.*\)/bin')"
    fi
    JAVA_HOME="$javaHome"
    export JAVA_HOME
  fi
fi

if [ -z "$JAVACMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
  else
    JAVACMD="$(command -v java)"
  fi
fi

if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not defined correctly or java not found." >&2
  exit 1
fi

find_maven_basedir() {
  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ]; do
    if [ -d "$wdir/.mvn" ]; then basedir="$wdir"; break; fi
    wdir="$(cd "$wdir/.." && pwd)"
  done
  printf '%s' "$(cd -P "$basedir" && pwd)"
}

BASE_DIR="$(find_maven_basedir "$(dirname "$0")")"
if [ -z "$BASE_DIR" ]; then exit 1; fi

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$BASE_DIR}"
export MAVEN_PROJECTBASEDIR

wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -r "$wrapperJarPath" ]; then
  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  while IFS="=" read -r key value; do
    value="$(echo "$value" | tr -d '\r')"
    case "$key" in wrapperUrl) wrapperUrl="$value"; break ;; esac
  done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
  if command -v curl > /dev/null; then
    curl --silent -o "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
  elif command -v wget > /dev/null; then
    wget --quiet -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
  fi
fi

if $cygwin; then
  MAVEN_PROJECTBASEDIR="$(cygpath --path --windows "$MAVEN_PROJECTBASEDIR")"
  wrapperJarPath="$(cygpath --path --windows "$wrapperJarPath")"
  [ -n "$JAVA_HOME" ] && JAVA_HOME="$(cygpath --path --windows "$JAVA_HOME")"
fi

export MAVEN_CMD_LINE_ARGS="$*"
exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$wrapperJarPath" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
