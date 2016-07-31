# DSR test
 - from 32 ---> 111


# Topology (latest)
### neighbor list of 192.168.24.31:
  - 192.168.24.32 31ms

### neighbor list of 192.168.24.32:
  - 192.168.24.31 58ms
  - 192.168.24.33 24ms

### neighbor list of 192.168.24.33:
 - 192.168.24.111 18ms
 - 192.168.24.32  26ms
 - 192.168.24.121 20ms

### neighbor list of 192.168.24.111:
 - 192.168.24.121 16ms
 - 192.168.24.33  19ms

### neighbor list of 192.168.24.121:
 - 192.168.24.111 11ms
 - 192.168.24.33  21ms


# Topology (old)

|  | vspi31 (192.168.24.31) | vspi32 (192.168.24.32) | vspi33 (192.168.24.33) | 129.69.210.197 (192.168.24.111) | 192.168.213.30 (192.168.24.121) |
|:-------------------------------:|:----------------------:|:----------------------:|:----------------------:|:-------------------------------:|:-------------------------------:|
| vspi31 (192.168.24.31) | - | Yes | No | No | No |
| vspi32 (192.168.24.32) | Yes | - | Yes | No | No |
| vspi33 (192.168.24.33) | No | Yes | - | Yes | Yes |
| 129.69.210.197 (192.168.24.111) | No | No | Yes | - | Yes |
| 192.168.213.30 (192.168.24.121) | No | No | Yes | Yes | - |

# Tools
 - Use `IntelliJ IDEA` for Java development instead of Eclipse
 - [Configuring Global, Project and Module SDKs][1]
 - [Error:java: invalid source release: 8 in Intellij. What does it mean?][2]
 - JDK version: `1.7`
 - `java -jar lab4_task1.jar`
 - [How to build jars from IntelliJ properly?][3]

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home
```


# Run script on remote server
```
sh team5@vspi31 "java -jar ~/lab4-task1/lab4_task1.jar"
```

# Machines
 - vspi31
   - wlan ip (wlan0): `192.168.24.31`
   - lan ip: `129.69.210.214`
 - vspi32
   - wlan ip (wlan0): `192.168.24.32`
   - lan ip: `129.69.210.215`
 - vspi33
   - wlan ip (wlan0): `192.168.24.33`
   - lan ip: `129.69.210.216`
 - 129.69.210.197
   - wlan ip 1 (wlan0): `192.168.24.111`
   - wlan ip 2 (wlan1): `192.168.25.111`
   - lan ip: `129.69.210.197`
 - 192.168.213.30
   - wlan ip 1 (wlan0): `192.168.24.121`
   - wlan ip 2 (wlan1): `192.168.25.121`
   - lan ip: `192.168.213.30`

# Useful commands
## Generate SSH key
```
ssh-keygen -t rsa
```
 - then enter filename `marvin-38` and it will create  `marvin-38.pub` and `marvin-38`

```
ssh-copy-id -i ~/.ssh/marvin-38 user@marvin.informatik.uni-stuttgart.de

alias goto_marvin="ssh -i ~/.ssh/marvin-38 user@marvin.informatik.uni-stuttgart.de"
```


## settings for Mobile computing lab
```
ssh-copy-id -i ~/.ssh/mclab team5@vspi31
ssh-copy-id -i ~/.ssh/mclab team5@vspi32
ssh-copy-id -i ~/.ssh/mclab team5@vspi33
ssh-copy-id -i ~/.ssh/mclab team5@129.69.210.197
ssh-copy-id -i ~/.ssh/mclab team5@192.168.213.30

ssh team5@vspi31
alias goto_vspi31="ssh -i ~/.ssh/mclab team5@vspi31"
alias goto_vspi32="ssh -i ~/.ssh/mclab team5@vspi32"
alias goto_vspi33="ssh -i ~/.ssh/mclab team5@vspi33"
alias goto_vspi197="ssh -i ~/.ssh/mclab team5@129.69.210.197"
alias goto_vspi30="ssh -i ~/.ssh/mclab team5@192.168.213.30"
```


## Copy file
```
scp -i ~/.ssh/marvin-38 user@remotehost:foobar.txt /some/local/directory
scp -i ~/.ssh/marvin-38 ~/kfj-env.sh user@marvin.informatik.uni-stuttgart.de:~/
```

## Copy directory
```
scp -i ~/.ssh/marvin-38 -r foo user@remotehost:/some/remote/directory/bar
```


[3]: http://stackoverflow.com/questions/1082580/how-to-build-jars-from-intellij-properly
[2]: http://stackoverflow.com/questions/25878045/errorjava-invalid-source-release-8-in-intellij-what-does-it-mean
[1]: https://www.jetbrains.com/help/idea/2016.1/configuring-global-project-and-module-sdks.html
