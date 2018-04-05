MAIN = thesis

all: build clean

build:
	rubber --module xelatex --warn=all --jobname thesis $(MAIN).tex

clean:
	rm -f *.log *.toc *.aux *.nav *.out *.blg *.snm *.vrb
