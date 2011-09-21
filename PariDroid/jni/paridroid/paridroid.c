/**
 * This file is part of PariDroid.
 *
 * PariDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PariDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Written by Charles Boyd
 * UMass Amherst - Dept. of Mathematics
 * (Copyright 2011)
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <android/log.h>
#include <pari.h>

#include "com_math_paridroid_PariDroidActivity.h"

/** number of bytes to initialize pari stack with */
size_t paridroid_stack_size = 4000000;

/** maximum value for the prime table*/
ulong paridroid_maxprime = 500509;

/**
 * Wrapper around pari_init() function. 
 */
void paridroid_init()
{
  pari_init(paridroid_stack_size, paridroid_maxprime);
}

/**
 * Evaluates a GP command.
 * @param in: command for GP to evaluate.
 * @return string containing the output from GP.
 */
char *paridroid_eval(const char *in) 
{
  GEN z = gp_read_str(in);
  char *out = GENtostr(z);
  return out;
}

/**
 * Wrapper around pari_close() function.
 */
void paridroid_close()
{
  pari_close();
}

/* JNI WRAPPERS */

/**
 * JNI wrapper around paridroid_init().
 */
JNIEXPORT void JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidInit(JNIEnv *env, jobject obj)
{
  paridroid_init();
}

/**
 * JNI wrapper around paridroid_eval().
 * @param env: pointer to the JVM environment.
 * @param obj: who the hell knows...
 * @param prompt: Java string to evaluate.
 * @return GP output encoded as a Java string.
 */
JNIEXPORT jstring JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidEval(JNIEnv *env, jobject obj, jstring prompt)
{
  // Turn the jstring into a C string.
  const char *cmd = (*env)->GetStringUTFChars(env, prompt, 0);

  // Send the string to GP for evaluation and get the output.
  char *ret = paridroid_eval(cmd);
  
  // We only need the return value, so we tell the JVM to free everything else from memory.
  (*env)->ReleaseStringUTFChars(env, prompt, cmd);
  
  // We encode the C string into a jstring and return it.
  return (*env)->NewStringUTF(env,ret);
}

/**
 * JNI wrapper around paridroid_close();
 */
JNIEXPORT void JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidClose(JNIEnv *env, jobject obj)
{
  paridroid_close();
}
