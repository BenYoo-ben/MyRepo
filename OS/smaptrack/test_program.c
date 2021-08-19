#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
int main(int argc, char *args[]){
    
    
    printf("use : %s [+ size] [+ duration]\n",args[0]);

    int plus_size = atoi(args[1]);
    int duration = atoi(args[2]);


    printf("adding %d in every %d secs.\n",plus_size,duration);
    while(1){

    int *a = malloc(plus_size);
    if(a==NULL)
        printf("malloc fail...\n");
    else
        printf("%d allocated...\n",plus_size);

    memset(a,0x1,plus_size);
    
    sleep(duration);

    
    }


}
