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
#include <pari.h>
#include <setjmp.h>
#include <android/log.h>

#include "com_math_paridroid_PariDroidActivity.h"

/** not sure what this does */
jmp_buf env;

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

  entree functions_gp[]={
    {"quit",0,(void*)gp_quit(),11,"vD0,L,","quit({status = 0}): quit, return to the system with exit status 'status'."},
    {"help",0,(void*)help(),11,"vr","help(fun): display help for function fun"},
    {NULL,0,NULL,0,NULL,NULL}};
  
  pari_add_module(functions_gp);
  cb_pari_err_recover = gp_err_recover();
  (void)setjmp(env);
}

/**
 * Evaluates a GP command.
 * @param in: command for GP to evaluate.
 * @return string containing the output from GP.
 */
char *paridroid_eval(const char *in) 
{
  GEN z;
  z = gp_read_str(in);
  if (z != gnil) 
    {
      if (in[strlen(in) -1]!=';') 
	{
	  char *out = GENtostr(z);
	  return out;
	}
    } 
  free(in); avma=top;
  return "NULL";
}

/**
 * Displays the help menu for a function.
 * @param the name of the function.
 */
void help(const char *s)
{
  entree *ep = is_entry(s);
  if (ep && ep->help)
    pari_printf("%s\n",ep->help);
  else
    pari_printf("Function %s not found\n",s);
}

/**
 * Allows paridroid to recover from error.
 * @param identifier for error to recover from.
 */
void gp_err_recover(long numerr) 
{ 
  longjmp(env, numerr); 
}

/**
 * Wrapper around pari_close() function.
 */
void paridroid_close()
{
  pari_close();
}

/**
 * Allows paridroid to quit with a given exit code.
 * @oaram the exit code.
 */
void gp_quit(long exitcode)
{
  exit(exitcode);
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
