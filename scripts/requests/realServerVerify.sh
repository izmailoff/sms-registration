#!/bin/bash

# A script that helps to create sample verification requests.
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

URL="54.251.131.116/api/users/verify"
deviceId="$1"
pin="$2"

if [ -z "$deviceId" ] || [ -z "$pin" ]; then
  echo "Device ID and PIN must be passed to the script!. Exiting..."
  exit 1
else
  shift 2
fi;

echo
echo "Requesting verification for device ID [$deviceId] and PIN [$pin]."
echo

curl \
  -X POST \
  -H "Content-Type: application/json" \
  -d "{ \"deviceId\" : \"$deviceId\", \"pin\" : $pin }" \
  "$URL" \
  "$@"


