#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>

int do_sum(const char *path) {
    sleep(1); // emulating an adittional delay of 1 second
	      //
    FILE *file = fopen(path, "rb");  // open in binary mode
    if (file == NULL) {
        return -1;  // indicate error
    }

    int sum = 0;
    int byte;

    while ((byte = fgetc(file)) != EOF) {
        sum += byte;
    }

    fclose(file);
    return sum;
}

void* do_sum_thread(void* arg) {
    const char* path = (char*) arg;
    int sum = do_sum(path);

    if (sum >= 0) {
        printf("%s : %d\n", path, sum);
    }

    return NULL;
}

int main(int argc, char *argv[]) {
    pthread_t threads[argc-1];

    for (int i = 1; i < argc; i++) {
        const char *path = argv[i];
        pthread_create(&threads[i-1], NULL, do_sum_thread, (void*)path);
    }

    for (int i = 1; i < argc; i++) {
        pthread_join(threads[i-1], NULL);
    }

    return 0;
}
