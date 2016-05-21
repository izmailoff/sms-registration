#!/bin/bash

# Runs the service in PRODUCTION mode.
# This means all features will be enabled, i.e sending SMS, emails, etc.

java -Drun.mode=Production -server -jar rest-assembly-0.0.1.jar
