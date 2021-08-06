#include <stdio.h>
#include <sys/socket.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <stdlib.h>


int main(int argc, char *argv[]) {

	int BUFFS =1024;
	if (argc != 4 && argc != 5 && argc !=6) {
		printf("Usage: %s [MODE 0=S] [FILENAME] [# Port] [# BUFF_SIZE Default = 1024] [server_addr, only if in mode 1] \n",
				argv[0]);
		exit(1);
	}


	BUFFS = atoi(argv[4]);
	printf("BUFFER set to %d\n",BUFFS);


	int mode = atoi(argv[1]);

	if (mode == 0) {
		printf("Server start ... \n");

		int fd = socket(AF_INET, SOCK_STREAM, 0);
		if (fd < 0)
			perror("sock fail\n");

		struct sockaddr_in caddr;
		struct sockaddr_in saddr;

		memset(&caddr, 0x0, sizeof(caddr));
		memset(&saddr, 0x0, sizeof(saddr));

		socklen_t clen = sizeof(caddr);

		saddr.sin_family = AF_INET;
		saddr.sin_addr.s_addr = htonl(INADDR_ANY);
		saddr.sin_port = htons(atoi(argv[3]));

		if (bind(fd, (struct sockaddr*) &saddr, sizeof(saddr)) < 0)
			perror("bind err\n");

		if (listen(fd, 1) == -1)
			perror("liten err\n");

		int cfd = accept(fd, (struct sockaddr*) &caddr, &clen);
		if (cfd < 0)
			perror("accept err\n");

		printf("accept.\n");

		FILE *fp = fopen(argv[2], "wb");

		char buffer[BUFFS];
		ssize_t size_read = -1;
		int written = 0;

		short write_flag = 0;

		while (!write_flag) {
			size_read = read(cfd, buffer, (size_t) sizeof(buffer));

			if (size_read > 0) {
				fwrite(buffer, size_read, 1, fp);
				written += size_read;
			} else if (size_read <= 0) {

				write_flag = 1;
				perror("read interrupt\n");
			}

			printf("%d received\n", written);
			size_read = -1;
			memset(buffer, 0x0, sizeof(buffer));
		}
		fclose(fp);
		close(cfd);
		close(fd);
		exit(0);
	} else if (mode == 1) {
		printf("Client start ... \n");

		int fd = socket(AF_INET, SOCK_STREAM, 0);
		if (fd < 0)
			perror("sock fail\n");

		struct sockaddr_in saddr;

		memset(&saddr, 0x0, sizeof(saddr));

		saddr.sin_family = AF_INET;
		saddr.sin_addr.s_addr = inet_addr(argv[5]);
		saddr.sin_port = htons(atoi(argv[3]));

		if (-1 == connect(fd, (struct sockaddr*) &saddr, sizeof(saddr))) {
			perror("conn fail\n");
			exit(1);
		}

		printf("connect.\n");

		FILE *fp = fopen(argv[2], "rb");
		if (fp == NULL)
			perror("no file\n");

		char buffer[BUFFS];
		int f_read = 0;
		int so_far_read = 0;
		short read_flag =  0;
		while (!read_flag) {

			f_read = fread(buffer, 1, BUFFS, fp);

			if (f_read > 0) {
				if (f_read != write(fd, buffer, f_read)) {
					perror("read=>write sync fail.\n");
					exit(1);
				} else {
					so_far_read += f_read;
				}
			}
			else
			{
				read_flag=1;
			}

			printf("%d sent\n", so_far_read);
			f_read = -1;
			memset(buffer, 0x0, sizeof(buffer));

		}
		fclose(fp);
		close(fd);
		exit(0);
	}
}
