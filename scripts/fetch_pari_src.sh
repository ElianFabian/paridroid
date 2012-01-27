#!/bin/bash

PARI_ROOT=../pari
TARGET_DIR=$PARI_ROOT/pari-dev

if [ -d $TARGET_DIR ]
then
    cd $TARGET_DIR && git pull
else
    git clone http://pari.math.u-bordeaux.fr/git/pari.git $TARGET_DIR
fi