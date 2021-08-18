#include <stdio.h> //sprintf, printf, etc
#include <fcntl.h> //open
#include <stdlib.h> //exit, atoi
#include <unistd.h> //lseek, read, sleep
#include <string.h> //strcpy, strcmp

#define __SMAPTRACK_DIR_BUFFER_SIZE__ 1024
#define __SMAPTRACK_DATA_COUNT__ 256
//#define __SMAPTRACK_DEBUG__ 
struct smaptrack_block {
	char lines[16][512];
};

	
int main(int argc, char *argv[]) {

    int __SMAPTRACK_HEAP_TRACK__ = 0;

	if (argc < 4) {
    
		printf("Usage : %s [# pid] [# duration(sec)] [# smap line count] [optional --HEAP]\n", argv[0]);
		exit(0);
	}else if(argc >=5 && !strcmp(argv[4],"--HEAP")){
    
        __SMAPTRACK_HEAP_TRACK__ = 1;
    }
    
    int smap_elem_line_count = atoi(argv[3]);
	int sleep_duration = atoi(argv[2]);
    
    long trial_count = 1;
	char dirloc[__SMAPTRACK_DIR_BUFFER_SIZE__ ];

	struct smaptrack_block smapsb[__SMAPTRACK_DATA_COUNT__ ];
    struct smaptrack_block smapsbprev[__SMAPTRACK_DATA_COUNT__];

	memset(dirloc, 0x0, __SMAPTRACK_DIR_BUFFER_SIZE__);

	sprintf(dirloc, "/proc/%d/smaps", atoi(argv[1]));

    printf("Start logging for %s -- line C:%d -- sleep duration --%d\n\n",dirloc,smap_elem_line_count,sleep_duration);
    
	int fd = open(dirloc, O_RDONLY);

    int chunk_size = 16 * 512 * __SMAPTRACK_DATA_COUNT__;
    
    char string_buffer[chunk_size];
    
	while (1) {
    
        printf("[%ld] trial... \n\n",trial_count++);

		int count = 0;
		int i=0,j=0;
		int fin_flag = 0;
        lseek(fd,0,SEEK_SET);
		memset(string_buffer,0x0,chunk_size);
    
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
#elif __SMAPTRACK_HEAP_TRACK__ == 1
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
                if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    
                    if(strstr(smapsb[i].lines[0],"heap"))
                    {
                        for(j=0;j<smap_elem_line_count;j++){
    
                            printf("%s   --->   %s\n",smapsbprev[i].lines[j],smapsb[i].lines[j]);
                            strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                        }
                    break;
                    printf("\n\n");
                    }
                }
            }
        }
#else
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
                if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    
                    for(j=0;j<smap_elem_line_count;j++){
                        printf("%s   --->   %s\n",smapsbprev[i].lines[j],smapsb[i].lines[j]);
                        strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                    }
                    break;
                    printf("\n\n");

                }
            }
        }
#endif
		sleep(sleep_duration);
	}

}
