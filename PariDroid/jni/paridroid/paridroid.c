/**
 *-----------------------------------------------
 * paridroid.c - Android wrapper around libpari.
 *-----------------------------------------------
 *
 * Copyright (C) 2011 Charles Boyd
 *               2014 Andreas Enge <andreas.enge@inria.fr>
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

#include <stdio.h>
#include <stdlib.h>
#include <pari/pari.h>
#include <setjmp.h>
#include <strings.h>

#include "org_freemonad_paridroid_PariNative.h"
#include "paridroid.h"

/** number of bytes to initialize pari stack with */
size_t paridroid_stack_size = 4000000;

/** maximum value for the prime table */
ulong paridroid_maxprime = 500509;

/** Error handling */
jmp_buf env;

void gp_err_recover(long numerr) 
{
  longjmp(env, numerr); 
}


static char *droidStr;

static pari_stack s_droidStr;

static void
droidOutC(char c) 
{ 
  long n = pari_stack_new(&s_droidStr); 
  droidStr[n] = c;
}

static void
droidOutS(const char *s) 
{
  while(*s) 
    droidOutC(*s++); 
}

static void
droidOutF(void) { }

static PariOUT droidOut = {droidOutC, droidOutS, droidOutF};

void 
paridroid_quit(long exitcode) 
{
  LOGW("Call to paridroid_quit: exit code = %l", exitcode);
  paridroid_close();
}

void
help(const char *s)
{
  entree *ep = is_entry(s);
  if (ep && ep->help)
    pari_printf("%s\n",ep->help);
  else
    pari_printf("Function %s not found\n",s);
}

void
print_version(char *vers)
{
  LOGI("PARIDROID_LIBRARY_VERSION = %s", vers);
}

void
paridroid_init()
{

  static entree functions_gp[]={
    {"quit",0,(void*)paridroid_quit,11,"vD0,L,","quit({status = 0}): quit, return to the system with exit status 'status'."},
    {NULL,0,NULL,0,NULL,NULL}};

  setenv ("GPTMPDIR", "/data/data/org.freemonad.paridroid/cache", 1);
  pari_init(paridroid_stack_size, paridroid_maxprime);
  pari_add_module(functions_gp);
  cb_pari_err_recover = gp_err_recover;
  pari_stack_init(&s_droidStr,sizeof(*droidStr),(void**)&droidStr);
  
  pariOut=&droidOut;
  pariErr=&droidOut;
  
  print_version("v1.5");
}

void
paridroid_eval(const char *in, char **out, int *returnsgnil)
{

  if(setjmp(env) != 0) {
    LOGE("setjmp different from zero");
    return "";
  }

  s_droidStr.n=0;
  avma=top;

  volatile GEN z = gnil;
  pari_CATCH(CATCH_ALL)
  {
    droidOutS(pari_err2str(pari_err_last()));
  } pari_TRY {
    z = gp_read_str(in);
  } pari_ENDCATCH;
 
  pari_add_hist(z, 0); /* FIXME: change 0 with execution time */
  if (z != gnil) {
    *returnsgnil = 0;
    if (in[strlen(in)-1]!=';')
      droidOutS (GENtostr (z));
  }
  else
    returnsgnil = 1;
  droidOutC(0);
  *out = droidStr;
}


/* copied from paripriv.h inside the pari project */
extern const long functions_tblsz;  /* hashcodes table size */
extern entree **functions_hash;   /* functions hashtable */
#define EpVALENCE(ep) ((ep)->valence & 0xFF)
enum { EpNEW = 100, EpALIAS, EpVAR, EpINSTALL };

void
paridroid_getcommands (char *** commands, int size, int *len)
{
   /* copied from the commands function in gp.c in the pari project */
   long i;
   entree *ep;

   *len = 0;
   for (i = 0; i < functions_tblsz; i++)
      for (ep = functions_hash[i]; ep; ep = ep->next)
      {
         switch (EpVALENCE(ep))
         {
            case EpVAR:
               if (typ((GEN)ep->value) == t_CLOSURE)
                  break;
               /* fall through */
            case EpNEW:
               continue;
         }
         if (ep->menu < 13) {
            if (*len < size) {
               (*commands) [*len] = ep->name;
               (*len)++;
            }
            else
               LOGE ("Array too small in paridroid_getcommands\n");
         }
      }
}


void
paridroid_close()
{
  pari_close();
}
