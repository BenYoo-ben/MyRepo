#include "types.h"
#include "stat.h"
#include "user.h"
#include "fcntl.h"

int main(void)
{
	  int ret = fork();

	  if (ret == 0)
	  {
		  set_prio(1000);
		  printf(2,"Original priority : %d\n",get_prio());
		  set_prio(75);
		  printf(2,"Change priority to 75, result : %d\n",get_prio());
		  exit();
	  }
	  else
	  {
		  wait();
		  exit();
	  }
}
