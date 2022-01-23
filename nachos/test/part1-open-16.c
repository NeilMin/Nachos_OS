/*
 * each process can use 16 file descriptors.
 */
#include "syscall.h"

int
main (int argc, char *argv[])
{
    char *existfile = "test.txt";
    int count = 0;
    for(count; count<14; count++){
        int r = open (existfile);
        if (r == -1) {
	        printf ("...failed to open existing file(r = %d)\n", r);
	        exit (-1);
	    }
        // printf("r is %d ",r);
    }
    int r = open (existfile);
    if (r != -1) {
	    printf ("...failed more than 16 file descriptors can be used(r = %d)\n", r);
	    exit (-1);
	}
    
    return 0;
}