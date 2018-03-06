#!python3
# profiling script random sampling

from projects_config.catena_config import Config_Catena
from projects_config.sunflow_config import Config_Sunflow
from projects_config.h2_config import Config_H2
from profiler import Profiler

import argparse
import sys
import random

rand_seed = 3

def init_configurations(config_generator, n):
    # save state of random to reproduce experiments
    random.seed(rand_seed)
    rand_state = random.getstate()
    
    configurations = []

    for _ in range(n):
        c = config_generator(rand_state)
        rand_state = c.get_rand_state()
        configurations.append(c)

    return configurations

def init_project(project):
    config_generator = None
    if project == 0:
        #print("catena")
        config_generator = Config_Catena
    elif project == 1:
        #print("h2")
        config_generator = Config_H2
    elif project == 2:
        #print("sunflow")
        config_generator = Config_Sunflow
    else:
        sys.exit("Project id: " + str(project) + " not defined")

    return config_generator

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("n", help="number of profiling calls", type=int)
    parser.add_argument("p", help="""specify project to be profiled: 
        0 - catena
        1 - h2
        2 - sunflow""", type=int)
    args = parser.parse_args()
    
    n = args.n
    proj = args.p

    print("init project")
    config_generator = init_project(proj)
    #print(config_generator.jar_file)
    print("init configs")
    configs = init_configurations(config_generator, n)
    print("start profiling")
    p = Profiler(config_generator, configs)
    p.profile()

if __name__ == "__main__":
    main()