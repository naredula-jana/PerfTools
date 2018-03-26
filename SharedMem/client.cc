#include <unistd.h>
#include <sys/mman.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <arpa/inet.h>
#include <time.h>

#include "queue.h"
shm_queue c_shm_queue;
#define MAX_REC 50000000
time_t ticks1, ticks2;
int client_socket() {
	int sockfd = 0, n = 0;
	int i, ret;
	unsigned char recvBuff[1024];
	struct sockaddr_in serv_addr;
	memset(recvBuff, '0', sizeof(recvBuff));
	if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		printf("\n Error : Could not create socket \n");
		return 1;
	}

	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);

	if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr) <= 0) {
		printf("\n inet_pton error occured\n");
		return 1;
	}

	if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr))
			< 0) {
		printf("\n Error : Connect Failed \n");
		return 1;
	}

	ticks1 = time(NULL);
	for (i = 0; i < MAX_REC; i++) {
		sprintf((char *)recvBuff,"    RECV :%d\n",i);
		write(sockfd, recvBuff, 100);
		ret = read(sockfd, recvBuff, 1025);
		//printf(" DATA  :%s:\n",recvBuff);
	}
	ticks2 = time(NULL);
	printf("socket  ticks :%ld ticks2:%ld count:%d\n", ticks1, ticks2,i);
	if (n < 0) {
		printf("\n Read error \n");
	}

	return 0;
}
int recv_pkts=0;
int recv_count=0;
int rlen=0;
void recv_func(){
	unsigned  char buf[1024];
	unsigned char *ret_buf =0;
	int i,flag,len;
	while(1)
	{
		if (c_shm_queue.peep_from_queue() == 0) {
			return;
		}
		recv_count++;
		//c_shm_queue.remove_from_queue(&buf[0], &len, &flag);
		ret_buf = c_shm_queue.remove_from_queue(0, &len, &flag);
		if (ret_buf!=0 && ret_buf[0] == '$') {
			c_shm_queue.put_freebuf(ret_buf);
			recv_pkts++;
			rlen=len;
		}
	}
}
int main() {
	unsigned char *shm;
	int i,k,flag,len;
	int count=0;
	int ret;
	int batch=10;

	//client_socket();
	//system("sleep 5");
	c_shm_queue.create(0);

	ticks1 = time(NULL);
	for (i=0; i<MAX_REC; i=i+batch){
		unsigned  char pkt[100];
		unsigned  char buf[3024];
		unsigned char *p;
		int plen;

		sprintf((char *)pkt,"*2\r\n$3\r\nGET\r\n$16\r\nkey:000000000001\r\n");
		p=&buf[0];
		len=0;
		plen=strlen((const char *)pkt);
		for (k=0; k<batch; k++){
			memcpy(p,pkt,plen);
			len=len+plen;
			p=p+plen;
		}
		//printf(" Sending Data  :%s: len:%d \n",buf,strlen((const char *)buf));
		ret=0;
		while(ret==0){
			ret=c_shm_queue.add_to_queue(&buf[0],len,0);
		}
		recv_func();
	}
	ticks2 = time(NULL);
	printf(" sharedmemoryv.1 ticks :%ld ticks2:%ld diff:%ld recv_count:%d recv_pkts:%d batch=%d rlen=%d\n", ticks1, ticks2,(ticks2-ticks1),recv_count,recv_pkts,batch,rlen);
}

