#!/bin/tcsh -f
#
# PATCH 20170110
#

# set REFSET_HOME 
set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
setenv REFSET_HOME $abs_rootdir:h

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"
echo "REFSET_HOME = $REFSET_HOME"

echo "Collect settings..."
set host = `grep 'javax.persistence.jdbc.url' $REFSET_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$1"'`
set port = `grep 'javax.persistence.jdbc.url' $REFSET_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$2"'`
set db = `grep 'javax.persistence.jdbc.url' $REFSET_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$3"'`
set user = `grep 'javax.persistence.jdbc.user' $REFSET_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set pwd = `grep 'javax.persistence.jdbc.password' $REFSET_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set mysql = "mysql -h$host -P$port -u$user -p$pwd $db"

echo ""


#
# Patch 20170110
# 1. Updatedb
echo "  Update the database ... `/bin/date`"
cd $REFSET_HOME/code/admin
mvn install -PUpdatedb -Drefset.config=$REFSET_HOME/config/config-load.properties

# 2. Run patch sql
echo "  Run the patch ... `/bin/date`"
$mysql < $REFSET_HOME/code/admin/src/main/resources/patch20170110.sql

# 3. Patch mojo
echo "  Run the patch ... `/bin/date`"
cd $REFSET_HOME/code/admin
mvn install -PPatch -Drefset.config=$REFSET_HOME/config/config-load.properties -Dstart=20170110 -Dend=20170110

# 4. Reindex
echo "  Reindex ... `/bin/date`"
/bin/rm -rf /var/lib/tomcat7/indexes/refset/*
cd $REFSET_HOME/code/admin
mvn install -PReindex -Drefset.config=$REFSET_HOME/config/config-load.properties

echo "--------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "--------------------------------------------------------"



