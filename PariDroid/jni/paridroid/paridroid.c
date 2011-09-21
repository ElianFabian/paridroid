#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <android/log.h>
#include <pari.h>

#include "com_math_paridroid_PariDroidActivity.h"

char *paridroid_eval(const char *in) 
{
  GEN z = gp_read_str(in);
  char *out = GENtostr(z);
  return out;
}

JNIEXPORT jstring JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidEval(JNIEnv *env, jobject obj, jstring prompt)
{
  const char *cmd = (*env)->GetStringUTFChars(env, prompt, 0);

  char *ret = paridroid_eval(cmd);

  (*env)->ReleaseStringUTFChars(env, prompt, cmd);

  return (*env)->NewStringUTF(env,ret);

}
