#!/bin/bash

sbt clean compile coverage test coverageReport
