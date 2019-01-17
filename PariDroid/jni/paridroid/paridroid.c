/**
 *-----------------------------------------------
 * paridroid.c - Android wrapper around libpari.
 *-----------------------------------------------
 *
 * Copyright (C) 2011 Charles Boyd
 *               2014, 2015, 2019 Andreas Enge <andreas.enge@inria.fr>
 *               2015 Bill Allombert <bill.allombert@math.u-bordeaux.fr>
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

#include "fr_ubordeaux_math_paridroid_PariNative.h"
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

/* timer default */
static int chrono = 0;

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
  LOGW("Call to paridroid_quit: exit code = %ld", exitcode);
  paridroid_close();
}

static void
help(const char *s)
{
  entree *ep = is_entry(s);
  if (ep && ep->help)
    pari_printf("%s\n",ep->help);
  else
    pari_printf("Function %s not found\n",s);
}

static GEN
mysd_timer(const char *v, long flag)
{
   return sd_toggle(v,flag,"timer", &chrono);
}

void
paridroid_init()
{
  static entree functions_gp[]={
    {"quit",0,(void*)paridroid_quit,11,"vD0,L,","quit({status = 0}): quit, return to the system with exit status 'status'."},
    {"help",0,(void*)help,11,"vr","help(fun): display help for function fun"},
    {NULL,0,NULL,0,NULL,NULL}};

  static entree defaults_gp[]={
    {"timer",0,(void*)mysd_timer,16,"","(default) toggle timer on/off"},
    {NULL,0,NULL,0,NULL,NULL}};

  setenv ("GPTMPDIR", "/data/data/fr.ubordeaux.math.paridroid/cache", 1);
  pari_init(paridroid_stack_size, paridroid_maxprime);
  pari_add_module(functions_gp);
  pari_add_defaults_module (defaults_gp);
  cb_pari_err_recover = gp_err_recover;
  pari_stack_init(&s_droidStr,sizeof(*droidStr),(void**)&droidStr);
  
  pariOut=&droidOut; /* destinaton of pari_printf */
  pariErr=&droidOut;
}

void
paridroid_eval(const char *in, char **out, int *returnsgnil)
{
   s_droidStr.n=0;

   if(setjmp(env) != 0) {
      LOGE("setjmp different from zero");
      pari_add_hist(gnil, 0);
      droidOutC(0);
      *out = droidStr;
      *returnsgnil = 1;
   }
   else {
      volatile GEN z = gnil;
      pari_timer T;
      long time;

      timer_start (&T);
      z = gp_read_str(in);
      time = timer_delay (&T);

      if (z != gnil) {
         *returnsgnil = 0;
         pari_add_hist(z, time);
         if (chrono && time > 0)
            pari_printf ("time = %li ms.\n", time);
         if (in[strlen(in)-1]!=';') {
            pari_printf ("%%%lu = ", pari_nb_hist ());
            output (z);
         }
      }
      else
         *returnsgnil = 1;
      droidOutC(0);
      *out = droidStr;
   }

   avma = pari_mainstack->top;
}


/* copied from paripriv.h inside the pari project */
extern const long functions_tblsz;  /* hashcodes table size */
extern entree **functions_hash;   /* functions hashtable */
#define EpVALENCE(ep) ((ep)->valence & 0xFF)
enum { EpNEW = 100, EpALIAS, EpVAR, EpINSTALL };

void
paridroid_getcommands (const char *** commands, int size, int *len)
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
