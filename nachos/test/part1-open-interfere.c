/*
 * open does not interfere with stdin and stdout
 */
#include "syscall.h"

int
main (int argc, char *argv[])
{
    char *existfile = "test.txt";
    int r = open (existfile);
    if (r == -1) {
	    printf ("...failed to open existing file(r = %d)\n", r);
	    exit (-1);
	}
    
    return 0;
}