#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set REFSET_CODE=~/refset/code
set REFSET_CONFIG=~/refset/config/config.properties
set REFSET_DATA=~/refset/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "REFSET_CODE = $REFSET_CODE"
echo "REFSET_DATA = $REFSET_DATA"
echo "REFSET_CONFIG = $REFSET_CONFIG"
echo "SERVER = $SERVER"

echo "    Generate sample data ...`/bin/date`"
cd $REFSET_CODE/admin
mvn install -PSample2 -Drefset.config=$REFSET_CONFIG -Dmode=create >&! mvn.log
if ($status != 0) then
    echo "ERROR running generating sample data"
    cat mvn.log
    exit 1
endif

# set permissions
chmod -R 777 /var/lib/tomcat7/indexes/refset

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
