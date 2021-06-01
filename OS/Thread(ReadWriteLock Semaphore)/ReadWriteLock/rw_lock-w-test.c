#include "rw_lock.h"

void init_rwlock(struct rw_lock * rw)
{
  //	Write the code for initializing your read-write lock.
	pthread_mutex_init(&(rw->mutex),NULL);
	pthread_cond_init(&(rw->cond),0);
	pthread_cond_init(&(rw->cond2),0);
	rw->read_thread_count=0;
	rw->write_thread_flag=0;
	rw->read_waiting_count=0;
	rw->write_waiting_count=0;


}

void r_lock(struct rw_lock * rw)
{
	pthread_mutex_lock(&(rw->mutex));

	while(rw->write_waiting_count!=0)
		pthread_cond_wait(&(rw->cond),&(rw->mutex));

	rw->read_thread_count++;
	pthread_mutex_unlock(&(rw->mutex));


  //	Write the code for aquiring read-write lock by the reader.
}

void r_unlock(struct rw_lock * rw)
{
	pthread_mutex_lock(&(rw->mutex));

	rw->read_thread_count--;
	pthread_mutex_unlock(&(rw->mutex));
	pthread_cond_signal(&(rw->cond2));

  //	Write the code for releasing read-write lock by the reader.
}

void w_lock(struct rw_lock * rw)
{
	pthread_mutex_lock(&(rw->mutex));


		rw->write_waiting_count++;
		while(rw->read_thread_count || rw->write_thread_flag)
			{
				pthread_cond_wait(&(rw->cond2),&(rw->mutex));
			}

	rw->write_thread_flag++;
	pthread_mutex_unlock(&(rw->mutex));
  //	Write the code for aquiring read-write lock by the writer.
}

void w_unlock(struct rw_lock * rw)
{
	pthread_mutex_lock(&(rw->mutex));
	rw->write_waiting_count--;
	rw->write_thread_flag--;
	if(rw->write_waiting_count>0)
		pthread_cond_signal(&(rw->cond2));
	else
		pthread_cond_broadcast(&(rw->cond));

	pthread_mutex_unlock(&(rw->mutex));
  //	Write the code for releasing read-write lock by the writer.
}
