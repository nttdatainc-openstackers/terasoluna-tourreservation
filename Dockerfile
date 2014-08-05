# pull base image.
FROM centos:centos6

RUN yum update -y

# install java
RUN yum install -y java-1.7.0-openjdk

# install git, curl, tar, wget
RUN yum install -y curl wget git tar

#install maven
RUN wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo

RUN yum install -y apache-maven

#RUN ln -s /usr/share/apache-maven/bin/mvn /usr/bin/mvn

# install tomcat7
RUN wget http://apache.mirrors.lucidnetworks.net/tomcat/tomcat-7/v7.0.55/bin/apache-tomcat-7.0.55.tar.gz

RUN tar xzf apache-tomcat-7.0.55.tar.gz

RUN mv apache-tomcat-7.0.55 /usr/local/tomcat7

# install postgresql 9.2
RUN wget http://yum.pgrpms.org/9.2/redhat/rhel-6-x86_64/pgdg-centos92-9.2-6.noarch.rpm

RUN rpm -ivh pgdg-centos92-9.2-6.noarch.rpm

RUN yum install -y postgresql92 postgresql92-server postgresql92-contrib

RUN service postgresql-9.2 initdb

RUN sleep 3

RUN service postgresql-9.2 start && netstat -alntp

# install terasoluna app
WORKDIR /home/

RUN git clone https://github.com/terasolunaorg/terasoluna-tourreservation.git

# add script in image which starts pgsql and terasoluna app
#ADD run_tera.sh /home/

#RUN chmod +x /home/run_tera.sh

RUN > /var/lib/pgsql/9.2/data/pg_hba.conf

RUN echo "local   all             postgres                                trust" >> /var/lib/pgsql/9.2/data/pg_hba.conf

RUN service postgresql-9.2 start && sleep 2 && psql -U postgres --command "ALTER USER postgres with encrypted password 'P0stgres';" && createdb -U postgres tourreserve;

# edit pgsql conf files
RUN echo "listen_addresses='*'" >> /var/lib/pgsql/9.2/data/postgresql.conf

RUN > /var/lib/pgsql/9.2/data/pg_hba.conf

RUN echo "host     all             all             0.0.0.0/32            md5" >> /var/lib/pgsql/9.2/data/pg_hba.conf

RUN echo "host    all             all             ::1/128                 md5" >> /var/lib/pgsql/9.2/data/pg_hba.conf

RUN echo "host    all             all             127.0.0.1/32            md5" >> /var/lib/pgsql/9.2/data/pg_hba.conf

# go to terasoluna repo
WORKDIR /home/terasoluna-tourreservation/terasoluna-tourreservation-initdb/

ENV JAVA_HOME /usr/lib/jvm/java-1.6.0

# create database
RUN service postgresql-9.2 start && mvn sql:execute

WORKDIR /home/terasoluna-tourreservation/

# install jars
RUN mvn -f terasoluna-tourreservation-parent/pom.xml install -Dmaven.test.skip=true

RUN mvn -f terasoluna-tourreservation-env/pom.xml install -Dmaven.test.skip=true

RUN mvn -f terasoluna-tourreservation-domain/pom.xml install -Dmaven.test.skip=true

RUN mvn -f terasoluna-tourreservation-web/pom.xml package -Dmaven.test.skip=true

# copy war file to tomcat
RUN cp terasoluna-tourreservation-web/target/terasoluna-tourreservation-web.war /usr/local/tomcat7/webapps/

# expose 8080 port
EXPOSE 8080

# start pgsql and tomcat server
CMD service postgresql-9.2 start && sleep 2 && ./usr/local/tomcat7/bin/startup.sh && tail -f /usr/local/tomcat7/logs/catalina.out

