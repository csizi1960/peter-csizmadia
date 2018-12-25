.SUFFIXES: .tex .pdf

all: appendix.pdf milkyway_whc.jpg

appendix.pdf: appendix.tex
	pdflatex appendix.tex
	pdflatex appendix.tex
	rm -f appendix.aux appendix.log appendix.out
	touch -r appendix.tex appendix.pdf

milkyway_whc.jpg: milkyway.jpg
	convert -scale 320x320 -fill none -strokewidth 2\
 -stroke "#330000" -draw "ellipse 160,246 15,15 0,360"\
 -stroke "#990000" -draw "ellipse 160,246 88,88 0,360"\
 -stroke "#ff3333" -draw "ellipse 160,246 229,229 0,360" milkyway.jpg $@
