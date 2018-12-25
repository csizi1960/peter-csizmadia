/*
 * pcsums 1.2
 * - version 1.2 Fri Dec 23 2005 - large file support
 * - version 1.1 Fri Apr 20 2001
 * - version 1.0 Thu Nov 11 1999  Peter Csizmadia
 *
 * Calculates BSD, SysV and CRC-32 checksums.
 * Updates output line on each megabyte of input data.
 *
 * Compilation with GNU CC:
 * gcc -O3 -D_LARGEFILE64_SOURCE pcsums.c -o pcsums
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define UINT32 unsigned int
#define FILE_SEPARATOR '/'

#define BUFSIZE 512

#define update_bsd(c, bsd) \
    ((((bsd & 1)? ((bsd >> 1) | 0x8000) : (bsd >> 1)) + c) & 0xffff)

#define update_crc32(c, crc) ((crc >> 8) ^ crc32tab[(c ^ (int)crc) & 0xff])

static UINT32 crc32tab[256];

static void make_table_crc32(void)
{
    int i;
    for (i=0; i<256; ++i) {
	int j;
	UINT32 entry32 = i;
	for (j = 0; j<8; ++j) {
	    int carry32 = entry32 & 1;
	    entry32 >>= 1;
	    if (carry32)
		entry32 ^= 0xedb88320;
	}
	crc32tab[i] = entry32;
    }
}

static void rdbuf(char* buf, int len, off64_t* ptroff,
		  int* bsdsum, int* sysvsum, UINT32* crc32sum,
		  const char* file, off64_t endPosition)
{
    int i;
    int bsd = *bsdsum;
    int sysv = *sysvsum;
    off64_t off = *ptroff;
    UINT32 crc32 = *crc32sum;
    if(endPosition != -1 && len > endPosition - off) {
	len = endPosition - off;
    }
    for(i = 0; i < len; ++i) {
	int c = (int)((unsigned char)buf[i]);
	if((off & 0xfffff) == 0) {
	    printf("\r%6ld %11ld %05d %5d %08lX %s",
		    (long)(off>>20), (long)(off>>10),
		   bsd, sysv, (long)(crc32 ^ (UINT32)0xffffffff), file);
	    fflush(stdout);
	}
	bsd = update_bsd(c, bsd);
	sysv += c;
	if(sysv & 0x10000)
	    sysv = (sysv + 1) & 0xffff;
	crc32 = update_crc32(c, crc32);
	++off;
    }
    *bsdsum = bsd;
    *sysvsum = sysv;
    *crc32sum = crc32;
    *ptroff = off;
}

static int sums(int fd, const char* file, off64_t endPosition)
{
    off64_t off = 0;
    int bsd = 0;
    int sysv = 0;
    UINT32 crc32 = (UINT32)0xffffffff;
    char buf[BUFSIZE];
    int len;
    int delta = BUFSIZE;
    while((len = read(fd, buf, delta)) > 0) {
	rdbuf(buf, len, &off, &bsd, &sysv, &crc32, file, endPosition);
	if(endPosition != -1 && off >= endPosition) {
	    break;
	}
	delta = BUFSIZE - off % BUFSIZE;
    }
    crc32 ^= (UINT32)0xffffffff;
    printf("\r%6.1f %11.1f %05d %5d %08lX %s\n",
	   off/1048576.0, off/1024.0, bsd, sysv, (long)crc32, file);
    return (len == -1)? -1 : 0;
}

static char* basename(char* path)
{
    char* p = path;
    char* p1 = path;
    char* p2 = path;
    while(*p != '\0') {
	int c = *p;
	++p;
	if(c == FILE_SEPARATOR) {
	    p1 = p2;
	    p2 = p;
	}
    }
    return (strlen(p2) > 0)? p2 : p1;
}

int main(int argc, char* argv[])
{
    const char* strhelp = "pcsums 1.2, Peter Csizmadia, 1999, 2001, 2005\n\n"
"Usage: pcsums [options] [file] [options] [file]...\n\n"
"Options:\n"
"-h --help  print this help message\n"
"-l #       set number of bytes to read from next file\n"
"           (#k = kilobytes, #M = megabytes)\n";
    int i;
    int nfiles = 0;
    char** filenames = (char**)malloc(argc*sizeof(char*));
    off64_t endPosition = -1;
    off64_t* endPositions = (off64_t*)malloc(argc*sizeof(off64_t));
    int errcode = 0;
    for(i = 1; i < argc; ++i) {
	char* s = argv[i];
	if(s[0] == '-' && s[1] != '\0') {
	    if(!strcmp(s, "--help")) {
		printf(strhelp);
		free(filenames);
		free(endPositions);
		return 0;
	    } else {
		int j;
		int l;
		off64_t x;
		char* tmp;
		for(j = 1; j < strlen(s); ++j) {
		    switch(s[j]) {
		    case 'h':
			printf(strhelp);
			free(filenames);
			free(endPositions);
			return 0;
		    case 'l':
			s = argv[++i];
			l = strlen(s);
			if(s[l-1] == 'k' || s[l-1] == 'M') {
			    tmp = (char*)malloc(l);
			    memcpy(tmp, s, l - 1);
			    tmp[l - 1] = '\0';
			} else {
			    tmp = (char*)malloc(l + 1);
			    memcpy(tmp, s, l);
			    tmp[l] = '\0';
			}
			x = atoll(tmp);
			free(tmp);
			if(s[l - 1] == 'k') {
			    x <<= 10;
			} else if(s[l - 1] == 'M') {
			    x <<= 20;
			}
			endPosition = x;
			break;
		    }
		}
	    }
	} else {
	    filenames[nfiles] = s;
	    endPositions[nfiles] = endPosition;
	    ++nfiles;
	}
    }
    make_table_crc32();
    printf("%6s %11s %5s %5s %8s\n",
	   "M", "k", "BSD", "SysV", "CRC-32");
    if(argc == 1) {
	if(sums(0, "-", endPosition) == -1) {
	    perror("stdin");
	    errcode = 1;
	}
    } else {
	for(i = 0; i < nfiles; ++i) {
	    char* path = filenames[i];
	    char file[40];
	    int fd;
	    if((fd = open(path, O_RDONLY | O_LARGEFILE)) == -1) {
		perror(path);
		errcode = 1;
		continue;
	    }
	    strncpy(file, (strncmp(path, "/dev/", 5) == 0)?
		    path : basename(path), 39);
	    file[39] = '\0';
	    if(sums(fd, file, endPositions[i]) == -1) {
		perror(path);
		close(fd);
		errcode = 1;
		continue;
	    }
	    if(close(fd) == -1) {
		perror(path);
		errcode = 1;
		continue;
	    }
	}
    }
    free(filenames);
    free(endPositions);
    return errcode;
}
