MAIN = green_dev

all: build clean

build:
	rubber --module xelatex --warn=all --jobname green_dev $(MAIN).tex

clean:
	rm -f *.log *.toc *.aux *.nav *.out *.blg *.snm *.vrb
