
#include "server.h"
#include "redismodule.h"

extern int shm_create(int is_server_arg);
extern int shm_add_to_queue(unsigned char *buf, int len, int flags);
extern int shm_remove_from_queue(unsigned char *buf, int *len, int *wr_flags);
extern int shm_peep_from_queue(unsigned char **buf, int *len, int *wr_flags);
client *client_obj;
extern void (*ModuleSHM_BeforeSelect)();
void ModuleSHM_BeforeSelect_Impl() {

}

extern void (*ModuleSHM_AfterSelect)();
void ModuleSHM_AfterSelect_Impl() {

}
extern ssize_t (*ModuleSHM_ReadUnusual)(int fd, void *buf, size_t count);
ssize_t ModuleSHM_ReadUnusual_Impl(int fd, void *buf, size_t count) {
	errno = 0;
	int len = count;

//printf("Reading the SHM...: count:%d \n");
	while (shm_peep_from_queue(0, 0, 0) == 0)
		;
	shm_remove_from_queue(buf, &len, 0);
	// printf("Read the data :%s:  readlen:%d\n",buf,len);

	if (len == 0) {
		errno = EAGAIN;
		return -1;
	}
	return len;
}

extern ssize_t (*ModuleSHM_WriteUnusual)(int fd, const void *buf, size_t count);
ssize_t ModuleSHM_WriteUnusual_Impl(int fd, const void *buf, size_t count) {
	errno = 0;

	// printf("Writing the SHM..: %s: \n",buf);
	int nwritten = shm_add_to_queue(buf, count, 0);

	return nwritten;
}

/* The thread entry point that actually executes the blocking part
 *  */
void *RedisMod_ThreadMain(void *arg) {
	int i;
	unsigned char buf[4096];
	int len, flag;

	while (1) {
		if (shm_peep_from_queue(0, 0, 0) != 0) {
			i++;

			readQueryFromClient(0, -1, client_obj, AE_READABLE);
			//shm_add_to_queue(&buf[0],len,0);
			if (clientHasPendingReplies(client_obj)) {
				sendReplyToClient(0, -1, client_obj, AE_WRITABLE);
			}
		}
	}

	return NULL;
}

#define REDISMODULE_APIVER_1 1

/* Registering the module */
int RedisModule_OnLoad(void *ctx) {
	pthread_t tid;
	if (RedisModule_Init(ctx, "SHM", 1, REDISMODULE_APIVER_1) == 1) {
		return 1;
	}
	ModuleSHM_BeforeSelect = ModuleSHM_BeforeSelect_Impl;
	ModuleSHM_AfterSelect = ModuleSHM_AfterSelect_Impl;
	ModuleSHM_ReadUnusual = ModuleSHM_ReadUnusual_Impl;
	ModuleSHM_WriteUnusual = ModuleSHM_WriteUnusual_Impl;

	shm_create(1);

	/* Create a client for replaying the input to */
	client_obj = createClient(-1);
	client_obj->flags |= CLIENT_MODULE;

	if (pthread_create(&tid, NULL, RedisMod_ThreadMain, 0) != 0) {
		return 1;
	}

	return 0;
}
