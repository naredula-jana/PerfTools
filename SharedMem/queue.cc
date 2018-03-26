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
	int last_buf_index;

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
		last_buf_index = buf_start_index + (i*BUF_SIZE) ;
	}
	last_buf_index = last_buf_index + BUF_SIZE;
	for (i=0; i< MAX_ADDITIONAL_BUFS ; i++){
		free_buf_offset[i] = last_buf_index + (i*BUF_SIZE) ;
	}
	queue_total_len = buf_start_index + (BUF_SIZE*(MAX_QUEUE_LENGTH + MAX_ADDITIONAL_BUFS)) ;
	printf(" Total len of fifo queue with %d bufs : %d  shmem:%p \n",MAX_QUEUE_LENGTH,queue_total_len,shmem);
	return queue_total_len;
}

int fifo_queue::add_to_queue(unsigned char *buf, int len,int data_flags) {
	unsigned long flags;
	int ret = 0;

	if (buf == 0 || len == 0)
		return ret;

	//ut_log("Recevied from  network and keeping in queue: len:%d  stat count:%d prod:%d cons:%d\n",len,stat_queue_len,queue.producer,queue.consumer);

	if ( data[producer.index].len== 0) {
		data[producer.index].flags = data_flags;
		/* copy the data */
		ret = copy_to_shm(data[producer.index].shm_offset, buf, len);
		data[producer.index].len = len;  /* CAUTION: this should be done after copying */
		producer.index++;
		producer.count++;

		if (producer.index >= MAX_QUEUE_LENGTH){
			producer.index = 0;
		}
		goto last;
	}
	error_full++;

last:
	return ret;
}

int fifo_queue::peep_from_queue() {

	return data[consumer.index].len;
}
int fifo_queue::copy_to_shm(int shm_offset, unsigned char *buf, int len){
	unsigned char *p;

	if (len > BUF_SIZE) return -1;
	p =  producer.shm_ptr + shm_offset;
	//printf(" copy to shm:  p:%p len:%d data:%s: \n",p,len,buf);
	memcpy(p,buf,len);
	return len;
}
int fifo_queue::copy_from_shm(int shm_offset, unsigned char *buf, int len){
	unsigned char *p;

	if (len > BUF_SIZE) return JFAIL;
	p =  consumer.shm_ptr + shm_offset;

	memcpy(buf, p,len);
	//printf(" copy from shm:  p:%p len:%d data:%s: \n",p,len,buf);
	return JSUCCESS;
}
int fifo_queue::get_freebuf(){
	int i;
	int ret =-1;

	for (i=0; i< MAX_ADDITIONAL_BUFS ; i++){
		if (free_buf_offset[i] != -1){
			ret = free_buf_offset[i];
			free_buf_offset[i] = -1;
			return ret;
		}
	}
	return ret;
}
int fifo_queue::put_freebuf(unsigned char *p){
	int i;

	for (i=0; i< MAX_ADDITIONAL_BUFS ; i++){
		if (free_buf_offset[i] == -1){
			free_buf_offset[i] = ( p - consumer.shm_ptr);
			return 1;
		}
	}
	printf(" BUG: Not able to put free buf: %d\n",i);
	while(1);
	return 0;
}
unsigned char *fifo_queue::remove_from_queue(unsigned char *buf, int *len, int *wr_flags) {
	unsigned long flags;
	unsigned char *ret = buf;

	if (data[consumer.index].len != 0) {
		if (buf==0){
			ret =  consumer.shm_ptr + data[consumer.index].shm_offset;
			data[consumer.index].shm_offset = get_freebuf();
			if (data[consumer.index].shm_offset  == -1){
				printf(" BUG: OUT OF BUFFERS in remove\n");
				while(1);
			}
		}else{
			copy_from_shm(data[consumer.index].shm_offset, buf,
				data[consumer.index].len);
		}
		*len = data[consumer.index].len;
		if (wr_flags != 0) {
			*wr_flags = data[consumer.index].flags;
		}

		data[consumer.index].len = 0;
		consumer.index++;
		consumer.count++;
		if (consumer.index >= MAX_QUEUE_LENGTH) {
			consumer.index = 0;
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
unsigned char  *shm_queue::remove_from_queue(unsigned char *buf, int *len,int *wr_flags){
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
int shm_queue::peep_from_queue(){
	if (is_server){
		return in_queue->peep_from_queue();
	}else{
		return out_queue->peep_from_queue();
	}
}
int shm_queue::put_freebuf(unsigned char *p){
	if (is_server){
		return in_queue->put_freebuf(p);
	}else{
		return out_queue->put_freebuf(p);
	}
}

extern "C" {
shm_queue shm_queue;

int shm_create(int is_server_arg){
    return shm_queue.create( is_server_arg);
}
int shm_add_to_queue(unsigned char *buf, int len, int flags){
	return shm_queue.add_to_queue(buf, len, flags);
}
unsigned char  *shm_remove_from_queue(unsigned char *buf, int *len,int *wr_flags){
	return shm_queue.remove_from_queue(buf, len,wr_flags);
}
int shm_peep_from_queue(){
	return shm_queue.peep_from_queue();
}
int shm_put_freebuf(unsigned char *p){
	return shm_queue.put_freebuf(p);
}
}

