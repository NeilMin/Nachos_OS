/*
 * the data read is actually the data stored in the file; the number of bytes read may be less than count.
 * the data written is actually stored in the file; the number of bytes written matches count.
 */
#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"
int
main (int argc, char *argv[])
{
	//create a file
    char *testnoexistss = "testnoexistss.txt";
    int r = creat(testnoexistss);
    if (r == -1) {
	    printf ("...failed to open existing file(r = %d)\n", r);
	    exit (-1);
	}
	//write to file 
	char *stringToWrite = "easy peasy lemen squeasy a\nsad sad\n";
	int buflen = strlen (stringToWrite);
	int byteswrite = write(r, stringToWrite, buflen);

	//read the file count
	char* stringread = "";
	int bytesread = read(r, stringread, buflen);
	if(byteswrite != bytesread){
		printf ("...failed to write and read same amount in file(write, read = %d %d)\n", byteswrite, bytesread);
		exit (-1);
	}
	// printf("stringreaded %s ",stringread);
	//how to test if stringread is the same with string write
	// printf ("...success to write and read same amount in file(write, read = %d %d)\n", byteswrite, bytesread);

	int closeResult = close(r);
	if(closeResult != 0){
		printf ("...failed cannot close file (write = %d)\n", closeResult);
		exit (-1);
	}
	int closedWrite = write(r, stringToWrite, buflen);
	if(closedWrite != -1){
		printf ("...failed can write to a closed file (write = %d)\n", closedWrite);
		exit (-1);
	}
	char* closestringread = "";
	int closebytesread = read(r, closestringread, buflen);
	if(closebytesread != -1){
		printf ("...failed can read to a closed file (write = %d)\n", closebytesread);
		exit (-1);
	}
    return 0;
}