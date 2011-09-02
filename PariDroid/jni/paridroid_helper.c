#include <stdlib.h>
#include<sys/stat.h>
#include <errno.h>
#include <jni.h>
#include <android/log.h>

static int install(void) {
  
  if (chmod("/data/data/com.math.paridroid/paridroid-installer.sh", S_IRWXU))
    {
      system("/data/data/com.math.paridroid/paridroid-installer.sh");
      return 0;
    }
  
  return -1;
}
