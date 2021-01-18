#include  <stdio.h>
#include  <sys/types.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_INPUT_SIZE 1024
#define MAX_TOKEN_SIZE 64
#define MAX_NUM_TOKENS 64
char **tokenize(char *line);
char **tokenize(char *line)
{
		char **tokens=NULL;
		char* token = NULL;
		tokens = (char **)malloc(MAX_NUM_TOKENS * sizeof(char *));
  if(tokens==NULL)
  {
	  perror("malloc fail1\n");
  }
  token = (char*)malloc(MAX_TOKEN_SIZE * sizeof(char));
  if(token==NULL)
   {
  	  perror("malloc fail2\n");
   }
  int i, tokenIndex = 0, tokenNo = 0;
	  for(i =0; i < strlen(line); i++){
	      char readChar = line[i];
	      if (readChar == ' ' || readChar == '\n' || readChar == '\t'){
	        token[tokenIndex] = '\0';
	        if (tokenIndex != 0){
	      	 tokens[tokenNo] = (char*)malloc(MAX_TOKEN_SIZE*sizeof(char));
	      	if(tokens[tokenNo]==NULL)
	      	  {
	      		  perror("malloc fail4\n");
	      	  }
	      	 strcpy(tokens[tokenNo++], token);
	      	 tokenIndex = 0;
	        }
	      } else {
	        token[tokenIndex++] = readChar;
	      }
	    }
  tokens[tokenNo] = NULL;
  return tokens;
}
typedef struct _pipe
{
	int loc;
	int fd[2];
}Pipe;
int main(int argc, char* argv[]) {
	char  line[MAX_INPUT_SIZE];
	int i,j,k,l;
	int token_no;
	int pipe_count=0;
	int pipe_offset=-1;
	pid_t pid;
	FILE* fp;
	if(argc == 2) {
		fp = fopen(argv[1],"r");
		if(fp < 0) {
		perror("File doesn't exists.");
			return -1;
		}
	}
	while(1) {
		/* BEGIN: TAKING INPUT */
		bzero(line, sizeof(line));
		if(argc == 2) { // batch mode
			if(fgets(line, sizeof(line), fp) == NULL)
			{ // file reading finished
				break;
			}
			line[strlen(line)-1] = '\0';
		} else
		{ // interactive mode
			printf("$ ");
			scanf("%[^\n]", line);
			getchar();
		}
		char**  tokens= NULL;
		char cmd_args[MAX_NUM_TOKENS][MAX_TOKEN_SIZE];
		line[strlen(line)] = '\n';
		tokens = tokenize(line);
		if(tokens==NULL)
			continue;
		pipe_count=0,token_no=0;
		for(i=0;tokens[i]!=NULL;i++)
		{
			token_no++;
			if(strcmp(tokens[i],"|")==0)
				{
					pipe_count++;
				}
		}
		Pipe pipes[pipe_count];
		for(i=0,j=0;tokens[i]!=NULL;i++)
		{
			if(strcmp(tokens[i],"|")==0)
			{
				pipes[j++].loc = i;
			}
		}
			for(i=0;i<token_no;i++)
			{
				if(strcmp(tokens[i],"|")==0)
				{
					tokens[i] = NULL;
				}
			}
		for(i=0,j=0;tokens[i]!=NULL;)
		{
			if(pipe_count>0 )
			{
				if( j == pipe_count+1)
				{
					break;
				}
				if(pipe(pipes[j].fd)<0)
				{
					perror("pipe err\n");
					exit(1);
				}
				pid_t pid;
				if(j==0)
				{
					//printf("First run\n");
					pid = fork();
					if(pid<0)
					{
						perror("fork err\n");
						exit(1);
					}
					else if(pid==0)
					{
						dup2(pipes[j].fd[1],STDOUT_FILENO);
						close(pipes[j].fd[0]);
						close(pipes[j].fd[1]);
						if(execvp(tokens[i],tokens+i)!=-1)
						{
							printf("SSUShell : Incorrect command\n");
							break;
						}
						exit(1);
					}
					else
					{
						int status;
						waitpid(pid,&status,0);
					}
				}
				else if(j==pipe_count)
				{
					//printf("Last run\n");
					pid=fork();
					if(pid<0)
					{
						perror("pipe err2\n");
						exit(1);
					}else if(pid==0)
					{
						dup2(pipes[j-1].fd[0],STDIN_FILENO);
						close(pipes[j-1].fd[1]);
						close(pipes[j-1].fd[0]);
						if(execvp(tokens[i],tokens+i)!=-1)
						{
							printf("SSUShell : Incorrect command\n");
							break;
						}
						exit(1);
					}
					else
					{
						int status;
						close(pipes[j-1].fd[0]);
						close(pipes[j-1].fd[1]);
						waitpid(pid,&status,0);
						break;
					}
				}
				else
				{
					//printf("Middle run\n");
					pid=fork();
					if(pid<0)
					{
						perror("pipe err2\n");
						exit(1);
					}else if(pid==0)
					{
						dup2(pipes[j-1].fd[0],STDIN_FILENO);
						close(pipes[j-1].fd[1]);
						close(pipes[j-1].fd[0]);
						dup2(pipes[j].fd[1],STDOUT_FILENO);
						close(pipes[j].fd[0]);
						close(pipes[j].fd[1]);
						if(execvp(tokens[i],tokens+i)!=-1)
						{
							printf("SSUShell : Incorrect command\n");
							break;
						}
						exit(1);
					}
					else
					{
						int status;
						close(pipes[j-1].fd[0]);
						close(pipes[j-1].fd[1]);
						waitpid(pid,&status,0);
					}
				}
				i=pipes[j++].loc+1;
			}
			else
			{
				pid =fork();
				if(pid==-1)
				{
				fprintf(stderr,"Error while forking a child\n");
				exit(1);
				}else if(pid==0)
				{
					if(execvp(tokens[i],tokens+i)!=-1);
					{
					printf("SSUShell : Incorrect command\n");
					break;
					}
					exit(1);
				}
				int status;
				waitpid(pid,&status,0);
				break;
		}
		}
		for(i=0;i<token_no;i++)
		{
			if(tokens[i]!=NULL)
				free(tokens[i]);
			tokens[i]=NULL;
		}
	}
	return 0;
}