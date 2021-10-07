#include <stdio.h>
#include <stdint.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>

#define __MINI_DIG_DNS_PORT__ 53
#define __MINI_DIG_STORE_LOCATION__ "/var/urlfilter/"
#define __MINI_DIG_RECV_TIMEOUT__ 5

char mini_dig_ips[100][16];
int mini_dig_query_end_idx = -1;

int MiniDigSendQuery(char *s, char *dns_server_addr) {

	int sock;
	sock = socket(PF_INET, SOCK_DGRAM, 0);
	if (sock < 0) {
		perror("sock init failed...\n");
		exit(1);
	}

	short addr_len = sizeof(struct sockaddr_in);

	struct sockaddr_in dns_addr;
	memset(&dns_addr, 0, addr_len);
	dns_addr.sin_family = AF_INET;
	dns_addr.sin_addr.s_addr = inet_addr(dns_server_addr);
	if(dns_addr.sin_addr.s_addr == INADDR_NONE ){
		printf("Input DNS server address is invalid! INPUT=[%s]\n",dns_server_addr);
		return -1;
	}

	dns_addr.sin_port = htons(__MINI_DIG_DNS_PORT__);

	uint16_t dns_query_header = 0;
	int p = 0;

	char rcode = 0b0000;
	char z = 0b000;
	char ra = 0b0;
	char rd = 0b1;
	char tc = 0b0;
	char aa = 0b0;
	char opcode = 0b0000;
	char qr = 0b0;

	dns_query_header += (rcode << p);
	p += 4;

	dns_query_header += (z << p);
	p += 3;

	dns_query_header += (ra << p);
	p += 1;

	dns_query_header += (rd << p);
	p += 1;

	dns_query_header += (tc << p);
	p += 1;

	dns_query_header += (aa << p);
	p += 1;

	dns_query_header += (opcode << p);
	p += 4;

	dns_query_header += (qr << p);

	char outgo_buffer[100];

	int transaction_id = 0x1234;

	//build outgo buffer

	// 256 =  ( 1 << 8 )	
	outgo_buffer[0] = transaction_id / 256;
	outgo_buffer[1] = transaction_id % 256;

	outgo_buffer[2] = dns_query_header / 256;
	outgo_buffer[3] = dns_query_header % 256;

	//QDCOUNT = 1;
	outgo_buffer[4] = 0;
	outgo_buffer[5] = 1;
	//ANCOUNT = 0
	outgo_buffer[6] = 0;
	outgo_buffer[7] = 0;

	//NSCOUNT = 0;
	outgo_buffer[8] = 0;
	outgo_buffer[9] = 0;

	//ARCOUNT = 0;
	outgo_buffer[10] = 0;
	outgo_buffer[11] = 0;

	int outgo_write_idx = 12;
	int i = 0, j = 0;
	char tmp_buf[99] = { 0, };
	while (1) {
		if (s[i] == '.' || s[i] == '\0') {
			tmp_buf[j] = 0;
			outgo_buffer[outgo_write_idx] = j;
			outgo_write_idx++;
			int orig_j = j;

			j--;
			for (; j >= 0; j--) {
				outgo_buffer[outgo_write_idx + j] = tmp_buf[j];
			}
			outgo_write_idx += orig_j;

			if (s[i] == '\0')
				break;

			j = 0;
			i++;

		} else {
			tmp_buf[j] = s[i];
			i++;
			j++;
		}

	}
	i = outgo_write_idx;
	//end of name
	outgo_buffer[i] = 0;
	i++;

	//qtype
	outgo_buffer[i] = 0;
	i++;

	//any
	//outgo_buffer[i] = 255; i++;

	//ipv4 only
	outgo_buffer[i] = 1;
	i++;

	//host address
	//outgo_buffer[i] =13; i++;

	//qclass
	outgo_buffer[i] = 0;
	i++;
	outgo_buffer[i] = 1;
	i++;

	mini_dig_query_end_idx = outgo_write_idx + 5;
	if ((sendto(sock, outgo_buffer, mini_dig_query_end_idx, 0,
			(struct sockaddr*) &dns_addr, addr_len)) < 0) {
		perror("sendto fail");
		exit(0);
	}

	return sock;

}

