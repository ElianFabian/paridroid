/**
 *-----------------------------------------------
 * paridroid.h - Android wrapper around libpari.
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

#include <string.h>
#include <strings.h>
#include <android/log.h>

#ifndef _PARIDROID_H
#define _PARIDROID_H

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "libparidroid", __VA_ARGS__)

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "libparidroid", __VA_ARGS__)

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "libparidroid",  __VA_ARGS__)

#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "libparidroid", __VA_ARGS__)

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "libparidroid", __VA_ARGS__)

void paridroid_init(void);

void paridroid_eval(const char *in, char **out, int *returnsgnil);

void paridroid_close(void);

void paridroid_getcommands (char *** commands, int size, int *len);

#endif /*_PARIDROID_H */
