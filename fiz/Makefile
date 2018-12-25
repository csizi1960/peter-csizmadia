all: cv.pdf

cv.pdf: cv.tex
	latex cv.tex
	dvipdf cv.dvi cv.pdf
	rm -f cv.log cv.aux cv.dvi
