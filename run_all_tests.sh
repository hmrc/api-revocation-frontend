#!/bin/bash

sbt clean compile coverage scalafmtAll scalafixAll test coverageReport