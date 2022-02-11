import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

matrix = pd.read_csv('Datasets\AccDataFile2022-02-09184438(Normal).csv')
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

def findMax():
    max_acc = Acc[0]
    for x in Acc:
        if x > max_acc:
            max_acc = x
    return max_acc

def defindUpperbound(max_a):
    if max_a < 10:
        return 10
    elif max_a < 20:
        return 20
    elif max_a < 30:
        return 30
    elif max_a < 40:
        return 40
    elif max_a < 50:
        return 50
    elif max_a < 60:
        return 60
    elif max_a < 70:
        return 70
    elif max_a < 80:
        return 80
    elif max_a < 90:
        return 90
    else:
        return 100

if TS[0] > 100000000:
    TS = convert_ts(TS)

max_a = findMax()
print("Max Acceleration: ",max_a)

u_bound = defindUpperbound(max_a)
print("U Bound: ",u_bound)

acc_count = [0,0,0,0,0,0,0,0,0,0]
peak_count = [0,0,0,0,0,0,0,0,0,0]
up_bound = u_bound
lo_bound = u_bound-10
while lo_bound >= 0:
    for id,x in enumerate(Acc):
        if x > lo_bound and x <= up_bound:
            acc_count[(int)((up_bound/10)-1)] = acc_count[(int)((up_bound/10)-1)]+1
            if id != 0 and Acc[id-1] < x and Acc[id+1] < x:
                peak_count[(int)((up_bound/10)-1)] = peak_count[(int)((up_bound/10)-1)]+1
    lo_bound = lo_bound-10
    up_bound = up_bound-10
print("Acc Range (m/s)\t Values\t Peaks")
for i in range(10,71,10):
    print(i-10,"->",i,"\t",acc_count[(int)(i/10)-1],"\t",peak_count[(int)(i/10)-1])

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