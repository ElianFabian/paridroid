/**
 *------------------------------------------------------
 * PariDroidActivity.c - JNI Callbacks from libparidroid.
 *------------------------------------------------------
 * Copyright (C) 2011 Charles Boyd
 * Copyright (C) 2014 Andreas Enge <andreas.enge@inria.fr>
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
#include <jni.h>
#include "paridroid.h"
#include "org_freemonad_paridroid_PariNative.h"

void
Java_org_freemonad_paridroid_PariNative_paridroidInit(JNIEnv *jenv, jobject obj)
{
  paridroid_init();
}

jobjectArray
Java_org_freemonad_paridroid_PariNative_paridroidEval(JNIEnv *env, jobject obj, jstring input)
{
  const jbyte *cmd;
  char *output;
  int returnsgnil;
  jobjectArray res;

  // Turn the jstring into a C string.
  cmd = (*env)->GetStringUTFChars(env, input, NULL);

  // Check if JVM has already thrown out of memory error.
  if (cmd == NULL) {
    LOGW("Returning NULL from Native:paridroidEval");
    return NULL;
  }
  
  // Evaluate the command; empty is 1 or 0 depending on whether or not
  // the return value of the command was gnil.
  paridroid_eval(cmd, &output, &returnsgnil);

  // Free the Java objects we no longer need.
  (*env)->ReleaseStringUTFChars(env, input, cmd);

  // We encode the native string into a first jstring object and gnil
  // into a second jstring object and return both in a jobjectArray.
  res = (*env)->NewObjectArray (env, (jsize) 2,
                                (*env)->FindClass (env, "java/lang/String"),
                                NULL);
  jstring s = (*env)->NewStringUTF (env, output);
  (*env)->SetObjectArrayElement (env, res, 0, s);
  (*env)->DeleteLocalRef (env, s);
  if (returnsgnil)
    s = (*env)->NewStringUTF (env, "1");
  else
    s = (*env)->NewStringUTF (env, "0");
  (*env)->SetObjectArrayElement (env, res, 1, s);
  (*env)->DeleteLocalRef (env, s);
  
  
  return res;
}

jobjectArray
Java_org_freemonad_paridroid_PariNative_getCommands(JNIEnv *env, jclass cls)
{
   /* returns the list of supported gp commands */
   const int size = 1000;
   char **commands;
   int len, i;
   jobjectArray res;

   commands = (char**) malloc (size * sizeof (char *));
   paridroid_getcommands (&commands, size, &len);
   res = (*env)->NewObjectArray (env, (jsize) len,
                                 (*env)->FindClass (env, "java/lang/String"),
                                 NULL);
   for (i = 0; i < len; i++) {
      jstring s = (*env)->NewStringUTF (env, commands [i]);
      (*env)->SetObjectArrayElement (env, res, i, s);
      (*env)->DeleteLocalRef (env, s);
   }
   free (commands);

   return res;
}
