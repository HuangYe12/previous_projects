#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import sys
from datetime import datetime

from myConfig import *

def init():
    # Use BCM numbering
    GPIO.setmode(GPIO.BCM)

    GPIO.setup(SENDER_PORT_1, GPIO.OUT)
    sendBit(SENDER_PORT_1, 0)

    # set port 24 to low !!
    GPIO.setup(SENDER_PORT_2, GPIO.OUT)
    sendBit(SENDER_PORT_2, 0)

def reset():
    GPIO.output(SENDER_PORT_1, GPIO.LOW)
    GPIO.output(SENDER_PORT_2, GPIO.LOW)
    GPIO.cleanup()

def sendBit(port, f_bit):
    GPIO.output(port, f_bit)
    time.sleep(BIT_DURATION)

def sendChar(port, f_char):
    # send starting bit
    sendBit(port, 1)

    bits = bin(ord(f_char))[2:]
    print 'send char: ', f_char
    print 'ascii code is: ', bits

    mask = [64, 32, 16, 8, 4, 2, 1];
    val = ord(f_char)
    for m in mask:
        print 'm&val = ', m&val
        b = (val & m) != 0
        sendBit(port, int(not b))

    # send stop bit (2 stop bits)
    sendBit(port, 0)
    sendBit(port, 0)
    print ' '

def sendString(port, f_str, index, numberLetters):
    for i in range(numberLetters):
        sendChar(port, f_str[(index + i) % len(f_str)])

def main():
    init()

    string1 = "HELLO FROM SENDER 1"
    string2 = "hello from sender 2"

    indexSender1 = 0
    indexSender2 = 0

    while True:
        sendString(SENDER_PORT_1, string1, indexSender1, NUMBER_LETTERS_SENDER_1)
        indexSender1 = (indexSender1 + NUMBER_LETTERS_SENDER_1) % len(string1)

        sendString(SENDER_PORT_2, string2, indexSender2, NUMBER_LETTERS_SENDER_2)
        indexSender2 = (indexSender2 + NUMBER_LETTERS_SENDER_2) % len(string2)

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        reset()
        print 'Catch ctrl + C! Exit now'
        sys.exit(0)

