import csv
import matplotlib.pyplot as plt
from collections import defaultdict
from statistics import mean 
import os

data = defaultdict(list)

output_dir = "./output_plots"

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

with open('./output/executor_based.csv', newline='') as csvfile:
    reader = csv.DictReader(csvfile, delimiter=';')
    for row in reader:
        key = (row["Image"], row["Filter"])
        data[key].append((int(row["Number of Threads"]), row["Image Division Method"], int(row["Threshold(px)"]), float(row["Time(ms)"])))

for key, image_data in data.items():
    image, filter_name = key
    plt.figure()
    plt.title(f"Time vs Threshold for Different Division Methods p/Threads\n {os.path.basename(image)} ({filter_name})")
    
    division_methods = list(set(entry[1] for entry in image_data))
    threads = list(set(entry[0] for entry in image_data))
    
    for thread_count in threads:
        for division_method in division_methods:
            subset = [(threshold, time) for thread, div_method, threshold, time in image_data if thread == thread_count and div_method == division_method]
            thresholds, times = zip(*subset)
            
            thresholds = (thresholds[0], thresholds[3], thresholds[6])
            times = (mean([times[0], times[1], times[2]]),
                            mean([times[3], times[4], times[5]]),
                            mean([times[6], times[7], times[8]]))
            
            plt.plot(thresholds, times, label=f"{thread_count} Threads ({division_method})")
            plt.xticks(thresholds)
    
    plt.xlabel("Threshold (px)")
    plt.ylabel("Time (ms)")
    plt.legend(fontsize=7, loc='upper left')
    plt.grid(True)

    output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(image))[0]}_{filter_name}_plot.png")
    plt.savefig(output_file, dpi=300)
    plt.close()