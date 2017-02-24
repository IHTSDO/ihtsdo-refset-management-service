#!/bin/tcsh -f
#
# PATCH 20161215
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
# Patch 20161215
# 1. Updatedb
echo "  Update the database ... `/bin/date`"
cd $REFSET_HOME/code/admin
mvn install -PUpdatedb -Drefset.config=$REFSET_HOME/config/config-load.properties

# 2. Patch mojo
echo "  Run the patch ... `/bin/date`"
cd $REFSET_HOME/code/admin
mvn install -PPatch -Drefset.config=$REFSET_HOME/config/config-load.properties -Dstart=20161215 -Dend=20161215

# 3. Reindex
#
echo "  Reindex ... `/bin/date`"
cd $REFSET_HOME/code/admin
mvn install -PReindex -Drefset.config=$REFSET_HOME/config/config-load.properties


echo "--------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "--------------------------------------------------------"



