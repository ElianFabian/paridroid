#ifndef _PARIDROID_H
#define _PARIDROID_H

/**
 * Allows paridroid to recover from error.
 * @param identifier for error to recover from.
 */

void gp_err_recover(long numerr);


/**
 * Allows paridroid to quit with a given exit code.
 * @oaram the exit code.
 */

void gp_quit(long exitcode);

/**
 * Displays the help menu for a function.
 * @param the name of the function.
 */

char *paridroid_help(const char *s);

/**
 * Wrapper around pari_close() function.
 */

void paridroid_close();

/**
 * Wrapper around pari_init() function. 
 */

void paridroid_init();

/**
 * Evaluates a GP command.
 * @param in: command for GP to evaluate.
 * @return string containing the output from GP.
 */

char *paridroid_eval(const char *in);

#endif /* _PARIDROID_H */
