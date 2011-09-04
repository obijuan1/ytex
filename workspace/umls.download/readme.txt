simple web application to verify valid UTS account in order to download ytex umls archive.

run "mvn process-sources" to get all the dependencies for this project.

modify instance so that it doesn't kill the root file system when the ami is shutdown
ec2-modify-instance-attribute i-08956568 -b "/dev/sda1=::false"

copy umls files to /home/ec2-user/ytex-umls

add following tomcat parameters:
-Dcas.service.host=ytex-env-piamhv2biy.elasticbeanstalk.com -Dumls.download.dir=/home/ec2-user/ytex-umls
