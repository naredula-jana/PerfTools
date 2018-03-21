typedef struct {
		unsigned int shm_offset; /* offset within  shared memory , and length is BUF_SIZE */
		unsigned int len; /* actual data length , not the buf lenegth, buf always constant length */
		int flags;
	} fifo_data_struct;

struct fifo_user{
		int index;
		unsigned long count;
		unsigned char *shm_ptr; /* this is virtual address of the process attched: only one preoducer and one cinsumer can use the shared memory currently */
	} __attribute__ ((aligned (128)));

#define MAX_FILENAME 100
#define MAX_QUEUE_LENGTH 10
#define BUF_SIZE 4096
#define QUEUE_PRODUCER 1
#define QUEUE_CONSUMER 2
class fifo_queue {
	unsigned char name[MAX_FILENAME];

	struct fifo_user producer;
	fifo_data_struct data[MAX_QUEUE_LENGTH];
	struct fifo_user consumer;

	unsigned long error_full;
	unsigned long error_empty_check;

	unsigned int queue_total_len;
    unsigned int buf_start_index; /* buf start from start of the shared memory */

    int copy_to_shm(int index, unsigned char *buf, int len);
    int copy_from_shm(int index, unsigned char *buf, int len);
public:
	int remove_from_queue(unsigned char *buf, int *len,int *wr_flags);
	int add_to_queue(unsigned char *buf, int len, int flags);
	int peep_from_queue(unsigned char **buf, int *len,int *wr_flags);
	int init(unsigned char *arg_name,int wq_enable, int type);

};
class shm_queue {
	fifo_queue *in_queue;
	fifo_queue *out_queue;
	int is_server;
public:
	int create(int type);
	int remove_from_queue(unsigned char *buf, int *len,int *wr_flags);
	int add_to_queue(unsigned char *buf, int len, int flags);
	int peep_from_queue(unsigned char **buf, int *len,int *wr_flags);
};
