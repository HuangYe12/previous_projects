BIT_DURATION = 0.04 # seconds

RECEIVER_PORT = 17

SENDER_PORT_1 = 23

SENDER_PORT_2 = 24

# number of time slots for sender 1
NUMBER_LETTERS_SENDER_1 = 4

# number of time slots for sender 2
NUMBER_LETTERS_SENDER_2 = 3

'''
For each each character, it has to transmit
1 start bit, 7 data bits, 2 stop bits.

It needs 10 bits for each character.

The length of a time slot is 10bits * 0.04second/bit = 0.4 seconds

'''