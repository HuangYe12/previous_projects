# References
 - [Application Fundamentals][1]
 - [Intents and Intent Filters][2]
 - [Common Intents][3]
 - [Activities][4]
 - [Fragments][5]
 - [Services][6]
 - [Providing Resources][7]
 - [Accessing Resources][8]
 - [String Resources][9]
 - [App Manifest][10]
 - [Input Controls][11]
 - [Bluetooth][12]
 - [Install Android Studio][14]
 - [Building Your First App][13]
 - [Starting an Activity][15]
 - [**Temperature Measurement**][17]


# Code examples
 - [bluetoothgatt][16]


# [Temperature sensor][17]

| bit 7 | bit 6 | bit 5 | bit 4 | bit 3 | bit 2 | bit 1 | bit 0 |           Meaning           |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:---------------------------:|
|       |       |       |       |       |   0   |   0   |       |        flags + value        |
|       |       |       |       |       |   0   |   1   |       |     flags + value + time    |
|       |       |       |       |       |   1   |   0   |       |     flags + value + type    |
|       |       |       |       |       |   1   |   1   |       | flags + value + time + type |
|       |       |       |       |       |   0   |   0   |   0   |     value is in Celsius     |
|       |       |       |       |       |   0   |   0   |   1   |    value is in Fahrenheit   |

# [Humidity Sensor][18]

# Useful commands
 - alf + f1 to show console window
 - alf + f7 to show GUI
 - `netcfg` to show network status in console window within virtual machine
 - `dhcpcd eth0` to get ip address within VM
 - `adb tcpip 5555` within VM

 - On host machine `cd lab2/lab2_task1/app/build/outputs/apk`
```
adb connect 192.168.56.101:5555
adb uninstall de.uni_stuttgart.ipvs.mclab.team05.lab2_task1
adb install app-debug.apk
adb logcat MainActivity:i *:S
```

[18]: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.humidity.xml
[17]: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.temperature_measurement.xml
[16]: https://github.com/devunwired/accessory-samples/tree/master/BluetoothGatt/src/com/example/bluetoothgatt
[15]: http://developer.android.com/training/basics/activity-lifecycle/starting.html
[14]: http://developer.android.com/sdk/installing/index.html
[13]: http://developer.android.com/training/basics/firstapp/index.html
[12]: http://developer.android.com/guide/topics/connectivity/bluetooth.html
[11]: http://developer.android.com/guide/topics/ui/controls.html
[10]: http://developer.android.com/guide/topics/manifest/manifest-intro.html
[9]: http://developer.android.com/guide/topics/resources/string-resource.html
[8]: http://developer.android.com/guide/topics/resources/accessing-resources.html
[7]: http://developer.android.com/guide/topics/resources/providing-resources.html
[6]: http://developer.android.com/guide/components/services.html
[5]: http://developer.android.com/guide/components/fragments.html
[4]: http://developer.android.com/guide/components/activities.html
[3]: http://developer.android.com/guide/components/intents-common.html
[2]: http://developer.android.com/guide/components/intents-filters.html
[1]: http://developer.android.com/guide/components/fundamentals.html
