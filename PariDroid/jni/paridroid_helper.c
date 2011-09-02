#include <stdlib.h>
#include <unistd.h>
#include <sys/stat.h>
#include <errno.h>
#include <jni.h>
#include <android/log.h>

#include "com_math_paridroid_NativeTask.h"

static int install(void)
{

  mode_t exec_mode = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IXOTH;

  if (chmod("/data/data/com.math.paridroid/paridroid-installer.sh", exec_mode))
    {
      system("/system/bin/sh /data/data/com.math.paridroid/paridroid-installer.sh");
      return 0;
    }
  
  return -1;
}


JNIEXPORT jint JNICALL Java_com_math_paridroid_NativeTask_install 
(JNIEnv *env, jclass class)
{
  return install();
}
