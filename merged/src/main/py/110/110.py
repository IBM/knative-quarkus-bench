#!/bin/python3

import datetime
from random import sample  
from os import path
from time import time                                                           
import os

from jinja2 import Template

SCRIPT_DIR = path.abspath(path.join(path.dirname(__file__)))

def handler():

    # start timing
    beginTime = datetime.datetime.now()
    name = 'testname'
    size = 1000
    cur_time = datetime.datetime.now()
    random_numbers = sample(range(0, 1000000), size)
    template = Template( open(path.join(SCRIPT_DIR, '110template.html'), 'r').read())
    html = template.render(username = name, cur_time = cur_time, random_numbers = random_numbers)
    # end timing
    endTime = datetime.datetime.now()
    processTime = (endTime - beginTime)
    # dump stats 
#    return {'result': html}
    return "###p110,"+"%.03f"%(processTime.seconds+processTime.microseconds/1000000.0)+","+str(len(html))

retval = handler()
print (retval)




