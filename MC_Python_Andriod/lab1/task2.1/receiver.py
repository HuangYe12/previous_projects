#!/usr/bin/python
import RPi.GPIO as GPIO
import sys
import serial

from myConfig import *




def main():
    ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

    while True:
        line = ser.readline()
        line = line.rstrip()
        val = int(line)
        if val > LED_0:
            print "2 LEDs off"
        elif val > LED_1:
            print "1 LED on, 1 LED off"
        else:
            print "2 LEDs on"


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print 'Catch ctrl + C! Exit now'
        sys.exit(0)