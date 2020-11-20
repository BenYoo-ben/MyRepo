#include "types.h"
#include "stat.h"
#include "user.h"
#include "fcntl.h"
#include "processInfo.h"

int main(void)
{
	int i=0;
	struct processInfo pi;

	printf(2,"\nPID     PPID     SIZE        NUMBER of Context Switch\n");

	for(i=0;i<=get_max_pid();i++)
	{
		if(get_proc_info(i,&pi)==-1)
			continue;
		else
			printf(2,"%d       %d       %d          %d\n",i,pi.ppid,pi.psize,pi.numberContextSwitches);
	}
	exit();
}
