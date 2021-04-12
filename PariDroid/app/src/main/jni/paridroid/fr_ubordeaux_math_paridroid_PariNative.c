/**
 *------------------------------------------------------
 * PariNative.c - JNI Callbacks from libparidroid.
 *------------------------------------------------------
 * Copyright (C) 2011 Charles Boyd
 * Copyright (C) 2014, 2019, 2020, 2021 Andreas Enge <andreas.enge@inria.fr>
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
#include "paridroid.h"
#include "fr_ubordeaux_math_paridroid_PariNative.h"

void
Java_fr_ubordeaux_math_paridroid_PariNative_paridroidInit(JNIEnv *env, jclass cls, jlong parisize)
{
  paridroid_init(parisize);
}

jstring
Java_fr_ubordeaux_math_paridroid_PariNative_paridroidEval(JNIEnv *env, jclass cls, jstring input)
{
  const jbyte *cmd;
  char *output;
  jstring res;

  // Turn the jstring into a C string.
  cmd = (*env)->GetStringUTFChars(env, input, NULL);

  // Check if JVM has already thrown out of memory error.
  if (cmd == NULL) {
    LOGW("Returning NULL from Native:paridroidEval");
    return NULL;
  }
  
  // Evaluate the command; returnsgnil is 1 or 0 depending on whether or not
  // the return value of the command was gnil.
  paridroid_eval(cmd, &output);

  // Free the Java objects we no longer need.
  (*env)->ReleaseStringUTFChars(env, input, cmd);

  // Encode the native string into a jstring object.
  res = (*env)->NewStringUTF (env, output);
  
  return res;
}

jobjectArray
Java_fr_ubordeaux_math_paridroid_PariNative_getCommands(JNIEnv *env, jclass cls)
{
   /* returns the list of supported gp commands */
   const int size = 1000;
   const char **commands;
   int len, i;
   jobjectArray res;

   commands = (const char**) malloc (size * sizeof (char *));
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

jstring
Java_fr_ubordeaux_math_paridroid_PariNative_read (JNIEnv *env, jclass cls, jint file)
{
   int fd;
   char *line;

   fd = (int) file;

   paridroid_read (fd, &line);

   jstring s = (*env)->NewStringUTF (env, line);

   return s;
}
