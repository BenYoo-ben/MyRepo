#include <stdio.h> //sprintf, printf, etc
#include <fcntl.h> //open
#include <stdlib.h> //exit, atoi
#include <unistd.h> //lseek, read, sleep
#include <string.h> //strcpy, strcmp
#include <time.h> //time measure

#define __SMAPTRACK_DIR_BUFFER_SIZE__ 1024
#define __SMAPTRACK_DATA_COUNT__ 128
#define __SMAPTRACK_BLOCK_LINE_MAXIMUM__ 32
#define __SMAPTRACK_LINE_LENGTH__ 128
//#define __SMAPTRACK_DEBUG__ 
struct smaptrack_block {
	char lines[__SMAPTRACK_BLOCK_LINE_MAXIMUM__][__SMAPTRACK_LINE_LENGTH__];
};

int EvaluateSmapElemLineCount(int fd){
    
    int line = 0;
    char c;
    while(1){
        read(fd,&c,1);

        if(c=='\n')
            line++;
        else if(c=='V'){
            read(fd,&c,1);
            if(c=='m'){
                 read(fd,&c,1);
                 if(c=='F'){
                    read(fd,&c,1);
                    if(c=='l')
                    {
                        read(fd,&c,1);
                        if(c=='a'){
                            read(fd,&c,1);
                            if(c=='g'){
                                break;
                            }
                        }
                    }
                 }
            }
        
        }
        
    }

    return line+1;
}

int main(int argc, char *argv[]) {

    int __SMAPTRACK_HEAP_TRACK__ = 0;
	
    if (argc < 2) {
    
		printf("Usage : %s [# pid] [# duration(sec)]  [optional --HEAP]\n", argv[0]);
		exit(0);
	}else if(argc >=4 && !strcmp(argv[3],"--HEAP")){

        __SMAPTRACK_HEAP_TRACK__ = 1;
    }
    int smap_elem_line_count = -1;
	int sleep_duration = atoi(argv[2]);
    
    long trial_count = 1;
	char dirloc[__SMAPTRACK_DIR_BUFFER_SIZE__ ];

	struct smaptrack_block smapsb[__SMAPTRACK_DATA_COUNT__ ];
    struct smaptrack_block smapsbprev[__SMAPTRACK_DATA_COUNT__];

	memset(dirloc, 0x0, __SMAPTRACK_DIR_BUFFER_SIZE__);

	sprintf(dirloc, "/proc/%d/smaps", atoi(argv[1]));

    printf("Start logging for %s -- line C:%d -- sleep duration --%d\n\n",dirloc,smap_elem_line_count,sleep_duration);
    
	int fd = open(dirloc, O_RDONLY);


    //calculate smap_elem_line_count
    smap_elem_line_count = EvaluateSmapElemLineCount(fd);   
    lseek(fd,0,SEEK_SET);
    printf("ELEM COUNT = %d\n",smap_elem_line_count); 
	
   
   //string buffer..
    int chunk_size = smap_elem_line_count * __SMAPTRACK_LINE_LENGTH__ * __SMAPTRACK_DATA_COUNT__;
    
    char string_buffer[chunk_size];

    //MAIN LOOP
    while (1) {
   
        //time measure, to string format..
        time_t curr_time = time(NULL);
        char * time_str = ctime(&curr_time);
        time_str[strlen(time_str)-1] = '\0';
        printf("\n[%ld] -- %s\n",trial_count++,time_str);

		int count = 0;
		int i=0,j=0;
        lseek(fd,0,SEEK_SET);

//      memset(string_buffer,0x0,chunk_size);
        
        //read
        int read_size=0;
        int curr_read = -1;
        while( (curr_read = read(fd,string_buffer+read_size,chunk_size))>0)
        {
            read_size += curr_read;
        }


      //string parse
       char buffer[256];
       int b_i=0;
        while(i<read_size){
        
        if(string_buffer[i]=='\n'){
        
            if(j==smap_elem_line_count){
                j=0;
                count++;
            }

            buffer[b_i] = '\0';
            strcpy(smapsb[count].lines[j],buffer);
 //printf("Written at %d:%d\nData:%s\n\n",count,j,smapsb[count].lines[j]);

            b_i=0;
            j++;
        }
        else{
        buffer[b_i] = string_buffer[i];
         b_i++;
        }
        
        i++;
        }
        buffer[b_i]='\0';
        strcpy(smapsb[count].lines[j],buffer);
        
        //comparison here...
#ifdef __SMAPTRACK_DEBUG__
      //  printf("Running in DEBUG mode-.\n");        
         
		for(i=0;i<count;i++)
		{ 
        printf("printing %d...\n",i);
			for(j=0;j<=smap_elem_line_count;j++)
			{
				printf("%s\n",smapsb[i].lines[j]);
			}
        printf("-----\n");
		}
        printf("\n\n----------------------\n\n");
#else
if(__SMAPTRACK_HEAP_TRACK__ == 1){
       // printf("Running in HEAP_TRACK mode-.\n");
        
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
                if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    
                    if((strstr(smapsb[i].lines[0],"heap"))!=NULL)
                    {
                        for(j=0;j<smap_elem_line_count;j++){
    
                            printf("%s   --->   %s\n",smapsbprev[i].lines[j],smapsb[i].lines[j]);
                            strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                        }
                    printf("\n-------------------------------------------\n");
                    break;
                    }
                }
            }
        }
}
else
{
        //printf("Running in 'all' mode-.\n");
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
                if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    
                    for(j=0;j<smap_elem_line_count;j++){
                        printf("%d] %s   --->   %s\n",j,smapsbprev[i].lines[j],smapsb[i].lines[j]);
                        strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                    }
                    printf("\n----------------------------------------------------------\n");
                    break;
                }
            }
        }
}
#endif
		sleep(sleep_duration);
	}

}
