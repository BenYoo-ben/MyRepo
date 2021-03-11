#include "ssufs-ops.h"

extern struct filehandle_t file_handle_array[MAX_OPEN_FILES];

int ssufs_allocFileHandle() {
	for (int i = 0; i < MAX_OPEN_FILES; i++) {
		if (file_handle_array[i].inode_number == -1) {
			return i;
		}
	}
	return -1;
}

int ssufs_create(char *filename) {

	if (open_namei(filename) != -1) {
		return -1;
	}
	/*int data_block_num;

	 if ((data_block_num = ssufs_allocDataBlock()) == -1) {
	 return -1;
	 }
	 */
	int inode_num = ssufs_allocInode();

	if(inode_num==-1)
		return -1;

	struct inode_t *inodeptr = malloc(sizeof(struct inode_t));
	strcpy(inodeptr->name, filename);
	inodeptr->file_size = 0;
	inodeptr->status = INODE_IN_USE;
	int i;
	for (i = 0; i < MAX_FILE_SIZE; i++) {
		inodeptr->direct_blocks[i] = -1;
	}
	//inodeptr->direct_blocks[0] = data_block_num;

	ssufs_writeInode(inode_num, inodeptr);
	free(inodeptr);
	//printf("created num = %d\n", inode_num);

	return inode_num;

}

void ssufs_delete(char *filename) {
	int inode_num;

	if ((inode_num = open_namei(filename)) == -1)
		return;

	struct inode_t *inodeptr = malloc(sizeof(struct inode_t));
	ssufs_readInode(inode_num, inodeptr);

	int i;

	for (i = 0; i < MAX_OPEN_FILES; i++) {
		if (file_handle_array[i].inode_number == inode_num)
			ssufs_close(i);
	}

	inodeptr->file_size = 0;
	inodeptr->status = INODE_FREE;
	memset(inodeptr->name, 0, MAX_NAME_STRLEN);

	ssufs_freeInode(inode_num);

	free(inodeptr);
}

int ssufs_open(char *filename) {
	int inode_num;

	if ((inode_num = open_namei(filename)) == -1)
		return -1;

	int i, file_index_no;
	for (i = 0; i < MAX_OPEN_FILES; i++) {
		if (file_handle_array[i].inode_number == -1)
			file_index_no = i;
	}

	file_handle_array[file_index_no].inode_number = inode_num;
	file_handle_array[file_index_no].offset = 0;

	//printf("opened inode_num = %d\n", inode_num);

	return file_index_no;

}

void ssufs_close(int file_handle) {
	file_handle_array[file_handle].inode_number = -1;
	file_handle_array[file_handle].offset = 0;
}

int ssufs_read(int file_handle, char *buf, int nbytes) {
	int inode_num = file_handle_array[file_handle].inode_number;
	int offset = file_handle_array[file_handle].offset;

	struct inode_t *inodeptr = malloc(sizeof(struct inode_t));
	ssufs_readInode(inode_num, inodeptr);

	if ((nbytes + offset) > inodeptr->file_size)
		return -1;

	int i, fortop = -1;
	char *buffer = malloc(sizeof(char) * BLOCKSIZE);
	int read_offset = 0;

	if ((nbytes + offset) % BLOCKSIZE == 0)
		fortop = (nbytes + offset) / BLOCKSIZE;
	else
		fortop = (nbytes + offset) / BLOCKSIZE + 1;

	for (i = 0; i < fortop; i++) {
		if (offset > BLOCKSIZE) {
			offset -= BLOCKSIZE;
			continue;
		} else if (offset > 0) {
			ssufs_readDataBlock(inodeptr->direct_blocks[i], buffer);
			memcpy(buf + read_offset, buffer + offset, (BLOCKSIZE - offset));
			read_offset += (BLOCKSIZE - offset);
			offset = 0;
		} else {
			ssufs_readDataBlock(inodeptr->direct_blocks[i], buffer);
			memcpy(buf + read_offset, buffer, BLOCKSIZE);
			read_offset += BLOCKSIZE;
		}
	}

	file_handle_array[file_handle].offset += nbytes;
	return 0;
}

int ssufs_write(int file_handle, char *buf, int nbytes) {
	int inode_num = file_handle_array[file_handle].inode_number;
	int offset = file_handle_array[file_handle].offset;

	int new_allocated_block_num[4];
	int count = 0,i;

	struct inode_t *inodeptr = malloc(sizeof(struct inode_t));
	ssufs_readInode(inode_num, inodeptr);

	if (nbytes + offset > MAX_FILE_SIZE * BLOCKSIZE)
		return -1;

	if (nbytes + offset > (inodeptr->file_size)) {
		while (inodeptr->file_size <= BLOCKSIZE * MAX_FILE_SIZE
				&& nbytes > (inodeptr->file_size- offset)) {
			inodeptr->file_size += BLOCKSIZE;
			//printf("NEW ALLOC\n");
			new_allocated_block_num[count] = ssufs_allocDataBlock();
			inodeptr->direct_blocks[((inodeptr->file_size) / BLOCKSIZE) - 1] =
					new_allocated_block_num[count];
			count++;
			if (inodeptr->direct_blocks[((inodeptr->file_size) / BLOCKSIZE) - 1]
					== -1) {
				inodeptr->file_size -= BLOCKSIZE;
				for(i=0;i<count;i++)
					ssufs_freeDataBlock(new_allocated_block_num[i]);

				return -1;
			}
		}
	}

	int start_num = 0;
	while (BLOCKSIZE < offset) {
		start_num++;
		offset -= BLOCKSIZE;
	}

	char *wholebuffer = malloc(sizeof(char) * (nbytes + offset));

	memset(wholebuffer, 0, offset);
	memcpy(wholebuffer + offset, buf, nbytes);

	char *buffer = malloc(sizeof(char) * BLOCKSIZE);
	int tot_size = nbytes + offset;

	//printf("writing %s \nsize %d\n", wholebuffer, tot_size);
	file_handle_array[file_handle].offset += nbytes;

	int j = 0;
	while (tot_size > 0) {
		if (tot_size >= BLOCKSIZE)
			memcpy(buffer, wholebuffer + j, BLOCKSIZE);
		else
			memcpy(buffer, wholebuffer + j, tot_size);

		//printf("writing to block %d\ndata : %s\n", inodeptr->direct_blocks[i],
				//buffer);
		//printf("comparing to %d\n", new_allocated_block_num[i]);
		tot_size -= BLOCKSIZE;
		j += BLOCKSIZE;

		ssufs_writeDataBlock(inodeptr->direct_blocks[i], buffer);
		i++;
	}
	ssufs_writeInode(inode_num, inodeptr);
	free(inodeptr);
	free(wholebuffer);
	free(buffer);

	return 0;

}

int ssufs_lseek(int file_handle, int nseek) {
	int offset = file_handle_array[file_handle].offset;

	struct inode_t *tmp = (struct inode_t*) malloc(sizeof(struct inode_t));
	ssufs_readInode(file_handle_array[file_handle].inode_number, tmp);

	int fsize = tmp->file_size;

	offset += nseek;

	if ((fsize == -1) || (offset < 0) || (offset > fsize)) {
		free(tmp);
		return -1;
	}

	file_handle_array[file_handle].offset = offset;
	free(tmp);

	return 0;
}
