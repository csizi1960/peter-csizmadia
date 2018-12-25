all: citations.pdf

citations.pdf: citations.tex citations.src.txt
	perl citations.pl tex
	latex citations.tex
	dvipdf citations.dvi citations.pdf
	rm -f citations.log citations.aux citations.dvi
	rm -f citations.sci.tex citations.eprint.tex citations.proc.tex
