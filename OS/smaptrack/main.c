#include <stdio.h> //sprintf, printf, etc
#include <fcntl.h> //open
#include <stdlib.h> //exit, atoi
#include <unistd.h> //lseek, read, sleep
#include <string.h> //strcpy

#define __SMAPTRACK_DIR_BUFFER_SIZE__ 1024
#define __SMAPTRACK_DATA_COUNT__ 256

struct smaptrack_block {
	char lines[16][512];
};

int Smaptrack_ReadLine_CopyToBuffer(int fd, char *buffer) {
	char local_buffer[1024];
	char c = '\r';
	int i = 0, k = 0;
	while ((k = read(fd, &c, 1) > 0)) {
		printf("%c\n",c);
		if (c == '\r' || c == '\n') {
			break;
		} else {
			local_buffer[i++] = c;
		}
	}

	if (!k)
		return 0;

	strcpy(buffer, local_buffer);

	printf("Buffer: %s : return 1\n",buffer);
	return 1;
}

int main(int argc, char *argv[]) {

	if (argc != 3) {
		printf("Usage : %s [# pid] [# duration(sec)]\n", argv[0]);
		exit(0);
	}

	int sleep_duration = atoi(argv[2]);

	char dirloc[__SMAPTRACK_DIR_BUFFER_SIZE__ ];

	struct smaptrack_block smapsb[__SMAPTRACK_DATA_COUNT__ ];

	memset(dirloc, 0x0, __SMAPTRACK_DIR_BUFFER_SIZE__);

	sprintf(dirloc, "/proc/%d/smaps", atoi(argv[1]));

	int fd = open(dirloc, O_RDONLY);

	while (1) {

		int count = 0;
		int i=0;
		int fin_flag = 0;
		while (1) {

			switch (i) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			default:
				if(Smaptrack_ReadLine_CopyToBuffer(fd, smapsb[count].lines[i++])==0){
					fin_flag ++;
				}
			}

			if(fin_flag){
				break;
			}
			if (i == __SMAPTRACK_DATA_COUNT__){
				count++;
				i=0;
			}
		}
		//comparison here...
		int j=0;
		for(i=0;i<count;i++)
		{
			for(j=0;j<count;j++)
			{
				printf("%s\n",smapsb[i].lines[j]);
			}

		}


		sleep(sleep_duration);
	}

}
