#!/bin/bash

# Runs the service in DEVELOPMENT mode.
# This means all additional features will be disabled, i.e sending SMS, emails, etc.

java -Drun.mode=Production -server -jar rest-assembly-0.0.1.jar
