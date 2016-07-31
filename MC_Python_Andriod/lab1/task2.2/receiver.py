#!/usr/bin/python
import RPi.GPIO as GPIO
import sys
import time
import serial
import datetime

from myConfig import *


ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

gStartTime = 0 # start time
gNumBits = 0 # number of bits received so far


def detectStartFlag():
    '''senders will send a flag to indicate that they are ready to send'''
    global  gStartTime
    print "try to detect start flag"
    ser.flushInput()
    while True:
        line = ser.readline()
        line = line.rstrip()
        val = int(line)
        if val < LED_1:
            # 2 LEDs on
            gStartTime = time.time() + DELAY - BIT_DURATION/2.0
            break

    # ignore the leading synchronization bit
    time.sleep(DELAY - BIT_DURATION/2.0)
    print datetime.datetime.now().time(), "start to receive"

def scalar_product(payload):
    bit1 = payload[0] * SENDER_CODE_1[0] + payload[1] * SENDER_CODE_1[1]
    bit2 = payload[0] * SENDER_CODE_2[0] + payload[1] * SENDER_CODE_2[1]

    bit1 /= 2.0
    bit2 /= 2.0

    bit1 = max(0, bit1)
    bit2 = max(0, bit2)

    return (bit1, bit2)

def receiveBits():
    """receive 2 bits"""
    global gNumBits
    global gSynBits

    if "compensation" not in receiveBits.__dict__:
        # reading serial port takes time, so it needs to compensate the sleep time
        receiveBits.compensation = 0

    val = [0, 0]

    for i in range(2):
        if gNumBits == 0 or gNumBits % (gSynBits) != 0:
            time.sleep(BIT_DURATION - receiveBits.compensation)
        else:
            nextBitAbsTime = gStartTime + (gNumBits + 1) * BIT_DURATION
            nextDelay = nextBitAbsTime - time.time()
            time.sleep(nextDelay)

        ser.flushInput()
        bytesToRead = ser.inWaiting()
        tStart = time.time()
        line = ser.readline()
        tEnd = time.time()
        receiveBits.compensation = tEnd - tStart
        if len(line) == 0:
            print "null value read. Error occurs! Please restart receiver and sender!"

        line = line.rstrip()
        val1 = int(line)
        gNumBits = gNumBits + 1

        if val1 > LED_0:
            # no LED on
            val[i] = -2
        elif val1 > LED_1:
            # 1 LED on
            val[i] = 0
        else:
            # 2 LEDs on
            val[i] = 2

    return val

def receiveChar():
    c1 = 0
    c2 = 0

    for i in range(7):
        bits = receiveBits()
        val = scalar_product(bits)
        c1 = int ((c1 << 1) + int(val[0]))
        c2 = int( (c2 << 1) + int(val[1]))

    return [c1, c2]

def main():
    detectStartFlag()
    while True:
        [c1, c2] = receiveChar()
        sys.stdout.write(chr(c1))
        sys.stdout.write(chr(c2))
        sys.stdout.flush()

if __name__ == '__main__':
    try:
        print ser
        main()
    except KeyboardInterrupt:
        print 'Catch ctrl + C! Exit now'
        ser.close()
        sys.exit(0)