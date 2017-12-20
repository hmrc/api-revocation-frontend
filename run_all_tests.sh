#!/bin/bash

sbt clean compile coverage test acceptance:test coverageReport
