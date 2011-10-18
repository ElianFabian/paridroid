/**
 *------------------------------------------------------
 * PariDroidActivity.c - JNI Callbacks from libparidroid.
 *------------------------------------------------------
 * Copyright (C) 2011, Charles Boyd
 *
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
 */

#include <stdlib.h>
#include <string.h>

#include "com_math_paridroid_PariDroidActivity.h"
#include "paridroid.h"

JNIEXPORT void JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidInit(JNIEnv *jenv, jobject obj)
{
  paridroid_init();
}

JNIEXPORT jstring JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidEval(JNIEnv *jenv, jobject obj, jstring prompt)
{
  // Turn the jstring into a C string.
  const char *cmd = (*jenv)->GetStringUTFChars(jenv, prompt, 0);

  // Send the string to GP for evaluation and get the output.
  char *ret = paridroid_eval(cmd);
  
  // We only need the return value, so we tell the JVM to free everything else from memory.
  (*jenv)->ReleaseStringUTFChars(jenv, prompt, cmd);
  
  // We encode the C string into a jstring and return it.
  return (*jenv)->NewStringUTF(jenv,ret);
}

JNIEXPORT void JNICALL
Java_com_math_paridroid_PariDroidActivity_paridroidClose(JNIEnv *jenv, jobject obj)
{
  paridroid_close();
}
