Breakpad Server
---------------

a Java web application for receiving Breakpad crash reports


Installation
------------

1. In the root directory, type `ant` to build `crashreportserver.war` in the `dist` subdirectory.

2. Put `crashreportserver.war` in your servlet container (Tomcat or other), with values for 3 parameters: 

    * mail.recipient
    * mail.smtp.host
    * crash.reports.dir


Configuration Example
---------------------

On my gentoo host in `/etc/tomcat-6/Catalina/crashreport.haystacksoftware.com` I created a file ROOT.xml containing:

    <?xml version="1.0" encoding="UTF-8"?>
    <Context docBase="/home/tomcat/production/crashreportserver.war">
        <Parameter name="mail.recipient" value="stefan@haystacksoftware.com" override="false"/>
        <Parameter name="mail.smtp.host" value="mail.reitshamer.com" override="false"/>
        <Parameter name="crash.reports.dir" value="/tmp/crashreports" override="false"/>
    </Context>

