#!/bin/bash

COMPILESDK_VER="22"
BUILDTOOLS_VER="23.0.1"

if [ ! -e 'libsuperuser' ]; then
    echo '>Clone libsuperuser'
    git clone https://github.com/chainfire/libsuperuser --depth=1
    mv libsuperuser/libsuperuser tmp
    rm -rf libsuperuser
    mv tmp libsuperuser
    sed -i "s/compileSdkVersion.*/compileSdkVersion $COMPILESDK_VER/g" ./libsuperuser/build.gradle
    sed -i "s/buildToolsVersion.*/buildToolsVersion $BUILDTOOLS_VER/g" ./libsuperuser/build.gradle
fi
