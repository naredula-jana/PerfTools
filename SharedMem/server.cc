#include <unistd.h>
#include <sys/mman.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <time.h>

#include "queue.h"

int server_socket() {
	int listenfd = 0, connfd = 0;
	struct sockaddr_in serv_addr;
	int ret;

	char recvBuff[1025];
	time_t ticks1, ticks2;

	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	memset(&serv_addr, '0', sizeof(serv_addr));
	memset(recvBuff, '0', sizeof(recvBuff));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(5000);

	bind(listenfd, (struct sockaddr*) &serv_addr, sizeof(serv_addr));

	listen(listenfd, 10);

	connfd = accept(listenfd, (struct sockaddr*) NULL, NULL);

	ticks1 = time(NULL);
	while (1) {
		ret = read(connfd, recvBuff, 1025);
		recvBuff[0]='A';
		if (ret > 0) {
			write(connfd, recvBuff, ret);
		} else {
			break ;
		}
	}
	ticks2 = time(NULL);
	close(connfd);
	printf(" ticks :%ld ticks2:%ld\n", ticks1, ticks2);

	return 1;
}

extern "C" {
extern int shm_create(int is_server_arg);
extern int shm_add_to_queue(unsigned char *buf, int len, int flags);
extern int shm_remove_from_queue(unsigned char *buf, int *len,int *wr_flags);
extern int shm_peep_from_queue();
}

int main(){
	int i;
	unsigned char buf[4096];
	int len,flag;

	shm_create(1);
	while(1){
		if (shm_peep_from_queue() != 0){
			i++;
			shm_remove_from_queue(&buf[0],&len,&flag);
			//printf(" %d: USINGC the message  :%s : len:%d \n",i,buf,len);
			buf[0]='B';
			shm_add_to_queue(&buf[0],len,0);
		}
	}

}
