#!/system/bin/sh

DATA_DIR='/data/data/com.math.paridroid'

BIN_DIR=$DATA_DIR/bin
LIB_DIR=$DATA_DIR/lib
SHARE_DIR=$DATA_DIR/share
EXTRAS_DIR=$SHARE_DIR/par

export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/$LIB_DIR

BUSYBOX=/data/busybox # test for other busybox install dir
TAR=$BUSYBOX/tar

cd $SHARE_DIR
$TAR xvf galdata.tar
mv data/galdata pari/galdata
rm -R data