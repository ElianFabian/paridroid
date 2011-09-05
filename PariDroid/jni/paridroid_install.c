#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>

#include "paridroid_install.h"
#include "log.h"

int install(void)
{
  
  mode_t exec = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IXOTH | S_IROTH;

  if (chmod("/data/data/com.math.paridroid/paridroid-installer.sh", exec) < 0) {
    log_write(LOG_ERROR, "Error! Failed to make install script executable: %s", strerror(errno));
    return -1;
  } else {
    log_write(LOG_INFO, "Set install script to executable.");
  }

  if (system("su -c /data/data/com.math.paridroid/paridroid-installer.sh") != 0) {
    log_write(LOG_ERROR, "Error! Failed to run install script: %s", strerror(errno));
    return -1;
  } else {
    log_write(LOG_INFO, "Executed install script without error.");
    return 0;
  }
}
