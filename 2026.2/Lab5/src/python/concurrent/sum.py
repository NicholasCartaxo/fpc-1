import os
import sys
import time
import threading

def do_sum_thread(path):
    _sum = do_sum(path)
    print(path + " : " + str(_sum))

def do_sum(path):
    time.sleep(1) # emulating adittional delay of 1 second
    _sum = 0
    with open(path, 'rb',buffering=0) as f:
        byte = f.read(1)
        while byte:
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        return _sum

if __name__ == "__main__":
    paths = sys.argv[1:]

    threads = []

    for i in range(len(paths)):
    #many error could be raised error. we don't care
        x = threading.Thread(target=do_sum_thread, name="sum-"+str(i), args=(paths[i],))
        x.start()
        threads.append(x)
    
    for thread in threads:
        thread.join()
    

