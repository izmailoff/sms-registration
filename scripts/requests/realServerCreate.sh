#!/bin/bash

# A script that helps to create sample registration requests.
#
# You can either run it without any args to get
# randomly generated device ID or pass any text as an argument.
#
# Additionally all other arguments except the first one will be
# passed to curl.
#
# Examples:
#
## 1) submit registration request with random device ID and random phone number:
# ./createRegistrationRequest.sh
#
## 2) submit registration request with specific device ID and specific phone number:
# ./createRegistrationRequest.sh "871336476184" "555778137"
#

URL="52.74.82.210/api/users/register"
deviceId="$1"
deviceIdLen=10
phoneNumber="$2"
phoneNumLen=8

if [ -z "$deviceId" ]; then
  deviceId=$(tr -cd '[:alnum:]' < /dev/urandom | fold -w"$deviceIdLen" | head -n1)
else
  shift
fi;

if [ -z "$phoneNumber" ]; then
  phoneNumber=$(tr -cd '[:digit:]' < /dev/urandom | fold -w"$phoneNumLen" | head -n1)
else
  shift
fi;

echo
echo "Requesting registration for device ID [$deviceId] and phone number [$phoneNumber]."
echo

curl \
  -X POST \
  -H "Content-Type: application/json" \
  -d "{ \"deviceId\" : \"$deviceId\", \"phoneNumber\" : \"$phoneNumber\" }" \
  "$URL" \
  "$@"


