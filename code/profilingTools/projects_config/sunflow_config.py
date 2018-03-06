#!python3

# Sunflow configuration class

import random
import hashlib

class Config_Sunflow:

    jar_file = "sunflow.jar"
    workload = [64,128,256]

    # thr , diff, refl, refr, bSize, samples
    # 1-10, 1-10, 1-10, 1-10, 1-64 , 1-10
    def __init__(self, rand_state):
        random.setstate(rand_state)
        self.c = []
        self.initialize()
        self.h = self.hash()
        self.fin_rand_state

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

        self.fin_rand_state = random.getstate()

    def hash(self):
        tmp = str(self.c[0]) + str(self.c[1]) \
            + str(self.c[2]) + str(self.c[3]) \
            + str(self.c[4]) + str(self.c[5])
        return hashlib.sha224(tmp.encode('utf-8')).hexdigest()

    def get_rand_state(self):
        return self.fin_rand_state