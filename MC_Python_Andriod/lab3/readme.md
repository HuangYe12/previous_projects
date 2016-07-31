
# References
 - [Google Beacons Platform Overview][1]
 - [Get Started with Beacons][2]
 - [Specification for Eddystone, an open beacon format from Google][3]
 - [Eddystone Protocol Specification][4]
 - [Android Services][5]
 - [Android - Services][6] example
 - [Android RPC example - Client / Server with service][7]
 - [Android GPS, Location Manager Tutorial][8]
 - [Using the Emulator Console][9]

# Method to compute distance
```
private double distanceFromRssi(int rssi, int txPower0m) {
  int pathLoss = txPower0m - rssi;
  return Math.pow(10, (pathLoss - 41) / 20.0);
}
```

# Sample data
## UID
```
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  -17
I/lab3_task1_team05( 3735):  33
I/lab3_task1_team05( 3735):  -125
I/lab3_task1_team05( 3735):  -88
I/lab3_task1_team05( 3735):  119
I/lab3_task1_team05( 3735):  -89
I/lab3_task1_team05( 3735):  -73
I/lab3_task1_team05( 3735):  93
I/lab3_task1_team05( 3735):  7
I/lab3_task1_team05( 3735):  21
I/lab3_task1_team05( 3735):  19
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  1
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
```

## URL

```
I/lab3_task1_team05( 3735):  16
I/lab3_task1_team05( 3735):  -17
I/lab3_task1_team05( 3735):  3
I/lab3_task1_team05( 3735):  103
I/lab3_task1_team05( 3735):  111
I/lab3_task1_team05( 3735):  111
I/lab3_task1_team05( 3735):  46
I/lab3_task1_team05( 3735):  103
I/lab3_task1_team05( 3735):  108
I/lab3_task1_team05( 3735):  47
I/lab3_task1_team05( 3735):  79
I/lab3_task1_team05( 3735):  103
I/lab3_task1_team05( 3735):  75
I/lab3_task1_team05( 3735):  121
I/lab3_task1_team05( 3735):  103
I/lab3_task1_team05( 3735):  85
```


## TLM
```
I/lab3_task1_team05( 3735):  32
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  9
I/lab3_task1_team05( 3735):  -5
I/lab3_task1_team05( 3735):  24
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  61
I/lab3_task1_team05( 3735):  113
I/lab3_task1_team05( 3735):  0
I/lab3_task1_team05( 3735):  2
I/lab3_task1_team05( 3735):  102
I/lab3_task1_team05( 3735):  106
```



# Useful commands
  - alf + f1 to show console window
  - alf + f7 to show GUI
  - `netcfg` to show network status in console window within virtual machine
  - `dhcpcd eth0` to get ip address within VM
  - `adb tcpip 5555` within VM

  - On host machine `cd lab2/lab2_task1/app/build/outputs/apk`

```
cd /Users/fangjun/Documents/bitbucket/mobile-computing-lab/lab3/lab3_task1/app/build/outputs/apk
adb connect 192.168.56.101:5555
adb uninstall de.uni_stuttgart.ipvs.mclab.team05.lab3_task1
adb install app-debug.apk
adb logcat lab3_task1_team05:i *:S
```
# GPS
```
telnet localhost 5554

java -jar androidgpsfeeder.jar track.nmea localhost 5554
```




[9]: https://developer.android.com/studio/run/emulator-commandline.html
[8]: http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial/
[7]: https://gist.github.com/tigerjj/9762586
[6]: http://www.tutorialspoint.com/android/android_services.htm
[5]: https://developer.android.com/guide/components/services.html
[4]: https://github.com/google/eddystone/blob/master/protocol-specification.md
[3]: https://github.com/google/eddystone
[2]: https://developers.google.com/beacons/get-started
[1]: https://developers.google.com/beacons/overview#components









..
