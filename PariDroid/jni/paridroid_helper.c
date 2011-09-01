#include <stdlib.h>
#include <jni.h>
#include <errno.h>

#include <android/log.h>

static int install(void) {

  system("/data/data/com.math.paridroid/bin/paridroid-installer.sh");
  
  return 0;

}
