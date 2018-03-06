#!python3

import os
import time
import subprocess

class Profiler:

    def __init__(self, project, configs):
        self.project_root = os.getcwd()
        self.project = project
        self.configs = configs

        self.jar_path = "jars/"
        self.output = "output/"
        self.profile_jar = "jip/profile.jar"
        self.profile_properties_path = "jip/properties/"
        self.profile_properties_file = "adaptedprofile.properties"
        self.parameter_file = "parameter.txt"

        #print("init profiler with project " + str(self.project.jar_file) + " and " + str(len(self.configs)) + " configs")
        #print("cwd " + project_root)

    def profile(self):
        for c in self.configs:
            # create folder name by config
            c_f_name = self.config_folder_name(c.get_config())
            #print(c_f_name)

            output_folder = self.project_root+"/"+self.output+str(self.project.jar_file)+"/"+c_f_name+"/"
            if not os.path.exists(output_folder):
                os.makedirs(output_folder)

            # update profiler properties
            profile_prop_path = self.project_root+"/"+self.profile_properties_path+self.profile_properties_file
            output=''
            with open(profile_prop_path, "r") as f:
                for line in f:
                    if "file=" in line:
                        output+="file="+output_folder+"\n"
                        #print("found it")
                    else:
                        output+=line
            #print(profile_prop_path)
            #print(c_f_name)
            prof_prop_adapted = profile_prop_path+"__"+c_f_name
            with open(prof_prop_adapted, "w") as text_file:
                text_file.write(output)
            #print(prof_prop_adapted)

            # iterate over workloads
            for w in self.project.workload:
                
                workload = str(w) + "\n"
                #print(w)
                #print(HASHING_PARAMETER)

                #print(PATH_FOR_PROFILING_PROPERTIES+PROGRAM_PARAMETERS_FILE)
                with open(output_folder+self.parameter_file, "a") as f:
                    f.write(workload)

                # output files
                stdout = open("stdout.txt","ab")
                stderr = open("stderr.txt","ab")

                # filally call java jar:
                javaagent = "-javaagent:"+self.project_root+"/"+self.profile_jar
                Dprofile  = "-Dprofile.properties="+prof_prop_adapted

                parameter_array = []
                parameter_array.append("java")
                parameter_array.append(javaagent)
                parameter_array.append(Dprofile)
                parameter_array.append("-noverify")
                parameter_array.append("-jar")
                parameter_array.append(self.project_root+"/"+self.jar_path+str(self.project.jar_file))
                #print(parameter_array)
                #print(c)
                if isinstance(w,list):
                    java_parameter = parameter_array+[str(el) for el in c.get_config()]+[str(el) for el in w]
                else:
                    java_parameter = parameter_array+[str(el) for el in c.get_config()]+[str(w)]
                # in case of sunflow, add the path to the image folder
                if str(self.project.jar_file) == "sunflow.jar":
                    java_parameter = java_parameter+[output_folder]

                # std of project execution
                #print(java_parameter)
                for _ in range(5):
                    #print()
                    subprocess.call(java_parameter, stdout=stdout, stderr=stderr)
                    time.sleep(2)
            print("Done: " + c_f_name)


    def config_folder_name(self, config_elements):
        config_folder = []
        first = True
        for el in config_elements:
                if first:
                    config_folder.append(str(el))
                    first = False
                else:
                    config_folder.append("_" + str(el))
        return "".join(config_folder)

    

    