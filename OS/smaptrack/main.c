#include <stdio.h> //sprintf, printf, etc
#include <fcntl.h> //open
#include <stdlib.h> //exit, atoi
#include <unistd.h> //lseek, read, sleep
#include <string.h> //strcpy, strcmp
#include <time.h> //time measure

/*
* just some big enough numbers.
* if using this program in a super limited environment,
* you might want to adjust values.
*/
#define __SMAPTRACK_DIR_BUFFER_SIZE__ 1024
#define __SMAPTRACK_BLOCK_LINE_MAXIMUM__ 50
#define __SMAPTRACK_LINE_LENGTH__ 128

// print every each line, configured.
//#define __SMAPTRACK_DEBUG__ 

//structure for parsing,
struct smaptrack_block {
	char lines[__SMAPTRACK_BLOCK_LINE_MAXIMUM__][__SMAPTRACK_LINE_LENGTH__];
};

//clear bits to 0.
void InitSmaptrackBlock(struct smaptrack_block *sb){

        memset(sb->lines,0x0, __SMAPTRACK_BLOCK_LINE_MAXIMUM__ * __SMAPTRACK_LINE_LENGTH__);
}

//count line by reading "general" last string in smap (VmFlag)
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

// main function.
int main(int argc, char *argv[]) {
    int smaptrack_data_count = 128;
    int __SMAPTRACK_GREP_TRACK__ = 0;

    int grep_block_count = 0;
    char grep_block[__SMAPTRACK_BLOCK_LINE_MAXIMUM__][64];
    int smaptrack_fd = STDOUT;
    if (argc < 2) {
    
		printf("Usage : %s [# pid] [# duration(sec)]  [--grep=string (option)]\n [--data_count=# (option)] [--file=string (option)] \n", argv[0]);
		exit(0);
    }else if(argc > 3){
        //parse arguments...
        int iter = 2;

        while(iter < argc-1)
        {
            iter++;

            char *c_p = NULL;
            
            if( (c_p = strstr(argv[iter],"--grep"))!= NULL){
                c_p = strstr(c_p,"=");
                sprintf(grep_block[grep_block_count],"%s",c_p+1);
                grep_block[grep_block_count][strlen(grep_block[grep_block_count])]='\0';
                grep_block_count++;
                __SMAPTRACK_GREP_TRACK__ = 1;
            }
            else if( (c_p = strstr(argv[iter],"--data_count"))!=NULL){
                c_p = strstr(c_p,"=");
                char buffer[100];
                sprintf(buffer,"%s",c_p+1);
                buffer[strlen(buffer)] = '\0';
                smaptrack_data_count = atoi(buffer);
           }else if( (c_p =strtsr(argv[iter], "--file"))!=NULL){
		c_p = strstr(c_p,"=");
		char buffer[100];
		sprintf(buffer,"%s",c_p+1);
		buffer[strlen(buffer)] = '\0';
		smaptrack_stdout = open(buffer,O_WRONLY|O_CREAT|O_APPEND);	
		
		}

            
        }
    
    }
//initialize variables...
 
    int smap_elem_line_count = -1;
    int sleep_duration = atoi(argv[2]);
    
    long trial_count = 1;
    char dirloc[__SMAPTRACK_DIR_BUFFER_SIZE__ ];

    struct smaptrack_block smapsb[smaptrack_data_count];
    struct smaptrack_block smapsbprev[smaptrack_data_count];
   
    int i,k;

//more initializations, clear bits...
    for(i=0;i<smaptrack_data_count;i++){
        InitSmaptrackBlock(&(smapsb[i]));
        InitSmaptrackBlock(&(smapsbprev[i]));
    }
	memset(dirloc, 0x0, __SMAPTRACK_DIR_BUFFER_SIZE__);

	sprintf(dirloc, "/proc/%d/smaps", atoi(argv[1]));

    printf("Start logging for %s -- line C:%d -- sleep duration --%d\n\n",dirloc,smap_elem_line_count,sleep_duration);
    
	int fd = open(dirloc, O_RDONLY);


    //calculate smap_elem_line_count
    smap_elem_line_count = EvaluateSmapElemLineCount(fd);   
    lseek(fd,0,SEEK_SET);
#ifdef __SMAPTRACK_DEBUG__
    fprintf(smaptrack_fd,"ELEM COUNT = %d\n",smap_elem_line_count); 
#endif	
   
   //string buffer..
    int chunk_size = smap_elem_line_count * __SMAPTRACK_LINE_LENGTH__ * smaptrack_data_count;
    
    char string_buffer[chunk_size];

    //MAIN LOOP
    while (1) {
   
        //time measure, to string format..
        time_t curr_time = time(NULL);
        char * time_str = ctime(&curr_time);
        time_str[strlen(time_str)-1] = '\0';
        fprintf(smaptrack_fd,"\n[%ld] -- %s\n",trial_count++,time_str);

		int count = 0;
		int i=0,j=0;
        lseek(fd,0,SEEK_SET);
        
        //read
        int read_size=0;
        int curr_read = -1;
        while( (curr_read = read(fd,string_buffer+read_size,chunk_size))>0)
        {
            read_size += curr_read;
        }


      //string parse + prepare struct
       char buffer[__SMAPTRACK_LINE_LENGTH__];
       int b_i=0;
        while(i<read_size){
        
        if(string_buffer[i]=='\n'){
        
            if(j==smap_elem_line_count){
                j=0;
            count++;
            
            }

            buffer[b_i] = '\0';
            strcpy(smapsb[count].lines[j],buffer);

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
#else
if(__SMAPTRACK_GREP_TRACK__ == 1){
        
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
             	//if line != previously read line.
		   if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    
                     int match = 0;
                     for(k=0;k<grep_block_count;k++){
                        // + if line is what you want to grep.
                        if(strstr(smapsb[i].lines[0],grep_block[k])!= NULL ){
                            match=1;
                            break;
                        }
                     }
                    if(match==1){
			// this is what I wanted to grep, so print it.
                     for(j=0;j<smap_elem_line_count;j++){
                            
                         if(j==0)
                             fprintf(smaptrack_fd,"%s\n",smapsb[i].lines[j]);
                         else{
                         fprintf(smaptracK_fd,"%-30s%-10s%30s\n",smapsbprev[i].lines[j],"   --->",smapsb[i].lines[j]);
                         }
                         strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                         
                    }
                    
                    fprintf(fd,"\n--------------------------------------------------\n");
                    }
                    break;

                }
            }
        }
}
else
{
        //printf("Running in 'all' mode or none 'grep-ping' mode\n");
        for(i=0;i<count;i++)
        {
            for(j=0;j<smap_elem_line_count;j++)
            {
		//hmm... let's see if value's changed compared to previous read event.
                if(strcmp(smapsb[i].lines[j],smapsbprev[i].lines[j])){
                    for(j=0;j<smap_elem_line_count;j++){
                           //print! print!  
                            if(j==0)
                                fprintf(smaptrack_fd,"%s\n",smapsb[i].lines[j]);
                            else{
                            fprintf(smaptrack_fd,"%-30s%-10s%30s\n",smapsbprev[i].lines[j],"   --->",smapsb[i].lines[j]);
                            }
                            strcpy(smapsbprev[i].lines[j], smapsb[i].lines[j]);
                          
                    }
                    fprintf(smaptrack_fd,"\n----------------------------------------------------------\n");
                    break;
                }
            }
        }
}
#endif
		//wait for a while... could be improved using nanosleep.
		sleep(sleep_duration);
	}

}
