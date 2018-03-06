#!python3
import os
import time
import hashlib
import argparse
import random
import subprocess
from multiprocessing import Pool

# global variables
HOME = "/home/max/uni/"
PROFILER_PATH = HOME + "GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/profile.jar"
PROFILER_PROPERTIES_PATH = HOME + "GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/p/adaptedprofile.properties"
PROJECTS_JAR_PATH = HOME + "GreenDev/code/subjectSystems/jars/"
PROFILING_OUTPUT_ROOT = HOME + "GreenDev/code/rawProfilingOutput/"
PROGRAM_PARAMETERS_FILE = "parameter.txt"

debug = True

class Config_H2:
    #http://www.h2database.com/javadoc/org/h2/engine/DbSettings.html:

    #ANALYZE_AUTO [int:0-X/2000]
    #ANALYZE_SAMPLE [int:0-X/10000]
    #COMPRESS [True/False]
    #EARLY_FILTER [True/False]
    #DELEATE: #MAX_QUERY_TIMEOUT [int:0-X/0]

    #MULTI_THREADED [True/False]
    #MV_STORE [True/False]
    #OPTIMIZE_EVALUATABLE_SUBQUERIES [True/False]
    #OPTIMIZE_IN_LIST [True/False]
    #OPTIMIZE_IN_SELECT [True/False]

    #OPTIMIZE_INSERT_FROM_SELECT [True/False]
    #OPTIMIZE_IS_NULL [True/False]
    #OPTIMIZE_OR [True/False]
    #OPTIMIZE_TWO_EQUALS [True/False]
    #PAGE_STORE_INTERNAL_COUNT [True/False]

    #RECOMPILE_ALWAYS [True/False]
    #ROWID [True/False]
    #workload
    
    def __init__(self):
        self.c = []
        self.initialize()
        self.h = self.hash()

    def get_config(self):
        return self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], self.c[5],\
        self.c[6], self.c[7], self.c[8], self.c[9], self.c[10], self.c[11],\
        self.c[12], self.c[13], self.c[14], self.c[15]

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13},{14},{15},{16}]".format(
            self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], self.c[5], self.c[6], self.c[7], 
            self.c[8], self.c[9], self.c[10], self.c[11], self.c[12], self.c[13], self.c[14], 
            self.c[15])

    def initialize(self):
        # configuration space (6,400,000):
        self.c.append(random.randrange(0,4001))
        self.c.append(random.randrange(0,10001))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))

        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))

    def hash(self):
        tmp = str(self.c[0]) + str(self.c[1]) \
            + str(self.c[2]) + str(self.c[3]) \
            + str(self.c[4]) + str(self.c[5])
        return hashlib.sha224(tmp.encode('utf-8')).hexdigest()

    def distance_to_list(self, c_list):
        dist = 0
        for i in range(len(c_list)):
            dist += self.distance(c_list[i])
        return dist

    def distance(self, c2):
        D0   = abs(self.c[0]-c2.c[0])/4000
        D1   = abs(self.c[1]-c2.c[1])/10000
        D2   = abs(self.c[2]-c2.c[2])
        D3   = abs(self.c[3]-c2.c[3])
        D5   = abs(self.c[4]-c2.c[4])
        D6   = abs(self.c[5]-c2.c[5])
        D7   = abs(self.c[6]-c2.c[6])
        D8   = abs(self.c[7]-c2.c[7])
        D9   = abs(self.c[8]-c2.c[8])
        D10   = abs(self.c[9]-c2.c[9])
        D11   = abs(self.c[10]-c2.c[10])
        D12   = abs(self.c[11]-c2.c[11])
        D13   = abs(self.c[12]-c2.c[12])
        D14   = abs(self.c[13]-c2.c[13])
        D15   = abs(self.c[14]-c2.c[14])
        D16   = abs(self.c[15]-c2.c[15])
        return D0+D1+D2+D3+D5+D6+D7+D8+D9+D10+D11+D12+D13+D14+D15+D16

def random_search(n):
    configurations = []
    configurations.append(Config_H2())
    
    iterations = 0

    lerning_parameter = 3.0
    lerning_rate = 0.1

    while len(configurations) < n:
        individual = Config_H2()
        if individual.distance_to_list(configurations)/len(configurations) > lerning_parameter:
            configurations.append(individual)
            lerning_parameter += lerning_rate
        else:
            lerning_parameter -= lerning_rate/10
        iterations += 1
        if iterations%n == 0 and debug:
            print("Num iterations: " + str(iterations))
            print("Conf len: " + str(len(configurations)))

    print("Fin lerning parameter: " + str(lerning_parameter))
    print()
    return configurations

def init_parameter_sunflow0():
    # 1 parameter for image resolution
    return 10000

def init_parameter_sunflow1():
    # 1 parameter for image resolution
    return 50000

