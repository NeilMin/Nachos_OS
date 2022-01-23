/*
 * open if exists return file descriptor 
 * if does not exist return -1
 * open same file multiple times return different file descriptor
 */
#include "syscall.h"

int
main (int argc, char *argv[])
{
    char *filename = "testnotexist.txt";
    char *existfile = "test.txt";
    int r = open (existfile);
    if (r == -1) {
	    printf ("...failed to open existing file(r = %d)\n", r);
	    exit (-1);
	}
    int r1 = open (existfile);
    if (r == r1 || r1 == -1) {
	    printf ("...failed should have different file descriptors(r, r1 = %d %d)\n", r, r1);
	    exit (-1);
	}
    // printf ("...success should have different file descriptors(r, r1 = %d %d)\n", r, r1);

	int r2 = open (filename);
	if (r2 != -1) {
	    printf ("...failed should not open non exisiting file(r = %d)\n", r);
	    exit (-1);
	}
    return 0;
}