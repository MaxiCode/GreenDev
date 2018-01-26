#!python3
import random
import hashlib
import math

class Config_Catena:
    def __init__(self):
        self.c = []
        self.initialize()
        self.h = self.hash()

    def __str__(self):
        return "[{0},{1},{2},{3},{4},{5},{6},{7},{8},{9}]".format(
            self.c[0], self.c[1], self.c[2], self.c[3], self.c[4], 
            self.c[5], self.c[6], self.c[7], self.c[8], self.c[9])

    def initialize(self):
        # configuration space:
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(1,5))
        self.c.append(random.randrange(0,2))
        self.c.append(random.randrange(1,25))
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
                self.c[dim] = random.randrange(1,25)
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
        GARLIC  = abs(self.c[4]-c2.c[4])/23
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
        if iterations%n == 0:
            print("Num iterations: " + str(iterations))
            print("Conf len: " + str(len(configurations)))

    print("Configurations: " + str(len(configurations)))
    print("Fin lerning parameter: " + str(lerning_parameter))
    #for i in range(len(configurations)):
    #    print(configurations[i])


def main():
    individuals = []
    for i in range(1000):
        # init configs
        individuals.append(Config_Catena())

    print()
    dist = 0
    for i in range(len(individuals)):
        dist += individuals[i].distance_to_list(individuals)

    print(dist)
    # mutate
    for i in range(10000):
        el = random.randrange(0,len(individuals))
        individuals[el].mutate(10)
        if (i % 1000 == 0):
            print(i)

    print()
    dist = 0
    for i in range(len(individuals)):
        dist += individuals[i].distance_to_list(individuals)
    print(dist)

def afunc(n):
    tmp = []
    for _ in range(n):
        tmp.append(Config_Catena())

    for i in range(100):
        tmp[i].distance_to_list(tmp)
    print("Done fiddeling with " + str(n) + " things.")

def threading_test():
    from multiprocessing import Pool
    

    arr = [1000,2000,3000,4000,5000,6000,7000,8000,9000,1000,2000,3000,4000, 1000,10000,1000,2000,3000,4000,5000,6000,7000,8000,9000,1000,2000,3000,4000, 1000,10000,1000,2000,3000,4000,5000,6000,7000,8000,9000,1000,2000,3000,4000, 1000,10000]

    pool = Pool(4)
    pool.map(afunc, arr)
    pool.close() 
    pool.join()


if __name__ == "__main__":
    threading_test()
    #random_search(1000)
    #main()