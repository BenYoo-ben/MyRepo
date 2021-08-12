#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

//predefined buffersize, if your iptable list is super huge, you might want to make it bigger.
#define __BUF_SIZE__ 32768
#define __LINE_SIZE__ 1024

int main(int argc, char *argv[]) {
	//predefined sleep duration, adjust it using 1st argument
	int sleep_sec = 3;
	//predefined location of iptables, pass 2nd argument if iptables is not located in default directory.
	char *iptables_loc = "/sbin/iptables";

	// change arguments..
	if (argc >= 2) {
		sleep_sec = atoi(argv[1]);
		iptables_loc = argv[2];
	} else {
		printf(
				"using iptables in /sbin/iptables...\nspecify other location if not working...\n%s [sleep time/sec] [iptables location, ex \"/sbin/iptables\"]\n\n",
				argv[0]);
	}

	//defining buffers...
	char buffer[__BUF_SIZE__ ];
	char prev_buffer[__BUF_SIZE__ ];

	//clearing dummy data with 0.

	memset(buffer,0x0,__BUF_SIZE__);
	memset(prev_buffer,0x0,__BUf_SIZE__);


	//len variables, and flag
	short prev_len = -1;
	short len = -1;
	//flag is more like count in this code.
	int flag = 0;

	while (1) {
		//pipe vars
		int pipe_link[2];

		//pid to be used with fork..
		pid_t pid;

		//pipe check.
		if (pipe(pipe_link) == -1) {
			perror("pipe failed.\n");
			exit(1);
		}

		//fork check.
		if ((pid = fork()) == -1) {
			perror("fork failed");
			exit(2);
		}

		if (pid == 0) {
			//child process

			//copy STDOUT --> pipe_link[1]
			dup2(pipe_link[1], STDOUT_FILENO);
			close(pipe_link[0]);
			close(pipe_link[1]);

			execl(iptables_loc, "iptables", "-L", "-nv", (char*) 0);

			exit(0);
		} else {

			//parent

			//close pipe.
			close(pipe_link[1]);

			//read from redirected stdout --> pipe[0].
			int len = read(pipe_link[0], buffer, sizeof(buffer));

			close(pipe_link[0]);

			char line[__LINE_SIZE__ ];

			short longer_line = -1;

			//? statement, comparing now to prev buffer to see which buffer is longer.
			(len > prev_len) ? (longer_line = len) : (longer_line = prev_len);

			printf(
					"\n\n%d th check... Displaying changes in %d sec...   read : %d\n",
					flag, sleep_sec, len);

			if (flag) {

				int i;
				for (i = 0; i < longer_line; i++) {

					if (buffer[i] != prev_buffer[i]) {
						int now = i;
						int start, end;
						while (i >= 0 && buffer[i] != '\n') {
							i--;
						}

						i++;
						start = i;
						i = now;

						while (buffer[i] < longer_line && buffer[i] != '\n') {
							i++;
						}

						end = i;
						memset(line, 0x0, __LINE_SIZE__);
						memcpy(line, buffer + start, end - start + 1);

						line[end - start + 1] = '\0';

						printf("%s\n", line);

					}

				}

			}

			//clear and copy data.
			memset(prev_buffer, 0x0, prev_len);
			memcpy(prev_buffer, buffer, len);
			memset(buffer, 0x0, len);
			prev_len = len;
			flag++;

			wait(NULL);
			sleep(sleep_sec);
		}

	}
	return 0;
}
