#!/bin/csh -f
# Prep environment for integration testing

# Configure
set REFSET_CODE=/home/ec2-tomcat/refset/code
set REFSET_CONFIG=/home/ec2-tomcat/refset/config/config.properties
set REFSET_DATA=/home/ec2-tomcat/refset/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "REFSET_CODE = $REFSET_CODE"
echo "REFSET_DATA = $REFSET_DATA"
echo "REFSET_CONFIG = $REFSET_CONFIG"
echo "SERVER = $SERVER"
echo ""

echo "    Undeploy server ... `/bin/date`"
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/refset-rest
if ($status != 0) then
    echo "Failed to undeploy app"
    exit 1
endif
/bin/rm -rf /var/lib/tomcat8/webapps/refset-rest
if ($status != 0) then
    echo "Failed to undeploy app"
    exit 1
endif
/bin/rm -rf /var/lib/tomcat8/webapps/refset-rest.war
if ($status != 0) then
    echo "Failed to undeploy app"
    exit 1
endif

echo "    Pull code ...`/bin/date`"
cd $REFSET_CODE
git pull | sed 's/^/      /'
if ($status != 0) then
    echo "Failed to pull project"
    exit 1
endif

echo "    Rebuild code ...`/bin/date`"
mvn -Dconfig.artifactId=refset-config-prod-wci clean install | sed 's/^/      /' >&! /tmp/x.$$
if ($status != 0) then
    cat /tmp/x.$$
    echo "Failed to build maven project"
    exit 1
endif

echo "    Generate sample data ...`/bin/date`"
cd $REFSET_CODE/admin
mvn install -PSample -Drefset.config=$REFSET_CONFIG -Dmode=create | sed 's/^/      /' >&! /tmp/x.$$
if ($status != 0) then
    cat /tmp/x.$$
    echo "ERROR running generating sample data"
    exit 1
endif

echo "    Redeploy ... `/bin/date`"
/bin/cp -f ~/refset/code/rest/target/refset-rest*war /var/lib/tomcat8/webapps/refset-rest.war
if ($status != 0) then
    echo "Failed to deploy app"
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
