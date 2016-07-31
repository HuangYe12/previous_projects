#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import sys

from datetime import datetime
from myConfig import *

def readBit(port):
    time.sleep(BIT_DURATION)
    val = GPIO.input(port)
    return val

def readChar(port):
    # disable interrupt
    GPIO.remove_event_detect(port)

    # ignore the leading bit which is always 1
    time.sleep(BIT_DURATION/2.0)

    val = 0
    bits = [0, 0, 0, 0, 0, 0, 0]
    for i in range(7):
        # seven bit per character
        b = readBit(port)
        bits[i] = b
        val = (val << 1) + b

    sys.stdout.write(chr(val))
    sys.stdout.flush()
    # read the stop bit
    #s = readBit(port) # just ignore the 2 stop bits!

    # re-enable interrupt
    GPIO.add_event_detect(RECEIVER_PORT, GPIO.FALLING, callback=readChar)

def init():
    # Use BCM numbering
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(RECEIVER_PORT, GPIO.IN)

    GPIO.add_event_detect(RECEIVER_PORT, GPIO.FALLING, callback=readChar)

def reset():
    GPIO.remove_event_detect(RECEIVER_PORT)
    GPIO.cleanup()

def main():
    init()
    while True:
        time.sleep(1)

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        reset()
        print 'Catch ctrl + C! Exit now'
        sys.exit(0)
