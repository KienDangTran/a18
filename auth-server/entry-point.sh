#!/bin/sh
# vim:set et sw=2 ts=2 tw=120:

export HOSTNAME="$(cat /etc/hostname)"

dockerize java -Duser.timezone="Asia/Ho_Chi_Minh" -jar /app/auth-server-1.0.jar
