
2 leds off --- 1023
1 led on, 1 led off --- 692 or 665
2 leds on --- 352

# show default values
ser = serial.Serial()
print ser # show its default values
ser.open() # to open it
ser.is_open() # chekc if it is open
ser.close() # close it


# time needed to read serial port
receive bits...
18:23:51.967525 start to read serial port
18:23:51.975153 finish reading serial port
it used  0.00717806816101 seconds
18:23:51.975925 661
18:23:52.376847 start to read serial port
18:23:52.384123 finish reading serial port
it used  0.0068359375 seconds
18:23:52.384914 1023
[0, -2]
receive bits...
18:23:52.786560 start to read serial port
18:23:52.794291 finish reading serial port
it used  0.00726914405823 seconds
18:23:52.795054 1023
18:23:53.195968 start to read serial port
18:23:53.204422 finish reading serial port
it used  0.0080349445343 seconds
18:23:53.205175 662
[-2, 0]
receive bits...
18:23:53.606810 start to read serial port
18:23:53.614746 finish reading serial port
it used  0.00727891921997 seconds
18:23:53.615838 346
18:23:54.016942 start to read serial port
18:23:54.024913 finish reading serial port
it used  0.00736093521118 seconds
18:23:54.026002 687
[2, 0]
receive bits...
18:23:54.428154 start to read serial port
18:23:54.435145 finish reading serial port
it used  0.0063271522522 seconds
18:23:54.436242 1023
18:23:54.837415 start to read serial port
18:23:54.845353 finish reading serial port
it used  0.00711822509766 seconds
18:23:54.846502 1023
[-2, -2]
receive bits...
18:23:55.248655 start to read serial port
18:23:55.255500 finish reading serial port
it used  0.00617408752441 seconds
18:23:55.256616 1023
18:23:55.657764 start to read serial port
18:23:55.664446 finish reading serial port
it used  0.00584316253662 seconds
18:23:55.665648 1023

# time needed to send bits
18:23:49.941094 sending start flag
18:23:50.943747 send character
sender1 char:  H
ascii code is:  1001000
sender2 char:  h
ascii code is:  1101000
m&val1 =  64
m&val2 =  64
18:23:50.947399 sender1= 1
18:23:50.947935 sender2= 1
18:23:51.349080 sender1= 1
18:23:51.349710 sender2= 0
m&val1 =  0
m&val2 =  32
18:23:51.751748 sender1= 0
18:23:51.752343 sender2= 1
18:23:52.153345 sender1= 0
18:23:52.153876 sender2= 0
m&val1 =  0
m&val2 =  0
18:23:52.555960 sender1= 0
18:23:52.556529 sender2= 0
18:23:52.957525 sender1= 0
18:23:52.958062 sender2= 1
m&val1 =  8
m&val2 =  8
18:23:53.359346 sender1= 1
18:23:53.359497 sender2= 1
18:23:53.760085 sender1= 1
18:23:53.760260 sender2= 0
m&val1 =  0
m&val2 =  0
18:23:54.160987 sender1= 0
18:23:54.161158 sender2= 0
18:23:54.561774 sender1= 0
18:23:54.561947 sender2= 1
