#include <stdio.h>
#include <stdint.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>

#define __MINI_DIG_DNS_PORT__ 53
#define __MINI_DIG_STORE_LOCATION__ "/var/urlfilter/"

char mini_dig_ips[100][16];
int mini_dig_query_end_idx = -1;



int MiniDigSendQuery(char *s, char *dns_server_addr){
	
	int sock;
	sock = socket(PF_INET, SOCK_DGRAM, 0);
	if(sock < 0){
		perror("sock init failed...\n");
		exit(1);
	}

	short addr_len = sizeof(struct sockaddr_in);

	struct sockaddr_in dns_addr;
	memset(&dns_addr, 0, addr_len);
	dns_addr.sin_family = AF_INET;
    dns_addr.sin_addr.s_addr = inet_addr(dns_server_addr);
    dns_addr.sin_port = htons(__MINI_DIG_DNS_PORT__ );


	uint16_t dns_query_header=0;
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

	dns_query_header += (opcode << p);

	char outgo_buffer[100];

	
	int transaction_id = 0x1234;

	//build outgo buffer

	// 256 =  ( 1 << 8 )	
	outgo_buffer[0] = transaction_id / 256;
	outgo_buffer[1] = transaction_id % 256;

	outgo_buffer[2] = dns_query_header / 256;
	outgo_buffer[3] = dns_query_header % 256;

	//QDCOUNT = 1;
	outgo_buffer[4] = 0; outgo_buffer[5] =1;
	//ANCOUNT = 0
	outgo_buffer[6] =0; outgo_buffer[7] = 0;

	//NSCOUNT = 0;
	outgo_buffer[8] =0; outgo_buffer[9] = 0;

	//ARCOUNT = 0;
	outgo_buffer[10] = 0; outgo_buffer[11] = 0;

	int outgo_write_idx = 12;
	int i=0,j=0;
	char tmp_buf[99] = {0, };
	printf(":%s:\n",s);
	while(1){
		if(s[i]=='.' || s[i]=='\0'){
			tmp_buf[j]=0;
			printf("COPYING : %s :\n",tmp_buf);
			outgo_buffer[outgo_write_idx] = j;
			outgo_write_idx++;
			int orig_j = j;

			j--;
			for(;j>=0;j--)
			{
				printf("::%c::\n",tmp_buf[j]);
				outgo_buffer[outgo_write_idx+j] = tmp_buf[j];
				printf("%d-%c wrote\n",j,outgo_buffer[outgo_write_idx+j]);
			}
			outgo_write_idx+=orig_j;

			if(s[i]=='\0')
				break;

			j=0;
			i++;


		}
		else{
			printf("i :%d, j :%d\n",i,j);
			tmp_buf[j] = s[i];
			printf("copy : %d-%c\n",j,tmp_buf[j]);
			i++;
			j++;
		}


	}
	i=outgo_write_idx;
    //end of name
	outgo_buffer[i] = 0; i++;
    
    //qtype
    outgo_buffer[i] = 0; i++;
    
    //any
    //outgo_buffer[i] = 255; i++;
	
    //ipv4 only
    outgo_buffer[i] = 1; i++;

    //host address
    //outgo_buffer[i] =13; i++;
    
    //qclass
    outgo_buffer[i] = 0; i++;
	outgo_buffer[i] = 1; i++;

	for(i=0;i<outgo_write_idx;i++)
		printf("%c ",outgo_buffer[i]);


	mini_dig_query_end_idx = outgo_write_idx+5;
    if((sendto(sock, outgo_buffer,mini_dig_query_end_idx, 0, (struct sockaddr *)&dns_addr,addr_len )) < 0) {
        perror("sendto fail");
        exit(0);
    	}

	return sock;
		
}

int MiniDigGetIPList(char *s, char *dns_server_addr){

	int sock = MiniDigSendQuery(s,dns_server_addr);

	char recv_buf[1000];
	int read_bytes = read(sock,recv_buf, 1000);
	recv_buf[read_bytes] = '\0';

	printf("READ : [%s]\n",recv_buf);
    //parse answers...
	uint16_t answer_count =  (256 * recv_buf[6]) + recv_buf[7];
    printf("# of answers : %d\n",answer_count);
    
    int found_ip_count = 0;

    uint8_t c = 0;
    int i = mini_dig_query_end_idx;
    while(found_ip_count < answer_count){
    
        c = recv_buf[i];
        if( (c & (0xc0)) == (0xc0)){
            printf("Q found : %d\n",found_ip_count);
            i += 12;
            
            c = recv_buf[i];
            sprintf(mini_dig_ips[found_ip_count],"%u.",c); i++; c = recv_buf[i];
            sprintf(mini_dig_ips[found_ip_count]+strlen(mini_dig_ips[found_ip_count]),"%u.",c); i++; c = recv_buf[i];
            sprintf(mini_dig_ips[found_ip_count]+strlen(mini_dig_ips[found_ip_count]),"%u.",c); i++; c = recv_buf[i];
            sprintf(mini_dig_ips[found_ip_count]+strlen(mini_dig_ips[found_ip_count]),"%u",c); i++; c = recv_buf[i];
            sprintf(mini_dig_ips[found_ip_count]+strlen(mini_dig_ips[found_ip_count]),"%c",'\0'); c = recv_buf[i];
            printf("DNS IP : %s\n",mini_dig_ips[found_ip_count]);

            found_ip_count ++;
        }
        else{
            i++;
        }
    }

    return found_ip_count;
}

void MiniDigIptablesAdd(char *string ,int count){

    int i=0;
    char buffer[100];
   
    memset(buffer,0x0,100);
    sprintf(buffer,"%s%s",__MINI_DIG_STORE_LOCATION__,string);
    int fd = open(buffer,O_RDWR);
    
    while(i<count){
        memset(buffer,0x0,100);
        sprintf(buffer,"iptables -I INPUT 1 -s %s/32 -j DROP",mini_dig_ips[i]);
        memset(buffer,0x0,100);
        sprintf(buffer,"%s#",mini_dig_ips[i]);
        write(fd,mini_dig_ips[i],16);
        system(buffer);
        i++;
    }
    
    close(fd);
    system("iptables-save");

}

void MiniDigIptablesRemove(char *string, int count){
   

   char buffer[100];
   memset(buffer,0x0,100);
   sprintf(buffer,"%s%s",__MINI_DIG_STORE_LOCATION__,string);
   int fd = open(buffer,O_RDONLY);
   if(
}

int main(int argc, char *argv[]){

	
    MiniDigIptablesAdd(MiniDigGetIPList("naver.com","192.168.254.1"));


}
