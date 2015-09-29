#!/bin/bash

if [ ! -e 'libsuperuser' ]; then
    echo 'Clone libsuperuser'
    git clone https://github.com/chainfire/libsuperuser --depth=1
    mv libsuperuser/libsuperuser ,
    sed -i "s/compileSdkVersion.*/compileSdkVersion 22/g" ./libsuperuser/build.gradle
    sed -i "s/buildToolsVersion.*/buildToolsVersion '23.0.0'/g" ./libsuperuser/build.gradle
fi

./gradlew build

echo 'Start building.'
