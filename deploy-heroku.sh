#!/bin/bash

time (sbt package && heroku deploy:war --war `find \`pwd\` -type f -name *war` --app LiftDrill)

