#include "ealloc.h"

#define MAX_CALLING_PAGE_NUM 4

typedef struct _m_list {
	void *base;
	int size;
	int avail;
} m_list;

m_list *mlist[MAX_CALLING_PAGE_NUM];
unsigned int __mem_total__;
unsigned int __mem_size__[MAX_CALLING_PAGE_NUM];
unsigned int __num_chunk__[MAX_CALLING_PAGE_NUM];
int __page_num__;
unsigned int __current_active_page__;

void *mem_init_loc;
void *mem_use_loc[MAX_CALLING_PAGE_NUM];

int mem_for_struct_m_list;

void clean_size_0(int p, int i) {
	int k;
	for (k = i; k < __num_chunk__[p]; k++) {
		mlist[p][k] = mlist[p][k + 1];
	}
	__num_chunk__[p] -= 1;
}

void print_mem_status(int page_num) {
	int i = 0;
	printf("\n==MEMCHECK PAGE : %d mem_avail : %d==\n", page_num,
			__mem_size__[page_num]);
	for (i = 0; i < __num_chunk__[page_num] + 1; i++) {
		printf("Chunk#%d- base = %p, size = %d, avail = %d\n", i,
				mlist[page_num][i].base, mlist[page_num][i].size,
				mlist[page_num][i].avail);

	}
	printf("==MEMCHECK PAGE : %d==\n\n", page_num);
}

int activate_new_page(int page_num) {

	mem_use_loc[page_num] = mmap(0, PAGESIZE, PROT_READ | PROT_WRITE,
			MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
	if (mem_use_loc[page_num] == MAP_FAILED) {
		perror("mmap failed\n");
		return 1;
	}

	__num_chunk__[page_num] = 0;
	__mem_size__[page_num] = PAGESIZE;
	mlist[page_num][0].base = mem_use_loc[page_num];
	mlist[page_num][0].avail = 1;
	mlist[page_num][0].size = PAGESIZE;
	return 0;
}

void* alloc_at_page(int page_num, int req_size) {
	int size_check = 0, i;
	int tmp;
	//print_mem_status(page_num);
	for (i = 0; i < __num_chunk__[page_num] + 1; i++) {

		if (mlist[page_num][i].avail == 1
				&& (mlist[page_num][i].size) >= req_size) {
			size_check = 1;

			__num_chunk__[page_num]++;

			mlist[page_num][__num_chunk__[page_num]].base =
					mlist[page_num][i].base
							+ (mlist[page_num][i].size - req_size);

			mlist[page_num][__num_chunk__[page_num]].size = req_size;
			mlist[page_num][__num_chunk__[page_num]].avail = 0;
			mlist[page_num][i].size -= req_size;
			if (mlist[page_num][i].size == 0)
				clean_size_0(page_num, i);

			break;
		}
	}
	if (size_check == 0) {
		perror("no chunk can handle req\n");
		return NULL;
	}
	__mem_size__[page_num] -= req_size;

	return (char*) (mlist[page_num][__num_chunk__[page_num]].base);
}

int dealloc_merge(int page_num, int i, int mode) {
	int j, k;
	if (mode == 1 && mlist[page_num][i].avail == 0)
		return 3;

	for (j = 0; j < __num_chunk__[page_num] + 1; j++) {
		if (mlist[page_num][j].avail == 0)
			continue;

		else if (mlist[page_num][j].base + mlist[page_num][j].size
				== mlist[page_num][i].base) {
			mlist[page_num][j].size += mlist[page_num][i].size;
			for (k = i; k < __num_chunk__[page_num]; k++) {
				mlist[page_num][k] = mlist[page_num][k + 1];
			}
			__num_chunk__[page_num] -= 1;

			return 1;
		} else if ((mlist[page_num][i].base + mlist[page_num][i].size)
				== mlist[page_num][j].base) {
			mlist[page_num][j].base -= mlist[page_num][i].size;
			mlist[page_num][j].size += mlist[page_num][i].size;
			for (k = i; k < __num_chunk__[page_num]; k++) {
				mlist[page_num][k] = mlist[page_num][k + 1];
			}
			__num_chunk__[page_num] -= 1;
			//print_mem_status();
			return 2;
		}
	}
	return 0;
}

void init_alloc() {

	mem_for_struct_m_list = ((PAGESIZE / MINALLOC) + 1) * sizeof(m_list);

	mem_init_loc = mmap(0, mem_for_struct_m_list * MAX_CALLING_PAGE_NUM,
			PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
	if (mem_init_loc == MAP_FAILED) {
		perror("mmap failed\n");
		return;
	}
	int i = 0;

	for (i = 0; i < MAX_CALLING_PAGE_NUM; i++) {
		mlist[i] = mem_init_loc + i * mem_for_struct_m_list;
	}

	__mem_total__ = PAGESIZE;

	for (i = 0; i < MAX_CALLING_PAGE_NUM; i++) {
		__mem_size__[i] = -1;
		__num_chunk__[i] = -1;
	}

	__page_num__ = -1;
	unsigned int __current_active_page__ = -1;

	return;
}
void cleanup() {
	int stat_check;
	stat_check = munmap(mem_init_loc,
			mem_for_struct_m_list * MAX_CALLING_PAGE_NUM);
	if (stat_check == -1)
		perror("munmap fail");

	int i;
	for (i = 0; i <= __page_num__; i++) {
		stat_check = munmap(mem_use_loc[i], PAGESIZE);
		if (stat_check == -1)
			perror("munmap fail");
	}

}
char* alloc(int req_size) {
	/*int tmp;
	 for(tmp=0;tmp<__page_num__ +1; tmp++)
	 print_mem_status(tmp);
	 */
	if (req_size % 256 != 0 && req_size <= 0) {
		perror("num != mult of MINALLOC");
		return NULL;
	}
	int i;
	if (__page_num__ < 0) {
		__page_num__++;
		if (activate_new_page(__page_num__) == 1)
			return NULL;
	}

	int p;
	char *return_addr;
	for (p = 0; p < __page_num__ + 1; p++) {

		if (__mem_size__[p] < req_size) {

			if (__page_num__ < MAX_CALLING_PAGE_NUM && p == __page_num__) {

				__page_num__++;
				activate_new_page(__page_num__);
				continue;
			} else
				continue;
		}

		return_addr = alloc_at_page(p, req_size);

		if (return_addr != NULL) {

			return (char*) return_addr;
		} else if (__page_num__ < MAX_CALLING_PAGE_NUM) {
			if (p == __page_num__) {

				__page_num__++;
				activate_new_page(__page_num__);
				continue;
			}
			continue;
		} else {
			return NULL;
		}

	}

}
void dealloc(char *dealloc_base) {
	int i, j, p;

	for (p = 0; p < __page_num__ + 1; p++) {
		for (i = 0; i < __num_chunk__[p] + 1; i++) {
			if (mlist[p][i].avail == 0 && mlist[p][i].base == dealloc_base) {

				__mem_size__[p] += mlist[p][i].size;

				if (dealloc_merge(p, i, 0) > 0) {
					for (j = __num_chunk__[p]; j >= 0; j--)
						dealloc_merge(p, j, 1);
					return;
				}

				mlist[p][i].avail = 1;
				return;
			}
		}
	}

}
