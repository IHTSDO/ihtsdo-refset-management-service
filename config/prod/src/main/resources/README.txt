PROD REFSET SERVER SETUP

1. Use AF starting point (mysql, Tomcat, java, mvn installed)

2. Install software (as root)
  emacs: apt-get install emacs
  git: apt-get install emacs

3. Edit /etc/default/tomcat7 - set memory and -Drefset.config in JAVA_OPTS

JAVA_OPTS="-Djava.awt.headless=true -Xmx3000m -XX:+UseConcMarkSweepGC -Drefset.config=/var/lib/tomcat7/conf/config.properties"

4. Follow "DIRECTORY AND PROJECT SETUP" shown below

5. Copy config file to a location that is accessible by tomcat and link back (as root)

sudo su - root
mv /home/ihtsdo/refset/config/config.properties /var/lib/tomcat7/conf/config.properties
chmod a+rw /var/lib/tomcat7/conf/config.properties
cd /home/ihtsdo/refset/config/
ln -s /var/lib/tomcat7/conf/config.properties

6. Create the database - N/A - "refset" db already created

7. Configure MYSQL - ./etc/mysql/my.cnf  (restart mysql when finished)

max_allowed_packet      = 100M
innodb_file_per_table

8. Download initial prod data and indexes, load prod data

cd ~/refset/data
wget https://s3.amazonaws.com/wci1/IHTSDO/refset-indexes.zip
wget https://s3.amazonaws.com/wci1/IHTSDO/refset-sql.zip
unzip refset-sql.zip
mysql -urefset -p refset < refset.sql

8. Prepare indexes (as root)

sudo su - root
cd /var/lib/tomcat7
mkdir indexes
cd indexes
unzip /home/ihtsdo/refset/data/refset-indexes.zip -d .
mv indexes refset
chown -R tomcat7:tomcat7 /var/lib/tomcat7/indexes/refset
chmod -R 777 /var/lib/tomcat7/indexes/refset

9. Make sure the /tmp/tomcat7-tomcat7-tmp is owned by tomcat7 user (for file uploads)

sudo su - root
chmod a+rw /tmp/tomcat7-tomcat7-tmp

10. nginx configuration

see /etc/nginx/sites-enabled/refset.ihtsdotools.org

11. See the REDEPLOY instructions below


DIRECTORY AND PROJECT SETUP

1. Create space and pull code

mkdir ~/refset
cd ~/refset
mkdir config data
git clone https://git.ihtsdotools.org/ihtsdo/ihtsdo-refset-management-service.git code

2. Build code with proper config

cd ~/refset/code
git pull
mvn -Dconfig.artifactId=refset-config-prod clean install

3. Unpack and edit config

cd ~/refset
unzip ~/refset/code/config/prod/target/refset*.zip -d config

# edit ~/refset/config/config.properties
#  *  javax.persistence.jdbc.url=jdbc:mysql://127.0.0.1:3306/refset?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&useLocalSessionState=true
#  *  javax.persistence.jdbc.username=*******
#  *  javax.persistence.jdbc.password=*******
#  *  hibernate.search.default.indexBase=/var/lib/tomcat7/indexes/refset
#  *  identifier.assignment.handler.DEFAULT.userName=refset-prod
#  *  identifier.assignment.handler.DEFAULT.password=********

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
mvn -Dconfig.artifactId=refset-config-prod clean install


# run as root:
sudo su - root
service tomcat7 stop
/bin/rm -rf /var/lib/tomcat7/work/Catalina/localhost/refset-rest
/bin/rm -rf /var/lib/tomcat7/webapps/refset-rest
/bin/rm -rf /var/lib/tomcat7/webapps/refset-rest.war
/bin/cp -f /home/ihtsdo/refset/code/rest/target/refset-rest*war /var/lib/tomcat7/webapps/refset-rest.war
service tomcat7 start
chmod -R 777 /tmp/tomcat7-tomcat7-tmp
chown -R tomcat7:tomcat7 /var/lib/tomcat7/indexes/refset
chmod -R 777 /var/lib/tomcat7/indexes/refset

