#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/stat.h>

#define LOG_FILE_SIZE 30

int main(int argc, char *argv[]) {
    int pid = fork();

    // remove control terminal
    // Make it be session leader without control terminal
    // call fork to detach the terminal
    if (pid > 0) {
        _exit(0);
    } else if (pid == 0) {
        // I'm session leader without terminal now
        if (setsid() == -1) {
            perror("setsid failure");
        }

        // session leader may get terminal again later.
        // call fork to prevent it from happening
        // which will make this process non-session leader

        int pid2 = fork();
        if (pid2 > 0) {
            _exit(0);
        } else if (pid2 == 0) {
            // do nothing
        } else {
            perror("fork 2nd failure\n");
            exit(1);
        }
    } else {
        perror("fork failure\n");
        exit(1);
    }

    // zone of grandchild
    // close all stdI/Os and reopen them at /dev/null/ 
    stdin = freopen("/dev/null", "r", stdin);
    stdout = freopen("/dev/null", "w", stdout);
    stderr = freopen("/dev/null", "w", stderr);

    umask(0);

    // log pid
    char buf[LOG_FILE_SIZE];
    snprintf(buf, LOG_FILE_SIZE, "/var/run/byd.%u", getpid());

    int fd = open(buf, O_CREAT | O_TRUNC | O_WRONLY);

    if (fd < 0) {
        printf("open log.pid failure \n");
        exit(7);
    }
    write(fd, buf, LOG_FILE_SIZE);
    close(fd);
    
    // do demon's job
}
