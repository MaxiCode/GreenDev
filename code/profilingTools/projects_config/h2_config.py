#!python3

# H2 configuration class

import random
import hashlib

class Config_H2:

    jar_file = "h2.jar"
    workload = [10000,55000,100000]

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
    
    def __init__(self, rand_state):
        random.setstate(rand_state)
        self.c = []
        self.initialize()
        self.h = self.hash()
        self.fin_rand_state

    def get_config(self):
        return self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], self.c[5],\
        self.c[6], self.c[7], self.c[8], self.c[9], self.c[10], self.c[11],\
        self.c[12], self.c[13], self.c[14], self.c[15]

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13},{14},{15}]".format(
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

        self.fin_rand_state = random.getstate()

    def hash(self):
        tmp = str(self.c[0]) + str(self.c[1]) \
            + str(self.c[2]) + str(self.c[3]) \
            + str(self.c[4]) + str(self.c[5]) \
            + str(self.c[6]) + str(self.c[7]) \
            + str(self.c[8]) + str(self.c[9]) \
            + str(self.c[10]) + str(self.c[11]) \
            + str(self.c[12]) + str(self.c[13]) \
            + str(self.c[14]) + str(self.c[15])
        return hashlib.sha224(tmp.encode('utf-8')).hexdigest()

    def get_rand_state(self):
        return self.fin_rand_state