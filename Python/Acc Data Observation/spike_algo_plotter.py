from turtle import color
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

plot_type = input('What type of plot? Single (S) or Multiple (M) or count spikes (CS): ')

# matrix = pd.read_csv('AccDataFile2022-02-01181429(Down).csv')
# matrix = pd.read_csv('AccDataFile2022-02-01184116(Up).csv')
# matrix = pd.read_csv('AccDataFile2022-02-01181805(Normal).csv')
matrix = pd.read_csv('./Datasets/AccDataFile2022-02-01183529(Normal).csv')
# matrix = pd.read_csv('AccDataFile2022-02-09184438(Normal).csv')

# matrix = pd.read_csv('General/AccDataFile1.csv')
# matrix = pd.read_csv('Casual/AccDataFileMe4.csv')
# matrix = pd.read_csv('Casual/AccDataFileDad.csv')
# matrix = pd.read_csv('3_steps/AccDataFile.csv')

X = matrix.iloc[:,[0]].values
Y = matrix.iloc[:,[1]].values
Z = matrix.iloc[:,[2]].values
Acc = matrix.iloc[:,[3]].values
#TS_i = matrix.iloc[:,[4]].values[0]
#TS = (matrix.iloc[:,[4]].values - TS_i)/100000000
TS = matrix.iloc[:,[4]].values

def get_avg(accData):
    sum = 0
    for a in accData:
        sum = sum+a
    return sum/len(accData)

def convert_ts(list_TS):
    init = float(list_TS[0])
    print(init)
    for idx,ts in enumerate(list_TS):
        ts = float(ts)
        #print(ts,init,ts-init)
        list_TS[idx] = float((ts-init)/1000.0)
    print(list_TS)
    return list_TS

def count_spikes(list,TS):
    
    std_spike_val = 8
    diff_ts_spike = 1.0
    prev_spike_ts = 0.0
    counter = 0
    for idx,val in enumerate(list):
        if val >= std_spike_val and TS[idx] - prev_spike_ts >= diff_ts_spike:
            counter+=1
            prev_spike_ts = TS[idx]
        
    print(f'Number of spikes: {counter}')

def plot_one_plot():
    val = input('Which section? (X,Y,Z,Acc): ')
    if val == 'X':
        plt.plot(TS,X,color="red",linewidth=1.0)
    elif val == 'Y':
        plt.plot(TS,Y,color="red",linewidth=1.0)
    elif val == 'Z':
        plt.plot(TS,Z,color="red",linewidth=1.0)
    else:
        plt.axhline(y=12.5)
        plt.axhline(y=10)
        plt.scatter(TS,Acc,s=10,color="blue")
        plt.plot(TS,Acc,color="red",linewidth=1.0)
    plt.xlabel('Time Stamp')
    plt.ylabel('acceleration in m/s (Gravity Incl)')


def plot_sub_plots():
    figure, axis = plt.subplots(2, 2)

    axis[0,0].plot(TS,X,color="red",linewidth=1.0)
    axis[0,0].set_title("WRT X Values")

    axis[0,1].plot(TS,Y,color="blue",linewidth=1.0)
    axis[0,1].set_title("WRT Y Values")

    axis[1,0].plot(TS,Z,color="yellow",linewidth=1.0)
    axis[1,0].set_title("WRT Z Values")

    axis[1,1].plot(TS,X,color="red",linewidth=1.0)
    axis[1,1].plot(TS,Y,color="blue",linewidth=1.0)
    axis[1,1].plot(TS,Z,color="yellow",linewidth=1.0)
    axis[1,1].plot(TS,Acc,color="grey",linewidth=1.0)
    axis[1,1].set_title("WRT X,Y,Z,ACC Values")

if TS[0] > 100000000:
    TS = convert_ts(TS)
if plot_type == 'S':
    plot_one_plot()
elif plot_type == 'M': 
    plot_sub_plots()
else:
    count_spikes(Acc,TS=TS)

plt.show()


# matrix = matrix.iloc[:,:].values
# print(matrix)

# for i in range(len(matrix)):
#     plt.scatter(matrix[i][4],matrix[i][0],s=5,c='red',label='X Values')
#     plt.scatter(matrix[i][4],matrix[i][1],s=5,c='blue',label='Y Values')
#     plt.scatter(matrix[i][4],matrix[i][2],s=5,c='yellow',label='Z Values')
#     plt.scatter(matrix[i][4],matrix[i][3],s=5,c='green',label='Acc Values')

# plt.title('Accelerometer trends')

