#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import sys

from myConfig import *

def init():
    # Use BCM numbering
    GPIO.setmode(GPIO.BCM)

    GPIO.setup(SENDER_PORT_1, GPIO.OUT)

    # set port 24 to low !!
    GPIO.setup(SENDER_PORT_2, GPIO.OUT)

def reset():
    GPIO.output(SENDER_PORT_1, 0)
    GPIO.output(SENDER_PORT_2, 0)
    GPIO.cleanup()

def main():
    init()
    while True:
        for i in range(4):
            GPIO.output(SENDER_PORT_1, i & 1)
            GPIO.output(SENDER_PORT_2, i & 2)
            print "%02d" % i
            time.sleep(DELAY)

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        reset()
        print 'Catch ctrl + C! Exit now'
        sys.exit(0)
