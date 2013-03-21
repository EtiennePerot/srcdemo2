#!/usr/bin/env bash

set -e

scriptRoot="$(cd "$(dirname "$BASH_SOURCE")" && pwd)"
buildRoot="$scriptRoot/build"
pkgname='srcdemo2'
mainInstallDir="opt/$pkgname"
filesystemRoot="$buildRoot/data"
controlRoot="$scriptRoot/control"
controlBuildRoot="$buildRoot/control"
repoRoot="$(cd "$scriptRoot/../../../.." && pwd)"
versionScript="$repoRoot/package/any/buildversion.py"
javaVersion='1.6'

echo 'Cleanup...'
rm -rf "$filesystemRoot" "$controlBuildRoot" "$buildRoot/data.tar.xz" "$buildRoot/control.tar.gz"

echo 'Running ant...'
cd "$repoRoot"
ant -Dant.build.javac.source="$javaVersion" -Dant.build.javac.target="$javaVersion" package

echo 'Building file structure...'
for directory in `cat "$scriptRoot/control/dirs"`; do
	mkdir -p "$filesystemRoot/$directory"
done
rmdir "$filesystemRoot/$mainInstallDir"
cp -ar "$repoRoot/build/jar" "$filesystemRoot/$mainInstallDir"
cd "$filesystemRoot"
for i in "$mainInstallDir/bin/"*; do
	ln -s "/$i" "$filesystemRoot/usr/bin/$(basename "$i")"
done
for i in "$mainInstallDir/desktopfiles/"*; do
	ln -s "/$i" "$filesystemRoot/usr/share/applications/$(basename "$i")"
done

echo 'Building .deb package (data.tar.xz)...'
cd "$filesystemRoot"
installedSize="$(du -sk --apparent-size "$filesystemRoot" | cut -f 1)"
tar cf "$buildRoot/data.tar" *
xz -e9 --memlimit-decompress=128MiB "$buildRoot/data.tar"

echo 'Building .deb package (control.tar.gz)...'
mkdir -p "$controlBuildRoot"
cp "$controlRoot/dirs" "$controlBuildRoot/dirs"
appVersion="$("$versionScript" -)"
cat "$controlRoot/control" | sed -e "s/%VERSION%/$appVersion/g" -e "s/%INSTALLEDSIZE%/$installedSize/g" > "$controlBuildRoot/control"
cat "$controlRoot/changelog.Debian" | sed "s/%VERSION%/$appVersion/g" > "$controlBuildRoot/changelog.Debian"
touch "$controlBuildRoot/rules"
cp "$repoRoot/LICENSE" "$controlBuildRoot/copyright"
cd "$filesystemRoot"
while IFS= read -d $'\0' -r file; do
	file=$(echo "$file" | sed 's#^./##')
	md5sum "$file" >> "$controlBuildRoot/md5sums"
done < <(find -type f -print0)
cd "$controlBuildRoot"
GZIP=-9 tar czf "$buildRoot/control.tar.gz" *
cd "$buildRoot"
rm -rf "$controlBuildRoot" "$filesystemRoot"

echo 'Building .deb package (final)...'
debFile="$scriptRoot/${pkgname}_${appVersion}-1_all.deb"
rm -f "$debFile"
cd "$buildRoot"
ar rcs "$debFile" debian-binary control.tar.gz data.tar.xz # The order of the files matter
