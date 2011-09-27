simple web application to verify valid UTS account in order to download ytex umls archive.

run "mvn compile war:war" to get all the dependencies for this project.

make sure root file system isn't wiped out on reboot:
https://forums.aws.amazon.com/thread.jspa?threadID=58650

modify instance so that it doesn't kill the root file system when the ami is shutdown
ec2-modify-instance-attribute i-08956568 -b "/dev/sda1=::false"

copy umls files to /home/ec2-user/ytex-umls

add following tomcat parameters:
-Dcas.service.host=ytex-umlsdownload.elasticbeanstalk.com -Dumls.download.dir=/home/ec2-user/ytex-umls

convert key file to der format
"c:\Program Files (x86)\OpenSSL-Win32\bin\openssl.exe"  pkcs8 -topk8 -nocrypt -in pk-APKAIZ5VVLI47TKWYQUQ.pem -inform PEM -out pk-APKAIZ5VVLI47TKWYQUQ.der -outform DER