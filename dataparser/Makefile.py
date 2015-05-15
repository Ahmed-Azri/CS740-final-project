#!/usr/bin/env python
import itertools
import random
import argparse
import re
import subprocess
import os
import sys
import shlex
import time
import glob
import flowTableParser
from time import localtime, strftime

args = None

def shcmd(cmd, ignore_error=False):
    print 'Doing:', cmd
    ret = subprocess.call(cmd, shell=True)
    print 'Returned', ret, cmd
    if ignore_error == False and ret != 0:
        exit(ret)
    return ret

class cd:
    """Context manager for changing the current working directory"""
    def __init__(self, newPath):
        self.newPath = newPath

    def __enter__(self):
        self.savedPath = os.getcwd()
        os.chdir(self.newPath)

    def __exit__(self, etype, value, traceback):
        os.chdir(self.savedPath)

def ParameterCombinations(parameter_dict):
    """
    Get all the cominbation of the values from each key
    http://tinyurl.com/nnglcs9
    Input: parameter_dict={
                    p0:[x, y, z, ..],
                    p1:[a, b, c, ..],
                    ...}
    Output: [
             {p0:x, p1:a, ..},
             {..},
             ...
            ]
    """
    d = parameter_dict
    return [dict(zip(d, v)) for v in itertools.product(*d.values())]

#########################################################
# Git helper
# you can use to get hash of the code, which you can put
# to your results
def git_latest_hash():
    cmd = ['git', 'log', '--pretty=format:"%h"', '-n', '1']
    proc = subprocess.Popen(cmd,
                            stdout=subprocess.PIPE)
    proc.wait()
    hash = proc.communicate()[0]
    hash = hash.strip('"')
    print hash
    return hash

def git_commit(msg='auto commit'):
    shcmd('git commit -am "{msg}"'.format(msg=msg),
            ignore_error=True)

########################################################
# table = [
#           {'col1':data, 'col2':data, ..},
#           {'col1':data, 'col2':data, ..},
#           ...
#         ]
def table_to_file(table, filepath, freshfile, adddic=None):
    'save table to a file with additional columns'
    if freshfile:
        mode = 'w'
    else:
        mode = 'a'

    colnames = get_all_colnames(table)

    with open(filepath, mode) as f:
        if adddic != None:
            colnames += adddic.keys()
        colnamestr = ','.join(colnames) + '\n'
        if freshfile:
            f.write(colnamestr)
        for row in table:
            if adddic != None:
                rowcopy = dict(row.items() + adddic.items())
            else:
                rowcopy = row
            rowstr = [ rowcopy[k] if rowcopy.has_key(k) else '' for k in colnames ]
            rowstr = [str(x) for x in rowstr]
            rowstr = ','.join(rowstr) + '\n'
            f.write(rowstr)
            f.flush()
            os.fsync(f.fileno())

def get_all_colnames(table):
    names = set()
    for row in table:
        names = names.union(row.keys())

    return list(names)

def dump_table_to_list():
    cmd = 'sudo ovs-ofctl dump-flows br0'.split()
    proc = subprocess.Popen(cmd, stdout=open('/tmp/_flowtmp','w'))
    proc.wait()

    f = open('/tmp/_flowtmp','r')
    return f.readlines()

def append_table_to_file(fpath):
    fresh = True
    while True:
        linelist = dump_table_to_list()
        tab = flowTableParser.text2table(linelist)
        table_to_file(tab, fpath, fresh)
        fresh = False

        sleeptime = 5
        print 'sleeping for ', sleeptime, 'seconds'
        time.sleep(sleeptime)

def append_raw_to_file(fpath):
    fresh = True
    batchid = 0
    while True:
        linelist = dump_table_to_list()
        if fresh:
            mode = 'w'
        else:
            mode = 'a'
        curtime = strftime("%Y-%m-%d-%H-%M-%S", localtime())
        linelist = [line.strip() + ' curtime=' + curtime + ' batchid=' + str(batchid)
                    for line in linelist]
        batchid += 1
        f = open(fpath, mode)
        f.write('\n'.join(linelist))
        f.flush()
        os.fsync(f.fileno())
        f.close()
        fresh = False

        sleeptime = 5
        print 'sleeping for ', sleeptime, 'seconds'
        time.sleep(sleeptime)

def debug2():
    fresh = True
    with open('./zihao2.txt', 'r') as f:
        linelist = f.readlines()
        if fresh:
            mode = 'w'
        else:
            mode = 'a'
        f = open('tmp.txt', mode)
        f.write(''.join(linelist))
        f.flush()
        os.fsync(f.fileno())
        f.close()
        fresh = False

def parse_raw_files():
    # filelist = ['./benchmark_read_128_files_1KB.txt']
    # filelist = ['./test-benchmark/benchmark_write_128_files_1MB.txt']
    # filelist = glob.glob('./benchmark2/bench*')
    #filelist = glob.glob('./data/ben*')
    filelist = glob.glob('./benchmark4/rep*')
    for fpath in filelist:
        parse_single_file(fpath)

def parse_single_file(fpath):
    with open(fpath, 'r') as f:
        linelist = f.readlines()
        tab = flowTableParser.text2table(linelist)
        tab = [row for row in tab if row['n_packets'] != '0']
        table_to_file(tab, fpath+'.parsed', freshfile=True)

def main():
    #function you want to call
    #print 'hello'
    # append_table_to_file('./mylog.txt')
    # append_table_to_file(args.log)
    append_raw_to_file(args.log)

def _main():
    global args
    parser = argparse.ArgumentParser(
            description="This file hold command stream." \
            'Example: python Makefile.py doexp1 '
            )
    parser.add_argument('-t', '--target', action='store')
    parser.add_argument('-l', '--log', action='store')
    args = parser.parse_args()

    if args.target == None:
        main()
    else:
        # WARNING! Using argument will make it less reproducible
        # because you have to remember what argument you used!
        targets = args.target.split(';')
        for target in targets:
            eval(target)

if __name__ == '__main__':
    _main()




