
TOOLCHAIN=/tmp/android-toolchain \

export CC=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \

SYSROOT=$TOOLCHAIN/sysroot \

CPPFLAGS=-I$SYSROOT/usr/include \
CFLAGS=-nostdlib \
LDFLAGS=-Wl,-rpath-link=$SYSROOT/usr/lib -L$SYSROOT/usr/lib \
LIBS=-lc \

./configure --host=arm-eabi --prefix=/home/charles/projects/paridroid/readline/out 