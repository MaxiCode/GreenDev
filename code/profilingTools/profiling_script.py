#!python3
import os
import hashlib
import argparse
import random
import subprocess

# global variables
PROFILER_PATH = "/home/max/uni/GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/profile.jar"
PROFILER_PROPERTIES_PATH = "/home/max/uni/GreenDev/code/profilingTools/JavaInteractiveProfiler/jip-src-1.2/profile/adaptedprofile.properties"
PROJECTS_JAR_PATH = "/home/max/uni/GreenDev/code/subjectSystems/jars/"
PROFILING_OUTPUT_ROOT = "/home/max/uni/GreenDev/code/rawProfilingOutput/"
PROGRAM_PARAMETERS_FILE = "parameter.txt"

def init_config_catena():
	# 10 parameter to initialize catena (config)
	# 	1 bool (FullHash or Reduced Hash)			HASH
	# 	2 bool (Gamma Layer or no Gamma layer)		GAMMA
	#	3 int(1-4) (1-DBG, 2-BRG, 3-GRG, 4-SBRG)	GRAPH
	# 	4 bool (Phi Layer or no Phi Layer)			PHI
	# 	5 byte (0-255) (garlic)						GARLIC 	!
	# 	6 byte (0-255) (lambda)						LAMBDA 	!
	# 	7 (Version ID)								V_ID			<- hex representation FIXED
	#	8 (gamma, additional salt)					GAMMA_SALT		<- hex representation FIXED
	# 	9 (aData, server salt)						ADDITIONAL_DATA	<- hex representation FIXED
	#  10 byte (0-255) (mode of catena)				D

	HASH = random.randrange(0,2)
	GAMMA = random.randrange(0,2)
	GRAPH = random.randrange(1,5)
	PHI = random.randrange(0,2)
	GARLIC = random.randrange(1,23)
	LAMBDA = random.randrange(1,5)
	V_ID = "Butterfly-Full-adapted"
	GAMMA_SALT = "6789ab"
	ADDITIONAL_DATA = "000000"
	D = random.randrange(0,256)
	return HASH, GAMMA, GRAPH, PHI, GARLIC, LAMBDA, V_ID, GAMMA_SALT, ADDITIONAL_DATA, D

def init_parameter_catena():
	# 3 parameter for hashing
	# 	11 str (password to be hashed)		<- hex representation	PWD
	# 	12 str (salt of user)				<- hex representation	SALT
	#	13 int(0-64) (output length)								OUTPUT_LENGTH

	PWD = "012345"
	SALT = "6789ab"
	OUTPUT_LENGTH = 64
	return PWD, SALT, OUTPUT_LENGTH

def test_dir(file):
	print(str(os.path.isdir(file)) + " " + str(os.path.exists(file)))


def profile_catena(iterations):
	print("start profiling catena with " + str(iterations) + " iterations")

	# project dependent
	PROJECT_JAR = "catenaExtract.jar"
	PROJECT_PATH = PROJECTS_JAR_PATH+PROJECT_JAR

	USED_CONFIGS=[]

	HASH, GAMMA, GRAPH, PHI, GARLIC, LAMBDA, V_ID, GAMMA_SALT, ADDITIONAL_DATA, D = init_config_catena()

	for i in range(iterations):
		print("Profiling {0} / {1}".format(i+1,iterations))

		PROFILING_OUTPUT_FOLDER_NAME = """hash-{0}_gamma-{1}_graph-{2}_phi-{3}_garlic-{4}_lambda-{5}_vID-{6}_gammaSalt-{7}_ad-{8}_d-{9}""".format(
			str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), V_ID, GAMMA_SALT, ADDITIONAL_DATA, D)

		hash = hashlib.sha224(PROFILING_OUTPUT_FOLDER_NAME.encode('utf-8')).hexdigest()
		while USED_CONFIGS.count(hash) > 0:
			random.seed()
			HASH, GAMMA, GRAPH, PHI, GARLIC, LAMBDA, V_ID, GAMMA_SALT, ADDITIONAL_DATA, D = init_config_catena()
			PROFILING_OUTPUT_FOLDER_NAME = """hash-{0}_gamma-{1}_graph-{2}_phi-{3}_garlic-{4}_lambda-{5}_vID-{6}_gammaSalt-{7}_ad-{8}_d-{9}""".format(
				str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), V_ID, GAMMA_SALT, ADDITIONAL_DATA, D)
			hash = hashlib.sha224(PROFILING_OUTPUT_FOLDER_NAME.encode('utf-8')).hexdigest()
		
		print(PROFILING_OUTPUT_FOLDER_NAME)
		print(hash)

		USED_CONFIGS.append(hash)
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
		with open(PROFILER_PROPERTIES_PATH, "w") as text_file:
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
		Dprofile  = "-Dprofile.properties="+PROFILER_PROPERTIES_PATH
		subprocess.call(['java', javaagent, Dprofile, '-noverify', '-jar', PROJECT_PATH, str(HASH), str(GAMMA), str(GRAPH), str(PHI), str(GARLIC), str(LAMBDA), V_ID, PWD, SALT, GAMMA_SALT, ADDITIONAL_DATA, str(OUTPUT_LENGTH), str(D)], stdout=stdout, stderr=stderr)
		print("")
		


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument("n", help="number of profiling calls", type=int)
	args = parser.parse_args()
	
	n = args.n
	print("start profiling")
	profile_catena(n)


if __name__ == "__main__":
    main()