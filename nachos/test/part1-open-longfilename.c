#include "syscall.h"

int
main (int argc, char *argv[])
{
    char *longfilename = "xZPBfP8oadsfdfsdPBnuavbiLQGcLfQywbvtubMZeQMsefmNi8nTor3Jpa0PzPgQPUXfA1GY3A0rWvf0eSfTEyzfcBZSPU0XquiwqmqOe1qU3U2IbujgFw4uAME94XFj57nnES6mrad8Ou7n2JoNrGdwadn302Se85xalZ6pYxM1p3llSiQfmQE1H89cDgRdHoLoAKPmjLrA8p5JkT7h1ZjfJsC8ZzVrmKL1XXlgRrgRlJ4FYpQnrhuNZXACBeaog88y7NBI";
    
	int r = open (longfilename);
	if (r != -1) {
	    printf ("failed to checked if a string is longer than 256 bytes");
	    exit (-1);
	}
	
    return 0;
}