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
    if(pid == 0) {
        _exit(3);
    } else if (pid > 0) {
        // I'm session leader without terminal now
        setsid();

        // session leader may get terminal again later, so call fork to prevent it from happening
        // which will make this process non-session leader

        int pid2 = fork();
        if (pid2 == 0) {
            _exit(4);
        } else if (pid2 > 0) {

        } else {
            exit(1);
        }
    } else {
        perror("fork went south\n");
        exit(1);
    } 

    // close all stdI/Os and open them on /dev/null/
    stdin = freopen("/dev/null", "r", stdin);
    stdout = freopen("/dev/null", "w", stdout);
    stderr = freopen("/dev/null", "w", stderr);
    

    umask(0);

    char buf[LOG_FILE_SIZE];
    snprintf(buf, LOG_FILE_SIZE, "/var/run/byd.%u", getpid());

    int fd = open(buf, O_CREAT | O_TRUNC | O_WRONLY);
    
    if (fd < 0) {
        printf("open went south \n");
        exit(7);
    }
    
    write(fd, buf, LOG_FILE_SIZE);
    close(fd);
}


