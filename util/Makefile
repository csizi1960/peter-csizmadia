all: sdirdiff.tar.gz $(HOME)/bin/pcsums

sdirdiff.tar.gz: sdirdiff sgetdiff sputdiff
	tar cvf - sdirdiff sgetdiff sputdiff | gzip -9 >sdirdiff.tar.gz

$(HOME)/bin/pcsums: pcsums.c
	gcc -Wall -O3 -D_LARGEFILE64_SOURCE pcsums.c -o $@
