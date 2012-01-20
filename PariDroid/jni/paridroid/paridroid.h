#ifndef _PARIDROID_H
#define _PARIDROID_H

void paridroid_init(void);

char *paridroid_eval(const char *in);

//char *paridroid_next_input(void);

//char *paridroid_prev_input(void);

//char *paridroid_next_output(void);

//char *paridroid_prev_output(void);

//int paridroid_save_history(const char *file_name);

void paridroid_close(void);

#endif /*_PARIDROID_H */
