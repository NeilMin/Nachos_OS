/*
 * returns success only if the file system successfully removes the file, and an error otherwise; validates the file name argument.
 */
#include "syscall.h"

int
main (int argc, char *argv[])
{
    //create a file
    char *testNoExistss = "testnoexistss.txt";
    char *fileNotCreated = "testnotcreated.txt";
    int r = creat(testNoExistss);
    if (r == -1) {
	    printf ("...failed to open existing file(r = %d)\n", r);
	    exit (-1);
	}

    //unlink
    int unlinkFileNotCreated = unlink(fileNotCreated);
    if(unlinkFileNotCreated != -1){
        printf ("...failed can unlink no existing file(unlinkFileNotCreated = %d)\n", unlinkFileNotCreated);
	    exit (-1);
    }
    int unlinkResults = unlink(testNoExistss);
    if(unlinkResults != 0){
        printf ("...failed to unlink existing file(testNoExistss = %d)\n", testNoExistss);
	    exit (-1);
    }

    return 0;
}