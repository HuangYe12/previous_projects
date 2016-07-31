BIT_DURATION = 0.03 # seconds, 0.03 is more reliable than 0.025

DELAY = 1 # time to delay after receiving the start flag

RECEIVER_PORT = 17

SENDER_PORT_1 = 23

SENDER_PORT_2 = 24

# No LED, 1023
LED_0 = 800

# 1 LED, 614 or 588
LED_1 = 500

# chip sequence
SENDER_CODE_1 = [1, 1]
SENDER_CODE_2 = [1, -1]

CODE_1 = [1, 1]
CODE_2 = [1, 0]

gSynBits = 4 # synchronize every ySynBits