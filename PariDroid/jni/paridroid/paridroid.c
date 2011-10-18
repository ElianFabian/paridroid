/**
 *-----------------------------------------------
 * paridroid.c - Android wrapper around libpari.
 *-----------------------------------------------
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
#include <pari.h>

#include "com_math_paridroid_PariDroidActivity.h"
#include "paridroid.h"

/** number of bytes to initialize pari stack with */
size_t paridroid_stack_size = 4000000;

/** maximum value for the prime table */
ulong paridroid_maxprime = 500509;


void paridroid_init()
{
  pari_init(paridroid_stack_size, paridroid_maxprime);
}


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
}

void paridroid_close()
{
  pari_close();
}
