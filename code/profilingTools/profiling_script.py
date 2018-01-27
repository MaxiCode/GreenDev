#!python3
import os
import hashlib
import argparse
import random
import subprocess
from multiprocessing import Pool

# global variables
PROFILER_PATH = "/home/max/uni/GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/profile.jar"
PROFILER_PROPERTIES_PATH = "/home/max/uni/GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/p/adaptedprofile.properties"
PROJECTS_JAR_PATH = "/home/max/uni/GreenDev/code/subjectSystems/jars/"
PROFILING_OUTPUT_ROOT = "/home/max/uni/GreenDev/code/rawProfilingOutput/"
PROGRAM_PARAMETERS_FILE = "parameter.txt"

debug = False

class Config_Catena:
    def __init__(self):
        self.c = []
        self.initialize()
        self.h = self.hash()

    def get_config(self):
        return self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], \
            self.c[5], self.c[6], self.c[7], self.c[8], self.c[9]

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5},{6},{7},{8},{9}]".format(
            self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], 
            self.c[5], self.c[6], self.c[7], self.c[8], self.c[9])

    def initialize(self):
        # configuration space (9,830,400):
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(1,5))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(1,20))
        self.c.append(random.randrange(1,121))
        self.c.append(random.randrange(0,256))
        # static part:
        self.c.append("Butterfly-Full-adapted")
        self.c.append("6789ab")
        self.c.append("000000")

    def mutate(self, n):
        for i in range(n):
            dim = random.randrange(0,8)
            if dim == 0 or dim == 1 or dim == 3:
                self.c[dim] = random.randrange(0,2)
            elif dim == 2:
                self.c[dim] = random.randrange(1,5)
            elif dim == 4:
                self.c[dim] = random.randrange(1,20)
            elif dim == 5:
                self.c[dim] = random.randrange(1,121)
            elif dim == 6:
                self.c[dim] = random.randrange(0,256)
        self.h = self.hash()

    def hash(self):
        tmp = str(self.c[0]) + str(self.c[1]) \
            + str(self.c[2]) + str(self.c[3]) \
            + str(self.c[4]) + str(self.c[5]) \
            + str(self.c[6])
        return hashlib.sha224(tmp.encode('utf-8')).hexdigest()

    def distance_to_list(self, c_list):
        dist = 0
        for i in range(len(c_list)):
            dist += self.distance(c_list[i])
        return dist

    def distance(self, c2):
        HASH    = abs(self.c[0]-c2.c[0])
        GAMMA   = abs(self.c[1]-c2.c[1])
        GRAPH   = abs(self.c[2]-c2.c[2])/3
        PHI     = abs(self.c[3]-c2.c[3])
        GARLIC  = abs(self.c[4]-c2.c[4])/18
        LAMBDA  = abs(self.c[5]-c2.c[5])/119
        D       = abs(self.c[6]-c2.c[6])/255
        return HASH+GAMMA+GRAPH+PHI+GARLIC+LAMBDA+D

def random_search(n):
    configurations = []
    configurations.append(Config_Catena())
    
    iterations = 0

    lerning_parameter = 3.0
    lerning_rate = 0.1

    while len(configurations) < n:
        individual = Config_Catena()
        if individual.distance_to_list(configurations)/len(configurations) > lerning_parameter:
            configurations.append(individual)
            lerning_parameter += lerning_rate
        else:
            lerning_parameter -= lerning_rate/10
        iterations += 1
        if iterations%n == 0 and debug:
            print("Num iterations: " + str(iterations))
            print("Conf len: " + str(len(configurations)))

    print("Configurations: " + str(len(configurations)))
    print("Fin lerning parameter: " + str(lerning_parameter))
    print()
    return configurations

def init_parameter_catena():
    # 3 parameter for hashing
    #   11 str (password to be hashed)      <- hex representation   PWD
    #   12 str (salt of user)               <- hex representation   SALT
    #   13 int(0-64) (output length)                                OUTPUT_LENGTH

    PWD = "012345"
    SALT = "6789ab"
    OUTPUT_LENGTH = 64
    return PWD, SALT, OUTPUT_LENGTH

def test_dir(file):
    print(str(os.path.isdir(file)) + " " + str(os.path.exists(file)))


def profile_catena(iterations):

    # generate configurations:
    configurations = random_search(iterations)

    print("Anz configs: " + str(len(configurations)))
    print(configurations[0])
    
    pool = Pool(7)
    pool.map(profile, configurations)
    pool.close() 
    pool.join()

    print("Done.")

def profile(config):

    # project dependent
    PROJECT_JAR = "catenaExtract.jar"
    PROJECT_PATH = PROJECTS_JAR_PATH+PROJECT_JAR

    HASH, GAMMA, GRAPH, PHI, GARLIC, LAMBDA, V_ID, GAMMA_SALT, ADDITIONAL_DATA, D = config.get_config()

    PROFILING_OUTPUT_FOLDER_NAME = """hash-{0}_gamma-{1}_graph-{2}_phi-{3}_garlic-{4}_lambda-{5}_vID-{6}_gammaSalt-{7}_ad-{8}_d-{9}""".format(
        str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), V_ID, GAMMA_SALT, ADDITIONAL_DATA, D)


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

    PWD, SALT, OUTPUT_LENGTH = init_parameter_catena()
    HASHING_PARAMETER = PWD + " " + SALT + " " + str(OUTPUT_LENGTH)
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
    
    subprocess.call(['java', '-jar', PROJECT_PATH, str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), str(V_ID), PWD, SALT, str(GAMMA_SALT), str(ADDITIONAL_DATA), str(OUTPUT_LENGTH), str(D)], stdout=stdout, stderr=stderr)
    subprocess.call(['java', '-jar', PROJECT_PATH, str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), str(V_ID), PWD, SALT, str(GAMMA_SALT), str(ADDITIONAL_DATA), str(OUTPUT_LENGTH), str(D)], stdout=stdout, stderr=stderr)
    
    subprocess.call(['java', javaagent, Dprofile, '-noverify', '-jar', PROJECT_PATH, str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), str(V_ID), PWD, SALT, str(GAMMA_SALT), str(ADDITIONAL_DATA), str(OUTPUT_LENGTH), str(D)], stdout=stdout, stderr=stderr)

    print("Done: " + PROFILING_OUTPUT_FOLDER_NAME)
        


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("n", help="number of profiling calls", type=int)
    args = parser.parse_args()
    
    n = args.n
    print("start profiling")
    profile_catena(n)


if __name__ == "__main__":
    main()