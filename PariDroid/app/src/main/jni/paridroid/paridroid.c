/**
 *-----------------------------------------------
 * paridroid.c - Android wrapper around libpari.
 *-----------------------------------------------
 *
 * Copyright (C) 2011 Charles Boyd
 *               2014, 2015, 2019, 2020, 2021 Andreas Enge <andreas.enge@inria.fr>
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
#include <pari/paripriv.h>
#include <setjmp.h>
#include <strings.h>

#include "fr_ubordeaux_math_paridroid_PariNative.h"
#include "paridroid.h"

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
  LOGW("Call to paridroid_quit: exit code = %ld", exitcode);
  paridroid_close();
}


void
paridroid_init(long parisize, long primelimit)
{
  static entree functions_gp[]={
    {"quit",0,(void*)paridroid_quit,11,"vD0,L,","quit({status = 0}): quit, return to the system with exit status 'status'."},
    {NULL,0,NULL,0,NULL,NULL}};

  setenv ("GPTMPDIR", "/data/data/fr.ubordeaux.math.paridroid/cache", 1);
  pari_init(parisize, primelimit);
  pari_add_module(functions_gp);
  cb_pari_err_recover = gp_err_recover;
  pari_stack_init(&s_droidStr,sizeof(*droidStr),(void**)&droidStr);
  
  pariOut=&droidOut; /* destinaton of pari_printf */
  pariErr=&droidOut;
}


static void
command_prepare (pari_timer *T, pari_timer *Tw)
   /* Common code to run before executing a command from paridroid_eval
      or paridroid_read. Prepares the global output variable and sets
      the timer T and the wallclock timer Tw, unless they are NULL. */
{
   s_droidStr.n=0;
   timer_start (T);
   walltimer_start (Tw);
}


static void
command_postpare (char **out, GEN z, int print, pari_timer T, pari_timer Tw)
   /* Common code to run after executing a command from paridroid_eval
      or paridroid_read. Creates the output from z to out depending on
      the boolean value of print.
      T and Tw must be the same as passed to command_prepare. */
{
   long time, rtime;

   time = timer_delay (&T);
   rtime = walltimer_delay (&Tw);

   if (z != gnil) {
      pari_add_hist(z, time, rtime);
      if (GP_DATA->chrono && time > 0)
         if (pari_mt_nbthreads == 1)
            pari_printf ("time = %s.\n",
               gp_format_time (time));
         else
            pari_printf ("cpu time = %s, real time = %s.\n",
               gp_format_time (time),
               gp_format_time (rtime));
      if (print) {
         char *s = GENtostr (z);
         pari_printf ("%%%lu = ", pari_nb_hist ());
         if (GP_DATA->lim_lines)
           lim_lines_output(s, 0, GP_DATA->lim_lines);
         else
           pari_puts(s);
         pari_free(s);
      }
   }

   droidOutC(0);
   *out = droidStr;
   avma = pari_mainstack->top;
}


void
paridroid_eval(const char *in, char **out)
{
   pari_timer T, Tw;
   command_prepare (&T, &Tw);

   if(setjmp(env) != 0) {
      LOGE("setjmp different from zero");
      pari_add_hist(gnil, 0, 0);
      command_postpare (out, gnil, 0, T, Tw);
   }
   else {
      volatile GEN z = gnil;

      z = gp_read_str (in);

      command_postpare (out, z, in[strlen(in)-1]!=';', T, Tw);
   }
}


/* Work around the behaviour of gp_read_stream for PARI<=2.14.0, which does
   not distinguish between a failed read (because there is only whitespace
   left at the end of the file) and an expression returning gnil (because
   it ends with ";", for instance), but always returns gnil. */
#if PARI_VERSION_CODE < 134656
static int
gp_read_stream_buf(FILE *fi, Buffer *b)
{
  input_method IM;
  filtre_t F;

  init_filtre(&F, b);

  IM.file = (void*)fi;
  IM.myfgets = (fgets_t)&fgets;
  IM.getline = &file_input;
  IM.free = 0;
  return input_loop(&F,&IM);
}

static GEN
gp_read_stream_null(FILE *fi)
{
   Buffer *b = new_buffer ();
   GEN x = NULL;
   while (gp_read_stream_buf(fi, b))
   {
     if (*(b->buf)) { x = readseq(b->buf); break; }
   }
   delete_buffer (b);
   return x;
}
#endif

void
paridroid_read (const int file, char **out)
{
   FILE *f;
   volatile GEN z_next = gnil, z;
   pari_timer T, Tw;

   f = fdopen (file, "r");

   command_prepare (&T, &Tw);

   do {
      z = z_next;
#if PARI_VERSION_CODE < 134656
      z_next = gp_read_stream_null (f);
#else
      z_next = gp_read_stream (f);
#endif
   } while (z_next != NULL);

   fclose (f);

   command_postpare (out, z, 1, T, Tw);
}


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
