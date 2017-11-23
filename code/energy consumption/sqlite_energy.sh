#!/bin/bash

# SETUP ENVIRONMENT
VERBOSE=0

# Get config id
CONFIG_NUM=$1
CONFIG_FILE=$2

# Get all additional opt flags for preoptimization
#PPROF_OPT_FLAGS=$5
echo $1

export LD_RUN_PATH
export PATH
export VERBOSE


# Run the experiments
export CONFIG_ID="${CONFIG_ID}"
export CONFIG_NUM="${CONFIG_NUM}"

export OPT_FLAGS="${PPROF_OPT_FLAGS}"

# Copy benchmark + sqlite to have a clean state
cd /local/siegmunn/
mkdir -p /local/siegmunn/benchmarks/casestudies/sqlite_bench
cd /local/siegmunn/benchmarks/casestudies/sqlite_bench
rsync -a -r -del "/home/siegmunn/benchmarks/casestudies/bech_sqlite/" "/local/siegmunn/benchmarks/casestudies/sqlite_bench"
cp /home/siegmunn/benchmarks/casestudies/sqlite_toMeasure/$2 /local/siegmunn/benchmarks/casestudies/sqlite_bench/user_config.h
cd /local/siegmunn/benchmarks/casestudies/sqlite_bench
make clean

# run configure script for sqlite
./configure
# copy our current configuration to a file that is used by the sqlite make process and call make
cat user_config.h >> config.h
make
# go to the benchmark folder and print some log infos
cd leveldb-master
RESULTS=${CONFIG_NUM}.log
echo $1 >> $RESULTS
echo $2 >> $RESULTS

# make db_bench_sqlite3 new (compile the benchmark with the newly generated sqlite that conforms to our configuration)
export LD_LIBRARY_PATH=/local/siegmunn/benchmarks/casestudies/sqlite_bench/.libs:$LD_LIBRARY_PATH

# ldd db_bench_sqlite3
# start the benchmark a possibly repeat it
# @Philipp: Achtung, hier muss du aber mit der Energiemessung das ganze synchronisieren und diesen Start evtl. in eine andere Datei auslagern!
(time ./db_bench_sqlite3) &>> $RESULTS
(time ./db_bench_sqlite3) &>> $RESULTS
(time ./db_bench_sqlite3) &>> $RESULTS

# copy the produced file containing the benchmark statistics back to a save place
# @Philipp: hier musst du statt rsync eher "cp ..." benutzen und dahin kopieren, wo du dann später mit deinem Tool die Ausgabedatei einliest
rsync -a "/local/siegmunn/benchmarks/casestudies/sqlite_bench/leveldb-master/$RESULTS" "/home/siegmunn/benchmarks/casestudies/sqlite_results/"

# delete the folder so that we have a clean state again
rm $RESULTS
