TOMCAT SETUP

* Edit /etc/default/tomcat7 - set memory and -Drefset.config in JAVA_OPTS
** -Xmx4096 may be needed for SNOMED spanish release.
* Put a config.properties into /var/lib/tomcat7/conf and link to it from ~/refset/config
** make sure the file is readable/writeable by tomcat7 process
* When running admin tools, make sure indexes directory is writeable by ihtsdo user
* after finishing, make sure the index files are writeable by all (E.g. tomcat7 user)
** each time "generate sample data" is run, this needs to happen again
** chmod -R a+w /var/lib/tomcat7/indexes/refset/*
* Make sure to "chmod a+rx /var/log/tomcat7" so that access to "catalina.out" as ihtsdo user is possible
* Make sure the /tmp/tomcat7-tomcat7-tmp is owned by tomcat7 user (for file uploads)

SETUP

mkdir ~/refset
cd ~/refset
mkdir config data
git clone https://git.ihtsdotools.org/ihtsdo/ihtsdo-refset-management-service.git code

cd ~/refset/code
git pull
mvn -Dconfig.artifactId=refset-config-uat clean install

# unpack sample data
cd ~/refset/code
unzip ~/refset/code/config/target/refset*.zip -d ~/refset/data

# unpack config and scripts
cd ~/refset
unzip ~/refset/code/config/uat/target/refset*.zip -d config
ln -s config/bin
# note these settings
# > javax.persistence.jdbc.password=otfpwd
# > workflow.action.handler.DEFAULT.path=DEFAULT
# > identifier.assignment.handler.DEFAULT.userName=bcarlsen
# > identifier.assignment.handler.DEFAULT.password=Sn0m3dCT
# > security.handler.DEFAULT.users.admin=admin,admin1,admin2,admin3
# > security.handler.DEFAULT.users.viewer=guest,guest1,author1,reviewer1,guest2,author2,reviewer2,guest3,author3,reviewer3
# > mail.smtp.user=***REMOVED***
# > mail.smtp.password=1H7D50Sn0m3d
# >  mail.smtp.host=auth.smtp.1and1.co.uk
# > mail.smtp.starttls.enable=true
# > mail.smtp.auth=true
# > mail.smtp.to=***REMOVED***
cp config/config.properties config/config-load.properties
# may need to be done by root:
cp config.properties /var/lib/tomcat7/conf
chmod a+rw /var/lib/tomcat7/conf/config.properties
# edit config.properties (use ims security)
# edit config-load.properties (use default security)

# run load script (after creating DB)
cd ~/refset
echo "CREATE database refsetdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlotf
bin/load.csh

# Check QA after the load
cd ~/refset/code/admin/qa
mvn install -PDatabase -Drun.config.refset=/home/ec2-tomcat/refset/config/config-load.properties


# edit /etc/tomcat7/tomcat7.conf file
# add a -Drun.config.refset property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/refset/config/config.properties file

REINDEX INSTRUCTIONS

cd ~/refset/code/admin
mvn install -PReindex -Drefset.config/home/ihtsdo/refset/config/config-load.properties >&! mvn.log &

PATCH INSTRUCTIONS

cd ~/refset/code/admin
mvn install -PUpdatedb -Drefset.config/home/ihtsdo/refset/config/config-load.properties >&! mvn.log
set patchStart=20161201
set patchEnd=20161215
mvn install -PPatch -Drefset.config/home/ihtsdo/refset/config/config-load.properties -Dstart=$patchStart -Dend=$patchEnd >&! mvn.log &


REDEPLOY INSTRUCTIONS

# run as ihtsdo:
cd ~/refset/code
git pull
mvn -Dconfig.artifactId=refset-config-uat clean install


# run as root:
sudo su - root
service tomcat7 stop
/bin/rm -rf /var/lib/tomcat7/work/Catalina/localhost/refset-rest
/bin/rm -rf /var/lib/tomcat7/webapps/refset-rest
/bin/rm -rf /var/lib/tomcat7/webapps/refset-rest.war
/bin/cp -f /home/ihtsdo/refset/code/rest/target/refset-rest*war /var/lib/tomcat7/webapps/refset-rest.war
service tomcat7 start
chmod -R 777 /tmp/tomcat7-tomcat7-tmp
chown -R tomcat7 /var/lib/tomcat7/indexes/refset/*
chmod -R 777 /var/lib/tomcat7/indexes/refset/*

CHANGE DATABASE

# If there were database/schema changes, reload sample data (run as ihtsdo)
cd ~/refset/bin
./load.csh

# OR If there were database/schema changes, but the db needs to be kept intact, and index permissions
cd /home/ihtsdo/refset/code/admin
mvn -PUpdatedb install -Drefset.config=/home/ihtsdo/refset/config/config-load.properties
chmod -R 777 /var/lib/tomcat7/indexes/refset/*



## REMEMBER that config.properties is a link to /var/lib/tomcat7/conf/config.properties

# to watch queries
mysql> set global general_log = 1;
% tail -f /var/lib/mysql/uat-refset.log
