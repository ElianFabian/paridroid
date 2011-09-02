#!/system/bin/sh

DATA_DIR='/data/data/com.math.paridroid'

BIN_DIR=$DATA_DIR/bin
LIB_DIR=$DATA_DIR/lib
SHARE_DIR=$DATA_DIR/share

if [[ -e $BIN_DIR ]]
    mv $DATA_DIR/gp.jpg $BIN_DIR/gp
    cd $BIN_DIR
    chmod 0775 gp
else
    mkdir $BIN_DIR
    mv $DATA_DIR/gp.jpg $BIN_DIR/gp
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

if [[ -e /data/busybox/tar ]]
then
    TAR=/data/busybox/tar
elif [[ -e /system/xbin/tar ]]
    TAR=/system/xbin/tar
else
    echo "Unable to locate tar binary, cannot install. Exiting."
    exit -1
fi

if [[ -e $SHARE_DIR ]]
    cd $SHARE_DIR
    mv galdata.tar.jpg galdata.tar
    $TAR xvf galdata.tar
    mv data/galdata pari/galdata
    rm -R data
else
    mkdir $SHARE_DIR
    cd $SHARE_DIR
    mv galdata.tar.jpg galdata.tar
    $TAR xvf galdata.tar
    mv data/galdata pari/galdata
    rm -R data
fi
#EOF