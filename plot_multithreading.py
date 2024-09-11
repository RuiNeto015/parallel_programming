import csv
import matplotlib.pyplot as plt
from statistics import mean 
import os

data = {}

output_dir = "./output_plots"

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

with open('./output/multithreading.csv', newline='') as csvfile:
    reader = csv.DictReader(csvfile, delimiter=';')
    for row in reader:
        key = (row["Image"], row["Filter"])
        if key not in data:
            data[key] = []
        data[key].append({
            "method": row["Method"],
            "division_method": row["Image Division Method"],
            "threads": int(row["Number of Threads"]),
            "time_ms": float(row["Time(ms)"])
        })

for key, image_data in data.items():
    image, filter_name = key
    plt.figure()
    plt.title(f"Time vs Number of Threads for Different Division Methods\n {os.path.basename(image)} ({filter_name})")
    division_methods = set(entry["division_method"] for entry in image_data)
    for division_method in division_methods:
        division_data = [(entry["threads"], entry["time_ms"]) for entry in image_data if entry["division_method"] == division_method]
        threads, times = zip(*division_data)
        
        threads = (threads[0], threads[3], threads[6])
        times = (mean([times[0], times[1], times[2]]),
                 mean([times[3], times[4], times[5]]),
                 mean([times[6], times[7], times[8]]))
        
        plt.plot(threads, times, label=division_method)
    plt.xlabel("Number of threads")
    plt.ylabel("Time (ms)")
    plt.legend()
    plt.grid(True)

    plt.xticks(threads)

    output_file = os.path.join(output_dir, f"{os.path.splitext(os.path.basename(image))[0]}_{filter_name}_plot.png")
    plt.savefig(output_file)
    plt.close()