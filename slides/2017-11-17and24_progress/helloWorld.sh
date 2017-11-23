#!/bin/bash

echo "Hello world!" >> /scratch/${USER}/test.txt
echo "The script was executed on ${SLURMD_NODENAME} having job id ${SLURM_JOB_ID} and the name ${SLURM_JOB_NAME}. "
