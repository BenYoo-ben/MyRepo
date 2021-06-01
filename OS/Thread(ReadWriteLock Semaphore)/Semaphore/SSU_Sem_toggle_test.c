#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <wait.h>
#include <pthread.h>
#include "SSU_Sem.h"
#define NUM_THREADS 3
#define NUM_ITER 10

SSU_Sem *sem;
int count;
void *justprint(void *data)
{
  int thread_id = *((int *)data);
  int j;

  for(int i=0; i < NUM_ITER; i++)
    {
	  SSU_Sem_down(sem+(thread_id));
	  printf("This is thread %d\n", thread_id);
	  SSU_Sem_up(sem+(thread_id+1)%NUM_THREADS);
    }
  return 0;
}

int main(int argc, char *argv[])
{

  count=0;

  pthread_t mythreads[NUM_THREADS];
  int mythread_id[NUM_THREADS];

  sem= malloc(sizeof(SSU_Sem) * NUM_THREADS);
  int i;
  for(i=0;i<NUM_THREADS;i++)
  {
	  SSU_Sem_init(sem+i,0);
  }
  SSU_Sem_init(sem,1);
  for(i =0; i < NUM_THREADS; i++)
    {
      mythread_id[i] = i;
      pthread_create(&mythreads[i], NULL, justprint, (void *)&mythread_id[i]);
    }
  
  for(int i =0; i < NUM_THREADS; i++)
    {
      pthread_join(mythreads[i], NULL);
    }
  
  free(sem);

  return 0;
}