int MiniDigGetIPList(char *s, char *dns_server_addr) {

	int sock = MiniDigSendQuery(s, dns_server_addr);

	//use select(2) to implement timeout.
	fd_set filedesc_set;
	FD_ZERO(&filedesc_set);
	FD_SET(sock, &filedesc_set);

	struct timeval timeout;
	timeout.tv_sec = __MINI_DIG_RECV_TIMEOUT__;
	timeout.tv_usec = 0;

	int rv = select(sock + 1, &filedesc_set, NULL, NULL, &timeout);
	if (rv == -1) //Boom. Error.
		return -1;
	if (rv == 0) {
		//DNS server timeout, wrong DNS address?
		printf("DNS server timeout\n");
		return -2;
	} else {
		char recv_buf[1024];
		int read_bytes = read(sock, recv_buf, 1024);
		recv_buf[read_bytes] = '\0';

		//parse answers...
		uint16_t answer_count = (256 * recv_buf[6]) + recv_buf[7];
		printf("# of answers : %d\n", answer_count);

		int found_ip_count = 0;

		uint8_t c = 0;
		int i = mini_dig_query_end_idx;
		while (found_ip_count < answer_count) {

			c = recv_buf[i];
			if ((c & (0xc0)) == (0xc0)) {
				i += 12;

				c = recv_buf[i];
				sprintf(mini_dig_ips[found_ip_count], "%u.", c);
				i++;
				c = recv_buf[i];
				sprintf(
						mini_dig_ips[found_ip_count]
								+ strlen(mini_dig_ips[found_ip_count]), "%u.",
						c);
				i++;
				c = recv_buf[i];
				sprintf(
						mini_dig_ips[found_ip_count]
								+ strlen(mini_dig_ips[found_ip_count]), "%u.",
						c);
				i++;
				c = recv_buf[i];
				sprintf(
						mini_dig_ips[found_ip_count]
								+ strlen(mini_dig_ips[found_ip_count]), "%u",
						c);
				i++;
				c = recv_buf[i];
				sprintf(
						mini_dig_ips[found_ip_count]
								+ strlen(mini_dig_ips[found_ip_count]), "%c",
						'\0');
				c = recv_buf[i];
				printf("DNS IP : %s\n", mini_dig_ips[found_ip_count]);

				found_ip_count++;
			} else {
				i++;
			}
		}
		return found_ip_count;
	}
}

int MiniDigIptablesAdd(char *string, int count) {

	int i = 0;
	char buffer[100];

	memset(buffer, 0x0, 100);
	sprintf(buffer, "%s%s", __MINI_DIG_STORE_LOCATION__, string);

	//check for directroy
	struct stat st = { 0 };

	if (stat(__MINI_DIG_STORE_LOCATION__, &st) == -1) {
		mkdir(__MINI_DIG_STORE_LOCATION__, 0700);
	}

	int fd = open(buffer, O_CREAT | O_RDWR);

	if (count < 0) {
		perror("DNS server error, not available ! \n");
		return -1;
	} else {
        //adding iptables rules for dropping packets.
		while (i < count) {

        //for local host(INPUT rules), deny all TCP
			memset(buffer, 0x0, 100);
			sprintf(buffer, "iptables -w -I INPUT 1 -p tcp -s %s/32 -j DROP",
				mini_dig_ips[i]);
			system(buffer);
			
            //DNS(port 53)
            memset(buffer, 0x0, 100);
            sprintf(buffer, "iptables -w -I INPUT 1 -p udp --dport %d -s %s/32 -j DROP",
               __MINI_DIG_DNS_PORT__,mini_dig_ips[i]);
            system(buffer);
            memset(buffer,0x0,100);
	
        //for other hosts(FORWARD rules), deny all TCP
            memset(buffer, 0x0, 100);
			sprintf(buffer, "iptables -w -I FORWARD 1 -p tcp -s %s/32 -j DROP",
				mini_dig_ips[i]);
			system(buffer);

            //DNS(port 53)
            memset(buffer, 0x0, 100);
			sprintf(buffer, "iptables -w -I FORWARD 1 -p udp --dport %d -s %s/32 -j DROP",
				__MINI_DIG_DNS_PORT__,mini_dig_ips[i]);
			system(buffer);

            sprintf(buffer, "%s%c", mini_dig_ips[i], '#');
			write(fd, buffer, strlen(buffer));

			i++;
		}
	}
	close(fd);
	return 0;
}

