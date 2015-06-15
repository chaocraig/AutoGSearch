#!/usr/bin/expect 

set userName user
set pass user
set command reboot

set timeout 20
spawn telnet 192.168.1.1
expect "Login:"
send "$userName\r"
expect "Password:"
send "$pass\r"
expect ">"
send "$command\r"
interact
