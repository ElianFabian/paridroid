#!/bin/bash

NDK_VERSION=r7
NDK=~/opt/android/android-ndk-$NDK_VERSION

TARGET_PLATFORM=android-8
TOOLCHAIN_INSTALL_DIR=/tmp/android-toolchain

$NDK/build/tools/make-standalone-toolchain.sh --platform=$TARGET_PLATFORM --install-dir=$TOOLCHAIN_INSTALL_DIR
cp $TOOLCHAIN_INSTALL_DIR/sysroot/usr/include/termios.h $TOOLCHAIN_INSTALL_DIR/sysroot/usr/include/sys/termios.h
#EOF