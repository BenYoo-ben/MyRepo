#include "alloc.h"

typedef struct _m_list {
	void *base;
	int size;
	int avail;
} m_list;

m_list *mlist;
unsigned int __mem_total__;
unsigned int __mem_size__;
unsigned int __num_chunk__;

void *mem_init_loc;
void *mem_use_loc;

void clean_size_0(int i)
{
	int k;
	for(k=i;k< __num_chunk__;k++)
	{
		mlist[k] = mlist[k+1];
	}
	__num_chunk__ -= 1;
}

int dealloc_merge(int i, int mode)
{
	if(mode==1 && mlist[i].avail==0) return 3;

	int j,k;
	for (j = 0; j < __num_chunk__ + 1; j++) {
		if (mlist[j].avail == 0)
			continue;
		else if (mlist[j].base + mlist[j].size
				== mlist[i].base) {
			//printf("Merge style 1\n");
			mlist[j].size += mlist[i].size;
			for(k=i;k< __num_chunk__;k++)
			{
				mlist[k] = mlist[k+1];
			}
			__num_chunk__ -= 1;

			return 1;
		}
		else if( ( mlist[i].base + mlist[i].size) == mlist[j].base)
		{
			//printf("Merge style 2\n");
			mlist[j].base -=  mlist[i].size;
			mlist[j].size += mlist[i].size;
			for(k=i;k< __num_chunk__;k++)
			{
				mlist[k] = mlist[k+1];
			}
			__num_chunk__ -= 1;
			//print_mem_status();
			return 2;
		}
		/*else if( (mlist[j].size==0 && mlist[j].base + mlist[i].size == mlist[i].base))
		{
			mlist[j].size = mlist[i].size;
			for(k=i;k< __num_chunk__;k++)
			{
				mlist[k] = mlist[k+1];
			}
			__num_chunk__ -= 1;
			return 4;
		}*/
	}
	return 0;
}
void print_mem_status()
{
	int i=0;
	printf("\n==MEMCHECK== mem_avail : %d \n",__mem_size__);
	for(i=0;i<__num_chunk__+1;i++)
	{
		printf("Chunk#%d- base = %p, size = %d, avail = %d\n",i,mlist[i].base,mlist[i].size,mlist[i].avail);

	}
	printf("==MEMCHECK==\n\n");
}

int init_alloc() {

	void *mem_init_loc;
	int mem_for_struct_m_list = ((PAGESIZE / MINALLOC)+1) * sizeof(m_list);

	__mem_total__ = PAGESIZE;
	__mem_size__ = PAGESIZE;
	__num_chunk__ = 0;

	printf("mem req = %d\n", mem_for_struct_m_list);

	mem_init_loc = mmap(0, mem_for_struct_m_list,
			PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
	if (mem_init_loc == MAP_FAILED) {
		perror("mmap failed\n");
		return 1;
	}

	mem_use_loc = mmap(0, PAGESIZE,
				PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
		if (mem_init_loc == MAP_FAILED) {
			perror("mmap failed\n");
			return 1;
		}

	mlist = mem_init_loc;

	mlist[0].base = mem_use_loc;
	mlist[0].avail = 1;
	mlist[0].size = PAGESIZE;

	return 0;
}
int cleanup()
{
	int stat_check;
	stat_check = munmap(mem_init_loc,(PAGESIZE / MINALLOC) * sizeof(m_list) + PAGESIZE);
	if(stat_check==-1) return 1;
	return 0;
}
char* alloc(int req_size) {
	//printf("Alloc %d",req_size);
	if (req_size % 8 != 0 && req_size <= 0) {
		perror("num != mult of MINALLOC");
		return NULL;
	}

	if (__mem_size__ < req_size) {
		perror("not enough mem size");
		return NULL;
	}

	int i;
	int size_check = 0;
	for (i = 0; i < __num_chunk__ + 1; i++) {
		if (mlist[i].avail == 1&&(mlist[i].size) >= req_size) {
			size_check = 1;
			__num_chunk__++;
			mlist[__num_chunk__].base = mlist[i].base
					+ (mlist[i].size - req_size);
			mlist[__num_chunk__].size = req_size;
			mlist[__num_chunk__].avail = 0;
			mlist[i].size -= req_size;
			if(mlist[i].size==0) clean_size_0(i);
			break;
		}
	}
	if (size_check == 0) {
		perror("no chunk can handle req\n");
		return NULL;
	} else
	{
		//print_mem_status();
		__mem_size__ -= req_size;
		//printf(" at %p\n",(char*) (mlist[__num_chunk__].base));
		//print_mem_status();
		return (char*) (mlist[__num_chunk__].base);
	}



}
void dealloc(char *dealloc_base) {
	int i,j;
	//printf("Dealloc #%p\n",dealloc_base);
	//print_mem_status();
	for (i = 0; i < __num_chunk__ + 1; i++) {
		if (mlist[i].base == dealloc_base) {
			//printf("Deallocating %p which size :%d\n",mlist[i].base,mlist[i].size);

			__mem_size__ +=mlist[i].size;
			if(dealloc_merge(i,0)>0)
			{
				for(j=__num_chunk__;j>=0;j--)
					dealloc_merge(j,1);

				//print_mem_status();
				return ;
			}


			mlist[i].avail=1;
			//print_mem_status();
			return ;
		}
	}


}
