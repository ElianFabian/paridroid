#include <jni.h>

#include "com_math_paridroid_NativeTask.h"
#include "paridroid_install.h"
#include "log.h"

JNIEXPORT jint JNICALL Java_com_math_paridroid_NativeTask_install (JNIEnv *env, jclass class)
{
  return install();
}
