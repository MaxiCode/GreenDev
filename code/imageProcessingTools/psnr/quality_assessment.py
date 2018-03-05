import numpy
import scipy.misc
import argparse
import os

import psnr
import vifp

def read_image(file):
    return scipy.misc.imread(file, flatten=True).astype(numpy.float32)

img_original_64 = read_image("/home/max/uni/GreenDev/code/subjectSystems/sunflow-src-v0.07.2/sunflow/resources/golden_0040.png")
img_original_128 = read_image("/home/max/uni/GreenDev/code/subjectSystems/sunflow-src-v0.07.2/sunflow/resources/golden_0080.png")
img_original_256 = read_image("/home/max/uni/GreenDev/code/subjectSystems/sunflow-src-v0.07.2/sunflow/resources/golden_0100.png")

gen_img_64 = "64.png"
gen_img_128 = "128.png"
gen_img_256 = "256.png"

quality_64_dump = []
quality_128_dump = []
quality_256_dump = []

def read_images(ref_file, dist_file):
    ref = read_image(ref_file)
    dist = read_image(dist_file)
    return ref, dist

def assess_psnr(ref, dist):
    return psnr.psnr(ref, dist)

def assess_vifp(ref, dist):
    return vifp.vifp_mscale(ref, dist)

def assess_mse(img1, img2):
    return numpy.mean( (img1 - img2) ** 2 )

def assess_all(img1, img2):
    mse = assess_mse(img1, img2)
    psnr = assess_psnr(img1, img2)
    vifp = assess_vifp(img1, img2)
    
    #print("MSE: " + str(assess_mse(img1, img2)))
    #print("PSNR: " + str(assess_psnr(img1, img2)))
    #print("VIFP: " + str(assess_vifp(img1, img2)))

    return (mse, psnr, vifp)

def assess_quality(folder):
    for config_folder in os.listdir(folder):
        #print(folder+config_folder)
        for file in os.listdir(folder+config_folder):
            # file = folder + config_folder + curr_file
            curr_file = os.path.basename(file)
            if curr_file == gen_img_64:
                img_64 = read_image(folder+config_folder+"/"+curr_file)
                quality_64_dump.append( (folder+config_folder+"/"+curr_file, assess_all(img_original_64, img_64)) )

            elif curr_file == gen_img_128:
                img_128 = read_image(folder+config_folder+"/"+curr_file)
                quality_128_dump.append( (folder+config_folder+"/"+curr_file, assess_all(img_original_128, img_128)) )

            elif curr_file == gen_img_256:
                img_256 = read_image(folder+config_folder+"/"+curr_file)
                quality_256_dump.append( (folder+config_folder+"/"+curr_file, assess_all(img_original_256, img_256)) )
    
    # -Start- Debug prints:
    print(str(len(quality_64_dump)))
    config_path, value_tup = quality_64_dump[0]
    mse, psnr, vifp = value_tup

    print("Test: " + config_path)
    print("Test: " + str(mse) + " " + str(psnr) + " " + str(vifp))
    
    for i in range(len(quality_64_dump)):
        config_path, value_tup = quality_64_dump[i]
    # -End- Debug prints



def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("dist", help="noice image folder", type=str)
    args = parser.parse_args()
    
    noice_img_folder = args.dist
    assess_quality(noice_img_folder)

    #print("start analyzing")
    #assess_mse(img1, img2)
    #assess_psnr(img1, img2)
    #assess_vifp(img1, img2)

if __name__ == "__main__":
    main()
    