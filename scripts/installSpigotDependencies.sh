#!/usr/bin/env bash

pushd "$(dirname "$BASH_SOURCE")"

# $1: Spigot MC version to build
# $2: "remapped" to check for a remapped server jar
# $3: Optional: The specific Spigot build number to build. If unset, we use $1 for this.
buildSpigotIfMissing() {
  local buildVersion="$1"
  local versionString="$1"
  local classifier=""
  local jarPath=""
  local installedImplementationVersion=""
  local installedBuildNumber=""
  local build="yes"

  if [ -n "$3" ]; then
    buildVersion="$3"
    versionString="$1 ($3)"
  fi
  if [ "$2" = "remapped" ]; then classifier="-remapped-mojang"; fi

  jarPath=$"$HOME/.m2/repository/org/bukkit/craftbukkit/$1-R0.1-SNAPSHOT/craftbukkit-$1-R0.1-SNAPSHOT${classifier}.jar"
  if [ -f "${jarPath}" ]; then
    installedImplementationVersion=$(unzip -p "${jarPath}" 'META-INF/MANIFEST.MF' | grep -oP '(?<=^Implementation-Version: )[^\n\r]*')
    installedBuildNumber=$(echo "${installedImplementationVersion}" | grep -oP '^\d+(?=-)')
    echo "Maven repository: Found Spigot $1 (${installedImplementationVersion}) (#${installedBuildNumber})"

    if [ -n "$3" ]; then
      if [ "${installedBuildNumber}" = "$3" ]; then
        build="no"
      fi
    else
      build="no"
    fi
  fi

  if [ "${build}" = "yes" ]; then
    ./installSpigot.sh "${buildVersion}"
  else
    echo "Not building Spigot ${versionString} because it is already in our Maven repository"
  fi
}

# We only re-build CraftBukkit/Spigot versions that are missing in the Maven cache.
# Add entries here for every required version of CraftBukkit/Spigot.

# The following versions require JDK 21 to build:
source installJDK.sh 21

buildSpigotIfMissing 1.21.5 remapped

popd
