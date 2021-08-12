#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

#define __BUF_SIZE__ 4096
#define __LINE_SIZE__ 1024

int main(int argc, char *argv[])
{
    int sleep_sec = 3;
    if(argc==2)
        sleep_sec = atoi(argv[1]);
        
    
    pid_t pid;
    char buffer[__BUF_SIZE__];
    char prev_buffer[__BUF_SIZE__];
 
    short prev_len = -1;
    short len = -1;
   int flag = 0 ;
   while(1){
    int pipe_link[2];

    if(pipe(pipe_link) == -1)
    {
        perror("pipe failed.\n");
        exit(1);
    }
    
    if( (pid=fork()) == -1)
    {
        perror("fork failed");
        exit(2);
    }

    if( pid == 0 ) {
        //child process
        
        //copy STDOUT --> pipe_link[1]
        dup2(pipe_link[1],STDOUT_FILENO);
        close(pipe_link[0]);
        close(pipe_link[1]);

//        printf("%d\n",flag);
        execl("/sbin/iptables","iptables","-L","-nv",(char *)0);

        exit(0);
    }else{
    
    //parent
    
    close(pipe_link[1]);
    
    int len = read(pipe_link[0],buffer,sizeof(buffer));
    
    close(pipe_link[0]);
    
    char line[__LINE_SIZE__];

    short longer_line = -1;
    
    //? statement, comparing now to prev buffer to see which buffer is longer.
    (len > prev_len) ? (longer_line = len) : (longer_line = prev_len) ;


    printf("\n\n%d th check... Displaying changes in %d sec...   read : %d\n",flag,sleep_sec,len);

    
    if(flag)
    {

    int i;
    for(i=0;i<longer_line;i++){
        
        if(buffer[i] != prev_buffer[i])
        {
            int now=i;
            int start,end;
            while(i>=0 && buffer[i]!='\n'){
                i--;
            }
            
            i++;
            start = i;
            i=now;
            
            while(buffer[i] < longer_line && buffer[i]!='\n'){
                i++;
            }

            end = i;
            memset(line,0x0,__LINE_SIZE__);
            memcpy(line,buffer+start,end-start+1);

            line[end-start+1] = '\0';
            
            printf("%s\n",line);
        
        }
    
    }

}
    memset(prev_buffer,0x0,__BUF_SIZE__);
    memcpy(prev_buffer,buffer,__BUF_SIZE__);
    memset(buffer,0x0,__BUF_SIZE__);
    prev_len = len;
    flag++;

    wait(NULL);
    sleep(sleep_sec); 
 }

    }
    return 0;
}
