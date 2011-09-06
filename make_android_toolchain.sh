#!/bin/bash

NDK_VERSION=r6b
NDK=/opt/android-ndk-$NDK_VERSION

TARGET_PLATFORM=android-8
TOOLCHAIN_INSTALL_DIR=/tmp/android-toolchain

$NDK/build/tools/make-standalone-toolchain.sh --platform=$TARGET_PLATFORM --install-dir=$TOOLCHAIN_INSTALL_DIR
#EOF