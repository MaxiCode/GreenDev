#!/bin/bash

# clear prev data
#rm -r output/*
rm std*
rm jip/properties/*_*

# run profiling 
# with 400 rand samples
# 0 - catena
# 1 - h2
# 2 - sunflow
taskset 0x1 python3 profiling_script_random.py 100 100 0