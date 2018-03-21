#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include <stdio.h>
#include <fcntl.h>

#include "queue.h"
#define ut_snprintf snprintf
#define ut_free free
#define ut_calloc calloc
#define JSUCCESS 0
#define JFAIL -1

int fifo_queue::init(unsigned char *arg_name, int shm_len, int type){
	unsigned char *shmem = (unsigned char *)this;
	int i;

	if (type == QUEUE_PRODUCER){
		producer.shm_ptr = shmem;
	}else{
		consumer.shm_ptr = shmem;
	}
	if (strcmp((const char *)name, (const char *)arg_name) ==0){
		printf(" QUEUE is already initialsed shmem :%p len:%d\n",shmem,queue_total_len);
		return queue_total_len;
	}
	memcpy(name,arg_name,strlen((const char *)arg_name));
	buf_start_index = sizeof (fifo_queue) + BUF_SIZE ;
	buf_start_index = (buf_start_index/4096) * BUF_SIZE ;

	for (i=0; i< MAX_QUEUE_LENGTH ; i++){
		data[i].shm_offset = buf_start_index + (i*BUF_SIZE) ;
	}
	queue_total_len = buf_start_index + (BUF_SIZE*MAX_QUEUE_LENGTH) ;
	printf(" Total len of fifo queue with %d bufs : %d  shmem:%p \n",MAX_QUEUE_LENGTH,queue_total_len,shmem);
	return queue_total_len;
}

int fifo_queue::add_to_queue(unsigned char *buf, int len,int data_flags) {
	unsigned long flags;
	int ret = JFAIL;

	if (buf == 0 || len == 0)
		return ret;

	//ut_log("Recevied from  network and keeping in queue: len:%d  stat count:%d prod:%d cons:%d\n",len,stat_queue_len,queue.producer,queue.consumer);

	if ( data[producer.index].len== 0) {
		data[producer.index].flags = data_flags;
		/* copy the data */
		copy_to_shm(data[producer.index].shm_offset, buf, len);
		data[producer.index].len = len;  /* CAUTION: this should be done after copying */
		producer.index++;
		producer.count++;

		if (producer.index >= MAX_QUEUE_LENGTH){
			producer.index = 0;
		}
		ret = JSUCCESS;
		goto last;
	}
	error_full++;

last:
	if (ret == JFAIL){
		;
	}else{

	}
	return ret;
}

int fifo_queue::peep_from_queue(unsigned char **buf, int *len,int *wr_flags) {
	if (data[consumer.index].len != 0) {
		if (len){
			*len = data[consumer.index].len;
		}
		if (wr_flags){
			*wr_flags = data[consumer.index].flags;
		}
		return data[consumer.index].len;
	}
	return 0;
}
int fifo_queue::copy_to_shm(int shm_offset, unsigned char *buf, int len){
	unsigned char *p;

	if (len > BUF_SIZE) return JFAIL;
	p =  producer.shm_ptr + shm_offset;
	//printf(" copy to shm:  p:%p len:%d data:%s: \n",p,len,buf);
	memcpy(p,buf,len);
	return JSUCCESS;
}
int fifo_queue::copy_from_shm(int shm_offset, unsigned char *buf, int len){
	unsigned char *p;

	if (len > BUF_SIZE) return JFAIL;
	p =  consumer.shm_ptr + shm_offset;

	memcpy(buf, p,len);
	//printf(" copy from shm:  p:%p len:%d data:%s: \n",p,len,buf);
	return JSUCCESS;
}
int fifo_queue::remove_from_queue(unsigned char *buf, int *len,int *wr_flags) {
	unsigned long flags;
	int ret = JFAIL;
	while (ret == JFAIL) {
		if (data[consumer.index].len != 0) {
			copy_from_shm(data[consumer.index].shm_offset, buf, data[consumer.index].len);
			*len = data[consumer.index].len;
			*wr_flags = data[consumer.index].flags;

			data[consumer.index].len = 0;
			consumer.index++;
			consumer.count++;
			if (consumer.index >= MAX_QUEUE_LENGTH){
				consumer.index = 0;
			}
			ret = JSUCCESS;
		}

		if (ret == JFAIL){
			return ret;
		}
	}
	return ret;
}

int shm_queue::create( int is_server_arg){
	int len;
	int type1,type2;
	const int SIZE = 50*1024*1024;
	unsigned char *shm;

	int fd = shm_open("/shm-example", O_RDWR| O_CREAT, 0777);
	if (fd == -1){
		//shm_unlink ("/tmp/myregion");
		printf("Error in client opening the shared memory\n");
		return 0;
	}
	ftruncate(fd, SIZE);
	shm =(unsigned char *)  mmap(NULL, SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);

	in_queue = (fifo_queue *)shm;
	is_server= is_server_arg;
	if (is_server){
		type1=QUEUE_CONSUMER;
		type2=QUEUE_PRODUCER;
	}else{
		type1=QUEUE_PRODUCER;
		type2=QUEUE_CONSUMER;
	}
	len = in_queue->init((unsigned char*)"queue1",SIZE,type1);

	shm = shm + len;
	out_queue = (fifo_queue *)shm;
	len = out_queue->init((unsigned char*)"queue2",SIZE,type2);
}
int shm_queue::remove_from_queue(unsigned char *buf, int *len,int *wr_flags){
	if (is_server){
		return in_queue->remove_from_queue(buf, len, wr_flags);
	}else{
		return out_queue->remove_from_queue(buf, len, wr_flags);
	}

}
int shm_queue::add_to_queue(unsigned char *buf, int len, int flags){
	if (is_server){
		return out_queue->add_to_queue(buf, len, flags);
	}else{
		return in_queue->add_to_queue(buf, len, flags);
	}
}
int shm_queue::peep_from_queue(unsigned char **buf, int *len,int *wr_flags){
	if (is_server){
		return in_queue->peep_from_queue(buf, len, wr_flags);
	}else{
		return out_queue->peep_from_queue(buf, len, wr_flags);
	}
}

