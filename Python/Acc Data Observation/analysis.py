import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

matrix = pd.read_csv('AccDataFile2022-02-01183529(Normal).csv')
X = matrix.iloc[:,[0]].values
Y = matrix.iloc[:,[1]].values
Z = matrix.iloc[:,[2]].values
Acc = matrix.iloc[:,[3]].values
TS = matrix.iloc[:,[4]].values

def convert_ts(list_TS):
    init = float(list_TS[0])
    print(init)
    for idx,ts in enumerate(list_TS):
        ts = float(ts)
        #print(ts,init,ts-init)
        list_TS[idx] = float((ts-init)/1000.0)
    print(list_TS)
    return list_TS

if TS[0] > 100000000:
    TS = convert_ts(TS)

'''
To DO:
Step 1:
Analyze the time difference between peaks in each 10m/s slots
Step 2:
Consider the time slot/s which are closer to human step time difference for ex 1sec
Step 3: 
Analyze the number of peaks which have closer to the above time difference and stop when the number of peaks 
count exponentially higher than the previous peaks

Step 1 + Step 2: Time interval between two consecutive steps.
Step 3: Minimum threshold acceleration to consider for a step.
'''