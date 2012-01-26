#!/bin/bash
# arg1 = target API level (optional)

DEFAULT_API=8

TOOLCHAIN_DIR=../android/toolchain

NDK_DIR=../android/ndk

MAKE_TOOLCHAIN_SH=$NDK_DIR/build/tools/make-standalone-toolchain.sh

if [ -n "$1" ]
then
    API_LEVEL="$1"
else
    API_LEVEL=$DEFAULT
fi

if [ -e $MAKE_TOOLCHAIN_SH  ]
then
    $MAKE_TOOLCHAIN_SH --platform=$API_LEVEL --install-dir=$TOOLCHAIN_DIR
else
    echo "Cannot find NDK script to make standalone toolchain. Exiting."
    exit -1
fi

