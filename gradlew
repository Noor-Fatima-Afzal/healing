#!/usr/bin/env sh

set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Missing Gradle wrapper properties: $WRAPPER_PROPERTIES" >&2
  exit 1
fi

distribution_url=$(sed -n 's/^distributionUrl=//p' "$WRAPPER_PROPERTIES" | tr -d '\r' | sed 's#\\:#:#g')
gradle_version=$(printf '%s' "$distribution_url" | sed -n 's#.*/gradle-\(.*\)-bin\.zip#\1#p')

if [ -z "$distribution_url" ] || [ -z "$gradle_version" ]; then
  echo "Unable to read Gradle distribution URL from $WRAPPER_PROPERTIES" >&2
  exit 1
fi

install_dir="$GRADLE_USER_HOME/wrapper/dists/gradle-$gradle_version-bin"
gradle_home="$install_dir/gradle-$gradle_version"

if [ ! -x "$gradle_home/bin/gradle" ]; then
  mkdir -p "$install_dir"
  archive="$install_dir/gradle-$gradle_version-bin.zip"

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$distribution_url" -o "$archive"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$distribution_url" -O "$archive"
  else
    echo "curl or wget is required to download Gradle" >&2
    exit 1
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$archive" -d "$install_dir"
  else
    echo "unzip is required to extract the Gradle distribution" >&2
    exit 1
  fi

  rm -f "$archive"
fi

exec "$gradle_home/bin/gradle" "$@"