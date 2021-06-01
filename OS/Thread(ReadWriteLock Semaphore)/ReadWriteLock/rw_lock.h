#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

struct rw_lock
{
	pthread_mutex_t  mutex;
	pthread_cond_t cond;
	pthread_cond_t cond2;
	int read_thread_count;
	int write_thread_flag;
	int read_waiting_count;
	int write_waiting_count;
	int init_flag;
};

void init_rwlock(struct rw_lock * rw);
void r_lock(struct rw_lock * rw);
void r_unlock(struct rw_lock * rw);
void w_lock(struct rw_lock * rw);
void w_unlock(struct rw_lock * rw);
long *max_element(long* start, long* end);
long *min_element(long* start, long* end);
