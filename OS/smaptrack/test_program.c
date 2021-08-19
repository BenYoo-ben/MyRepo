#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char *args[]){
    
    
    printf("use : %s [+ size] [+ duration]\n",args[0]);

    int plus_size = atoi(args[1]);
    int duration = atoi(args[2]);

    while(1){

    malloc(plus_size);

    sleep(duration);

    
    }


}
