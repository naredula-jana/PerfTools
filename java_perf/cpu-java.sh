#!/bin/sh
top -H -b -n 1 | grep java > ./top.log
jstack -l $1  > ./jstack.log
