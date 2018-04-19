MAIN = thesis

all: build clean

build:
#	rubber --pdf --warn=all --jobname thesis $(MAIN).tex
#	rubber --module xelatex --warn=all --jobname thesis $(MAIN).tex
	pdflatex $(MAIN).tex
	bibtex $(MAIN)
	#makeindex -s $(MAIN).ist -t $(MAIN).glg -o $(MAIN).gls $(MAIN).glo
	pdflatex $(MAIN).tex
	pdflatex $(MAIN).tex

clean:
	rm -f *.log *.toc *.aux *.nav *.out *.blg *.snm *.vrb *.bbl *.brf *.syg *.ist *.ind *.ilg *.glsdefs *.gls *.glo *.glg *.acn


