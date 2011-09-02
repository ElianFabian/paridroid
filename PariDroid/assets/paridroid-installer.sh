#!/system/bin/sh

DATA_DIR='/data/data/com.math.paridroid'

BIN_DIR=$DATA_DIR/bin
LIB_DIR=$DATA_DIR/lib
SHARE_DIR=$DATA_DIR/share

if [[ -e /data/busybox ]]
then
    MV='/data/busybox/mv'
    TAR='/data/busybox/tar'
elif [[ -e /system/xbin/busybox ]]
then
    MV='busybox mv'
    TAR='busybox tar'
else
    echo "Unable to locate tar binary, cannot install. Exiting."
    exit -1
fi

if [[ -e $BIN_DIR ]]
then
    $MV $DATA_DIR/gp.jpg $BIN_DIR/gp
    cd $BIN_DIR
    chmod 0775 gp
else
    mkdir $BIN_DIR
    $MV $DATA_DIR/gp.jpg $BIN_DIR/gp
    cd $BIN_DIR
    chmod 0775 gp
fi

## Not needed unless we are working with dynamically linked binary.

#if [[ -e $LIB_DIR ]]
#    export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/$LIB_DIR
#else
#    mkdir $LIB_DIR
#    export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/$LIB_DIR
#fi


if [[ -e $SHARE_DIR ]]
then
    cd $DATA_DIR
    $MV galdata.tar.jpg $SHARE_DIR/galdata.tar
    cd $SHARE_DIR
    $TAR xvf galdata.tar
    mkdir $SHARE_DIR/pari
    $MV data/galdata pari/galdata
    rm -R data
    rm -f galdata.tar
else
    cd $DATA_DIR
    mkdir $SHARE_DIR
    $MV galdata.tar.jpg $SHARE_DIR/galdata.tar
    cd $SHARE_DIR
    $TAR xvf galdata.tar
    mkdir $SHARE_DIR/pari
    $MV data/galdata pari/galdata
    rm -R data
    rm -f galdata.tar
fi
#EOF