int MiniDigIptablesRemove(char *string) {

	char buffer[200];
	memset(buffer, 0x0, 200);
	sprintf(buffer, "%s%s", __MINI_DIG_STORE_LOCATION__, string);
	int fd = open(buffer, O_RDONLY);
	int i = 0, j = 0;
	char ip[16];
	char cmd_buf[100];
	if (fd < 0) {
		printf("is valid file %s%s ? \n", __MINI_DIG_STORE_LOCATION__, string);
		exit(1);
	}

	int file_size = read(fd, buffer, 200);
	if (file_size > 15) {
		
            //Delete rules from written DROP rules in iptables(filter)

        while (1) {

			if (buffer[i] == '#') {
				ip[j] = '\0';
			//for local traffics(TCP, DNS)
                memset(cmd_buf, 0x0, 100);
				sprintf(cmd_buf, "iptables -w -D INPUT -p tcp -s %s/32 -j DROP", ip);
				system(cmd_buf);

                memset(cmd_buf,0x0,100);
                sprintf(cmd_buf, "iptables -w -D INPUT -p udp --dport %d -s %s/32 -j DROP",__MINI_DIG_DNS_PORT__,ip);
                system(cmd_buf); 
                
            //for other traffics(TCP, DNS)    
                memset(cmd_buf, 0x0, 100);
				sprintf(cmd_buf, "iptables -w -D FORWARD -p tcp -s %s/32 -j DROP", ip);
				system(cmd_buf);

                memset(cmd_buf, 0x0, 100);
				sprintf(cmd_buf, "iptables -w -D FORWARD -p udp --dport %d -s %s/32 -j DROP",__MINI_DIG_DNS_PORT__,ip);
				system(cmd_buf);

                j = 0;
				//EOF
				if (i >= (file_size - 1))
					break;
			} else {
				ip[j] = buffer[i];
				j++;
			}
			i++;
		}

		memset(buffer, 0x0, 200);
		sprintf(buffer, "%s%s", __MINI_DIG_STORE_LOCATION__, string);
		remove(buffer);
	} else {
		printf("is valid file %s%s ? \n", __MINI_DIG_STORE_LOCATION__, string);
		exit(1);
	}

	close(fd);
	return 0;
}

int main(int argc, char *argv[]) {

	if(0){
		wrong_arg_seq :
			printf("Usage : %s [Action: 'A' or 'D'] [example.com] [DNS IP, only in a mode] \n",argv[0]);
			return 1;
	}

	if(argc<2){
		goto wrong_arg_seq;

	}
	else if(argv[1][0]=='A'){
		if(argc!=4){
			goto wrong_arg_seq;
		}

		MiniDigIptablesAdd(argv[2],MiniDigGetIPList(argv[2],argv[3]));
		return 0;
	}
	else if(argv[1][0]=='D')
	{
		if(argc!=3){
			goto wrong_arg_seq;
		}

		MiniDigIptablesRemove(argv[2]);
		return 0;
	}else{
        goto wrong_arg_seq;
    }

}
