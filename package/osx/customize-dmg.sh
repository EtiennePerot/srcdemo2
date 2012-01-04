#!/bin/sh

echo "Customizing disk image..."
echo "Adding Applications symlink..."
ln -sn /Applications "$1"/Applications
echo "Setting custom icon"
cp ./img/icon.icns "$1"/.VolumeIcon.icns
SetFile -a C "$1"
echo "Setting custom background…"
mkdir "$1"/.background
cp ./package/osx/dmg-background.png "$1"/.background/background.png
cp ./package/osx/DS_Store "$1"/.DS_Store
echo "Done customizing .dmg."
