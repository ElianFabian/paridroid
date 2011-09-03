#!/system/bin/sh

DATA_DIR=/data/data/com.math.paridroid

## Locate busybox binaries, exit if busybox is not installed.

if [[ -e /data/busybox ]]
then
    echo "Found busybox directory: /data/busybox"
    MV='/data/busybox/mv -f'
    TAR='/data/busybox/tar xvf'
    RM='/data/busybox/rm -f'
    MKDIR='/data/busybox/mkdir -p'
    CHMOD='/data/busybox/chmod'
    LN='/data/busybox/ln -s'
elif [[ -e /system/xbin/busybox ]]
then
    echo "Found busybox directory: /system/xbin/busybox"
    MV='busybox mv -f'
    TAR='busybox tar xvf'
    RM='busybox rm -f'
    MKDIR='busybox mkdir -p'
    CHMOD='busybox chmod'
    LN='busybox ln -s'
else
    echo "Unable to locate busybox, cannot install. Exiting."
    exit 1
fi

## Install the static gp binary and make it executable.

BIN_DIR=$DATA_DIR/bin

if [[ -e $BIN_DIR ]]
then
    $MV $DATA_DIR/gp.jpg $BIN_DIR/gp
    echo "Installed gp binary to directory: $BIN_DIR"
    $CHMOD 0775 $BIN_DIR/gp
    echo "Set gp binary to executable."
else
    $MKDIR $BIN_DIR
    echo "Created new directory: $BIN_DIR"
    $MV $DATA_DIR/gp.jpg $BIN_DIR/gp
    echo "Installed gp binary to directory: $BIN_DIR"
    $CHMOD 0775 $BIN_DIR/gp
    echo "Set gp binary to executable."
fi

## Not needed unless we are working with dynamically linked binary.
#LIB_DIR=$DATA_DIR/lib
#
#if [[ -e $LIB_DIR ]]
#    export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/$LIB_DIR
#else
#    mkdir $LIB_DIR
#    export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/$LIB_DIR
#fi

if [[ -e /sdcard ]]
then
    SD_CARD=/sdcard
    EXTERNAL_DATA_DIR=$SD_CARD/paridroid
    if [[ ! -e $EXTERNAL_DATA_DIR ]]
    then
	$MKDIR $EXTERNAL_DATA_DIR
	echo "Created external data storage directory: $EXTERNAL_DATA_DIR"
	$MV $DATA_DIR/galdata.tar.jpg $EXTERNAL_DATA_DIR/galdata.tar
	if [[ ! -e $EXTERNAL_DATA_DIR/data/galdata ]]
	then
	    cd $EXTERNAL_DATA_DIR
	    $TAR galdata.tar
	    echo "Extracted files from galdata.tar to: $EXTERNAL_DATA_DIR/data/galdata"
	else
	    echo "Files for extra package galdata already exist in directory: $EXTERNAL_DATA_DIR/data/galdata"
	fi
    fi
elif [[ -e /mnt/sdcard ]]
then
    SD_CARD=/mnt/sdcard
    EXTERNAL_DATA_DIR=$SD_CARD/paridroid
    if [[ ! -e $EXTERNAL_DATA_DIR ]]
    then
	$MKDIR $EXTERNAL_DATA_DIR
	echo "Created external data storage directory: $EXTERNAL_DATA_DIR"
	$MV $DATA_DIR/galdata.tar.jpg $EXTERNAL_DATA_DIR/galdata.tar
	if [[ ! -e $EXTERNAL_DATA_DIR/data/galdata ]]
	then
	    cd $EXTERNAL_DATA_DIR
	    $TAR galdata.tar
	    echo "Extracted files from galdata.tar to: $EXTERNAL_DATA_DIR/data/galdata"
	else
	    echo "Files for extra package galdata already exist in directory: $EXTERNAL_DATA_DIR/data/galdata"
	fi
    fi
else
    echo "Unable to locate SD card, cannot install extra data packages."
    exit 1
fi

EXTRAS_DIR=$DATA_DIR/share/pari

if [[ ! -e $EXTRAS_DIR ]]
then
    $MKDIR $EXTRAS_DIR
    echo "Created new directory: $EXTRAS_DIR"
    if [[ -e $EXTERNAL_DATA_DIR/data/galdata ]]
    then
	$LN $EXTERNAL_DATA_DIR/data/galdata $EXTRAS_DIR/galdata
	echo "Created symbolic link from $EXTERNAL_DATA_DIR/data/galdata to $EXTRAS_DIR/galdata"
    else
	echo "Failed to create symbolic link for extra package: galdata"
	exit 1
    fi
else
    echo "Symbolic link for extra package galdata already exists: $EXTRAS_DIR/galdata"
    exit 0
fi
#EOF