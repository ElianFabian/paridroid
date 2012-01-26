#!/bin/bash
# arg1 = version of PARI/GP to download

STABLE="2.5.0"
DEV="2.6.0"

if [ "$1" = "stable" ]
then
    if [ -d ./pari-$STABLE ]
    then
	echo "Pari-$STABLE already exists."
	exit 0
    else
	wget http://pari.math.u-bordeaux.fr/pub/pari/unix/pari-2.5.0.tar.gz
	exit 0
    fi
elif [ "$1" = "dev" ]
then
    if [ -d ./pari-$DEV ]
    then
	echo "Pari-$DEV already exists."
	exit 0
    else
	wget -r -l1 --no-parent -A.tar.gz http://pari.math.u-bordeaux.fr/pub/pari/snapshots/
	exit 0
    fi
fi