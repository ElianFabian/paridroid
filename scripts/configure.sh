#!/bin/bash
# arg1 = path to android ndk
# arg2 = path to android sdk

ANDROID_DIR=../android

NDK_SYMLINK=$ANDROID_DIR/ndk
SDK_SYMLINK=$ANDROID_DIR/sdk

TOOLCHAIN=$ANDROID_DIR/toolchain

if [ ! -d $ANDROID_DIR ]
then
    mkdir $ANDROID_DIR
fi

if [ -d $NDK_SYMLINK ]
then
    echo "Found link to NDK: $NDK_SYMLINK"
elif [ -n "$1" ]
then
    NDK_DIR="$1"
    ln -s $NDK_DIR $NDK_SYMLINK
    echo "Created symbolic link: $NDK_SYMLINK -> $NDK_DIR"
else
    echo "Error: not enough arguments, arg1 missing"
    echo "Usage: arg1 = path to NDK, arg2 = path to SDK"
fi


if [ -d $SDK_SYMLINK ]
then
    echo "Found link to SDK: $SDK_SYMLINK"
elif [ -n "$2" ]
then
    SDK_DIR="$2"
    ln -s $SDK_DIR $SDK_SYMLINK
    echo "Created symbolic link: $SDK_SYMLINK -> $SDK_DIR"
else
    echo "Error: not enough arguments, arg1 missing"
    echo "Usage: arg1 = path to NDK, arg2 = path to SDK"
fi
#EOF