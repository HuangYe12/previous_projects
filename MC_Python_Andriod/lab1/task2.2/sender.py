#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import sys
import datetime

from myConfig import *

gStartTime = 0 # start time
gNumBits = 0 # number of bits sent so far

def init():
    # Use BCM numbering
    GPIO.setmode(GPIO.BCM)

    GPIO.setup(SENDER_PORT_1, GPIO.OUT)
    GPIO.output(SENDER_PORT_1, GPIO.LOW)

    GPIO.setup(SENDER_PORT_2, GPIO.OUT)
    GPIO.output(SENDER_PORT_2, GPIO.LOW)

def reset():
    GPIO.output(SENDER_PORT_1, GPIO.LOW)
    GPIO.output(SENDER_PORT_2, GPIO.LOW)
    GPIO.cleanup()

def sendBit(port, f_bit):
    GPIO.output(port, f_bit)

def sendChar(f_char1, f_char2):
    global gNumBits

    bits1 = bin(ord(f_char1))[2:]
    bits2 = bin(ord(f_char2))[2:]
    print 'sender1 char: ', f_char1
    print 'ascii code is: ', bits1
    print 'sender2 char: ', f_char2
    print 'ascii code is: ', bits2

    mask = [64, 32, 16, 8, 4, 2, 1];
    val1 = ord(f_char1)
    val2 = ord(f_char2)
    for m in mask:
        print 'm&val1 = ', m & val1
        print 'm&val2 = ', m & val2
        b1 = int ((val1 & m) != 0)
        b2 = int ((val2 & m) != 0)
        for i in range(2):
            if b1 == 1:
                sendBit(SENDER_PORT_1, CODE_1[i])
                print datetime.datetime.now().time(), 'sender1=', CODE_1[i]
            else:
                sendBit(SENDER_PORT_1, CODE_1[i] ^ 1)
                print datetime.datetime.now().time(), 'sender1=', CODE_1[i] ^ 1

            if b2 == 1:
                sendBit(SENDER_PORT_2, CODE_2[i])
                print datetime.datetime.now().time(), 'sender2=', CODE_2[i]
            else:
                sendBit(SENDER_PORT_2, CODE_2[i] ^ 1)
                print datetime.datetime.now().time(), 'sender2=', CODE_2[i] ^ 1

            if gNumBits == 0 or gNumBits % gSynBits != 0:
                time.sleep(BIT_DURATION)
            else:
                print "current time is", time.time()
                print "number of bits sent so far", gNumBits
                nextBitAbsTime = gStartTime + (gNumBits + 1) * BIT_DURATION
                print "nextBitAbsTime = ", nextBitAbsTime
                nextDelay = nextBitAbsTime - time.time()
                print "nextDelay is", nextDelay

                time.sleep(nextDelay)

            gNumBits = gNumBits + 1

def main():
    global gStartTime
    init()
    string1 = "HELLO FROM SENDER 1"
    indexSender1 = 0
    string2 = "hello from sender 2"
    indexSender2 = 0

    print datetime.datetime.now().time(), "sending start flag"
    # send starting bit
    gStartTime = time.time() + DELAY

    sendBit(SENDER_PORT_1, 1)
    sendBit(SENDER_PORT_2, 1)
    time.sleep(DELAY)

    while True:
        print datetime.datetime.now().time(), "send character"
        sendChar(string1[indexSender1], string2[indexSender2])
        indexSender1 = (indexSender1 + 1) % len(string1)
        indexSender2 = (indexSender2 + 1) % len(string2)

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        reset()
        print 'Catch ctrl + C! Exit now'
        sys.exit(0)
