Breakpad Server
---------------

a Java web application for receiving crash reports from Breakpad <http://google-breakpad.googlecode.com>


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

Generating Crash Reports
------------------------
See <http://www.reitshamer.com/?p=18> for tips on integrating Breakpad into an OS X application.

When the application crashes, it'll send a "minidump" file to your
crashreportserver. If you've specified `BreakpadLogFileTailSize` in your app's
Info.plist, it'll also send a log file (in a separate HTTP request) to your
crashreportserver.

The crashreportserver will send you an email message with the minidump file
attached. If you've specified `BreakpadLogFileTailSize` it'll send a separate
email with the log file snippet.


Processing Crash Reports
------------------------

First, build Breakpad's `crash_report` utility -- the Xcode project is in google-breakpad/src/tools/mac.

Once you receive a minidump file, use `crash_report` to read it:

    crash_report upload_file_minidump 