def init_parameter_sunflow2():
    # 1 parameter for image resolution
    return 100000

def profile_h2(iterations):

    # generate configurations:
    configurations = random_search(iterations)

    print("Number configs: " + str(len(configurations)))

    #for i in range(len(configurations)):
    #    profile(configurations[i])
    pool = Pool(7)
    pool.map(profile, configurations)
    pool.close() 
    pool.join()

    print("Done.")


# map number to boolean
def mntb(i):
    if i==0:
        return "FALSE"
    else:
        return "TRUE"


def profile(config):

    # project dependent
    PROJECT_JAR = "h2.jar"
    PROJECT_PATH = PROJECTS_JAR_PATH+PROJECT_JAR

    D0,D1,D2,D3,D5,D6,D7,D8,D9,D10,D11,D12,D13,D14,D15,D16 = config.get_config()

    PROFILING_OUTPUT_FOLDER_NAME = """AUTO-{0}_SAMPLE-{1}_COMPRESS-{2}_FILTER-{3}_THREADED-{4}\
_MV_STORE-{5}_SUBQUERIES-{6}_IN_LIST-{7}_IN_SELECT-{8}_INSERT_FROM_SELECT-{9}\
_IS_NULL-{10}_OR-{11}_TWO_EQUALS-{12}_PAGE_STORE-{13}_RECOMPILE_ALWAYS-{14}_ROWID-{15}""".format(
        str(D0), str(D1), str(D2), str(D3), str(D5), str(D6), str(D7), str(D8), str(D9), str(D10), str(D11), str(D12), str(D13), 
        str(D14), str(D15), str(D16))


    PATH_FOR_PROFILING_PROPERTIES = PROFILING_OUTPUT_ROOT + PROJECT_JAR + "/" + PROFILING_OUTPUT_FOLDER_NAME + "/"

    if not os.path.exists(PATH_FOR_PROFILING_PROPERTIES):
        os.makedirs(PATH_FOR_PROFILING_PROPERTIES)

    # update profiler properties
    output=''
    with open(PROFILER_PROPERTIES_PATH, "r") as f:
        for line in f:
            if "file=" in line:
                output+="file="+PATH_FOR_PROFILING_PROPERTIES+"\n"
                #print("found it")
            else:
                output+=line
    profpropadapted = PROFILER_PROPERTIES_PATH+PROFILING_OUTPUT_FOLDER_NAME
    with open(profpropadapted, "w") as text_file:
        text_file.write(output)

    for i in range(3):
        time.sleep(2)
        if debug:
            print(i)

        if i == 0:
            res = init_parameter_sunflow0()
        elif i == 1:
            res = init_parameter_sunflow1()
        elif i == 2:
            res = init_parameter_sunflow2()

        HASHING_PARAMETER = str(res) + "\n"
        #print(HASHING_PARAMETER)

        #print(PATH_FOR_PROFILING_PROPERTIES+PROGRAM_PARAMETERS_FILE)
        with open(PATH_FOR_PROFILING_PROPERTIES+PROGRAM_PARAMETERS_FILE, "a") as f:
            f.write(HASHING_PARAMETER)

        # output files
        stdout = open("stdout.txt","ab")
        stderr = open("stderr.txt","ab")

        # filally call java jar:
        javaagent = "-javaagent:"+PROFILER_PATH
        Dprofile  = "-Dprofile.properties="+profpropadapted

        #print("KJASGDFOAISFDHG: " + str(mntb(D2)))
        subprocess.call(['java', javaagent, Dprofile, '-noverify', '-jar', PROJECT_PATH, str(D0), str(D1), str(mntb(D2)), str(mntb(D3)), str(mntb(D5)), str(mntb(D6)), str(mntb(D7)),
            str(mntb(D8)), str(mntb(D9)), str(mntb(D10)), str(mntb(D11)), str(mntb(D12)), str(mntb(D13)), str(mntb(D14)), str(mntb(D15)), str(mntb(D16)), str(res), PROFILING_OUTPUT_FOLDER_NAME])

        #subprocess.call(['java', '-jar', PROJECT_PATH, str(D0), str(D1), str(mntb(D2)), str(mntb(D3)), str(mntb(D5)), str(mntb(D6)), str(mntb(D7)),
        #    str(mntb(D8)), str(mntb(D9)), str(mntb(D10)), str(mntb(D11)), str(mntb(D12)), str(mntb(D13)), str(mntb(D14)), str(mntb(D15)), str(mntb(D16)), str(res)])

    print("Done: " + PROFILING_OUTPUT_FOLDER_NAME)



def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("n", help="number of profiling calls", type=int)
    args = parser.parse_args()
    
    n = args.n
    print("start profiling")
    profile_h2(n)


if __name__ == "__main__":
    main()