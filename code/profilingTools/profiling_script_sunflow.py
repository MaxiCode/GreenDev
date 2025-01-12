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

debug = False

class Config_Sunflow:
    # thr , diff, refl, refr, bSize, samples
    # 1-10, 1-10, 1-10, 1-10, 1-64 , 1-10
    def __init__(self):
        self.c = []
        self.initialize()
        self.h = self.hash()

    def get_config(self):
        return self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], self.c[5]

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5}]".format(
            self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], self.c[5])

    def initialize(self):
        # configuration space (6,400,000):
        self.c.append(random.randrange(1,10))
        self.c.append(random.randrange(1,10))
        self.c.append(random.randrange(1,10))
        self.c.append(random.randrange(1,10))
        self.c.append(random.randrange(1,64))
        self.c.append(random.randrange(1,10))

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
        D0    = abs(self.c[0]-c2.c[0])/9
        D1   = abs(self.c[1]-c2.c[1])/9
        D2   = abs(self.c[2]-c2.c[2])/9
        D3     = abs(self.c[3]-c2.c[3])/9
        D4  = abs(self.c[4]-c2.c[4])/63
        D5  = abs(self.c[5]-c2.c[5])/9
        return D0+D1+D2+D3+D4+D5

def random_search(n):
    configurations = []
    configurations.append(Config_Sunflow())
    
    iterations = 0

    lerning_parameter = 3.0
    lerning_rate = 0.1

    while len(configurations) < n:
        individual = Config_Sunflow()
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
    return 64

def init_parameter_sunflow1():
    # 1 parameter for image resolution
    return 128

def init_parameter_sunflow2():
    # 1 parameter for image resolution
    return 256

def profile_sunflow(iterations):

    # generate configurations:
    configurations = random_search(iterations)

    print("Number configs: " + str(len(configurations)))
    
    
    pool = Pool(4)
    pool.map(profile, configurations)
    pool.close() 
    pool.join()

    print("Done.")

def profile(config):

    # project dependent
    PROJECT_JAR = "sunflow.jar"
    PROJECT_PATH = PROJECTS_JAR_PATH+PROJECT_JAR

    thr , diff, refl, refr, bSize, samples = config.get_config()

    PROFILING_OUTPUT_FOLDER_NAME = """thr-{0}_diff-{1}_refl-{2}_refr-{3}_bSize-{4}_samples-{5}""".format(
        str(thr), str(diff), str(refl), str(refr), str(bSize), str(samples))


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

        subprocess.call(['java', javaagent, Dprofile, '-noverify', '-jar', PROJECT_PATH, str(res), str(thr), str(diff), str(refl), str(refr), str(bSize), str(samples)], stdout=stdout, stderr=stderr)

    print("Done: " + PROFILING_OUTPUT_FOLDER_NAME)
        


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("n", help="number of profiling calls", type=int)
    args = parser.parse_args()
    
    n = args.n
    print("start profiling")
    profile_sunflow(n)


if __name__ == "__main__":
    main()
