#!python3

# Catena configuration class

import hashlib
import random

class Config_Catena:

    jar_file = "catena.jar"
    workload = [["","",1],
                ["012345012345012345012345","6789ab6789ab",32],
                ["012345012345012345012345012345012345012345012345","6789ab6789ab6789ab6789ab",64]]

    def __init__(self, rand_state):
        random.setstate(rand_state)
        self.c = []
        self.initialize()
        self.h = self.hash()
        self.fin_rand_state

    def get_config(self):
        return self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], \
            self.c[5], self.c[6], self.c[7], self.c[8], self.c[9]

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5},{6},{7},{8},{9}]".format(
            self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], 
            self.c[5], self.c[6], self.c[7], self.c[8], self.c[9])

    def initialize(self):
        # configuration space (3,774,873,600):
        self.c.append(random.randrange(0,2))        # hash
        self.c.append(random.randrange(0,2))        # gamma
        self.c.append(random.randrange(1,5))        # graph
        self.c.append(random.randrange(0,2))        # phi
        self.c.append(random.randrange(1,15))       # garlic
        self.c.append(random.randrange(1,101))      # lambda
        self.c.append(random.randrange(0,256))      # v_id
        self.c.append(random.randrange(0,256))      # d
        # static part:
        self.c.append("Butterfly-Full-adapted")
        self.c.append("6789ab")
        self.fin_rand_state = random.getstate()

    def hash(self):
        tmp = str(self.c[0]) + str(self.c[1]) \
            + str(self.c[2]) + str(self.c[3]) \
            + str(self.c[4]) + str(self.c[5]) \
            + str(self.c[6]) + str(self.c[7])
        return hashlib.sha224(tmp.encode('utf-8')).hexdigest()

    def get_rand_state(self):
        return self.fin_rand_state