
#Clearinghouse Administrator Guide

Version 2.1


Space Dynamics Laboratory

Utah State University Research Foundation

1695 North Research Park Way

North Logan, Utah 84341

![logo](images/sdl.png)


#Overview
-----

The Open Storefront application is a software cataloging system that is used to catalog components
of interest to the DI2E community. Components include Government off
the shelf (GOTS), commercial off the shelf (COTS), and Open Source
software (OSS). The component evaluations done by DI2E's Centers of
Excellence are displayed in the Storefront and give details on the
following:

-   Ownership

-   Where/How to access the software

-   Software vitals

-   Conformance

-   Links to documentation, source code and other artifacts

-   Evaluation information

**Open Storefront is developed by Space Dynamics Laboratory and is
licensed under Apache V2.**

## 1.  Client Architecture
------

##1.1 Client Architecture Diagram

![clientarch](images/client-archtechture-new.png)

Figure 1. Client Architecture Diagram

##1.2 Client Details
-----

The client core structure is based on Ext.js which provides UI components and utilities. This reduces third-part dependencies significantly which in turn reduce mantainance, learning curve and improves quaility and consistency.

Added to that is application specific overrides and high-level components created to facilitate re-use.
The application is simply composed by stripes layouts with top-level page and fragment tool pages.

## 2.  Server Architecture
------

##2.1 Server Architecture Diagram

![serverarch](images/serverarch.png)

Figure 2. Server Architecture Diagram

##2.2 Server Details
-----

Component definitions are as shown below:

  -  **Security**        - Authentication and authorization is delegated to OpenAm. This is configured through a custom realm using the Apache Shiro library. All request are passed through this filter.
  -  **REST API**       - The REST API is the component that handles the data interaction between the clients and provides the interface with which the clients can communicate. The REST API is broken into two sections: resources and services. Resources handle the CRUD operations on the data. Service handle operation across data sets. This provides a clean and clear API for integrators.
  -  **API Docs**       - The API docs are generated live based on the currently running code. This keeps the documents always current and reduces maintenance. Other system related call backs (e.g., retrieving binary resources, login handling, etc.) are handled through the Stripes framework.
  -  **Business Layer**  - Handles all rules applied to the data as well transaction support.
  
>-  **Managers**   - The role of the manager class is to handle the interaction with a resource. This allow for clean initialization and shutdown of resources and provides centralized access.
  -   **Services**    - Each service is in charge of handling a specific group of Entity models. Services provide transaction support and business logic handling. All service are accessed through a service proxy class.  The service proxy class provides auto transaction and service interception support.
  -   **Models**  - The entity models represent the data in the system and provide the bridge from the application to the underlying storage.  
  -   **Import / Export** -The entity models represent the data in the system and provide the bridge from the application to the underlying storage. 


The server build environment relies on the following platforms/tools:

  -  **Java**     -            Core language and platform
  -  **Maven**   -            Used for the project structure, building and dependency management

#3. Runtime Environment
-----

##3.1 Runtime Environment Diagram

![deployarch](images/deployarch.png)

Figure 3 . Runtime Environment diagram

##3.2 Runtime Details
-----

The runtime environment relies upon the following applications:

-  **Proxy Server**   -   This is an external system that proxies requests to the application server.
-  **Tomcat 7**  -    Tomcat is the web container used to host the storefront application.
-  **Java 8**  -            It the runtime platform which runs Tomcat
-  **OS/VM**  -             Is the host machines operating system
-  **Solr**          -    Enterprise search appliance runs externally
-  **OpenAM**    -        OpenAM runs externally and a policy agent in Tomcat make sure the site is secure.

##3.3 Runtime Component Integration Vectors
----

![component vectors](images/civarch.png)

Figure 4. Runtime Component Integration Vectors

##3.4 Component Integration Vectors Details
-----

The component integration vectors (CIV) are show below.

-----

 **Source Component**:  openstorefront           
 **Class**:  C  
 **Target Component**:      Solr/ESA                 
**Notes**

 **Source Component**:  openstorefront           
 **Class**:  C  
 **Target Component**:      OpenAM        
**Notes**: OpenAM with their policy agent; requires a hard tie to the application and the application server

 **Source Component**:  openstorefront           
 **Class**:  B  
 **Target Component**:      Solr/ESA/Elasticsearch                 
**Notes**: JEE Application Server   Currently configured to deploy on Tomcat

 **Source Component**:    Orient DB           
 **Class**:  B  
 **Target Component**:     openstorefront                
**Notes**: Embedded

 **Source Component**:   JEE Application Server           
 **Class**:  A  
 **Target Component**:      OS/VM                 
**Notes**: Currently targeted for CentOS

------

The CIVs represent an integration activity involving a source, Component
A, and a target, Component B.

The CIVs, as defined by the DI2E PMO, are as follows:

-   **Class A: A-deployed On-B**. Component B is the underlying
    environment (providing resources) for A; B does not actively manage
    A (e.g. OS, VM).

-   **Class B: A-contained In-B**. Component "lives in"  B; B manages
    the lifecycle of A, from cradle to grave. (e.g. Widget in OWF; EJB
    in JEE server; OSGi bundle in Karaf; SCA).

-   **Class C: A-interfaces With-B**. Component A initiates
    communication with B via API(s). (e.g., JDBC, JMS, REST/SOAP call,
    legacy communications)

-   **Class D: A-indirectly Consumes-B**. Component A has a dependency
    o.n data/functionality of B even though it doesn't interface with B.
    (e.g. subscriber/publisher relationship; A integrates with another
    component that offers data from B).

##3.5 Ports
-----

The applicable ports are shown below:

-----

**Port (Defaults):**  8080
**Description:** Tomcat HTTP
**Type:** Inbound

**Port (Defaults):**  8009
**Description:** Tomcat AJP
**Type:** Inbound  (Open if not using 8080)

**Port (Defaults):**  2424
**Description:** OrientDB
**Type:** Internal  (Shouldn't be exposed externally)

**Port (Defaults):**  2480
**Description:** OrientDB
**Type:** Internal  (Shouldn't be exposed externally)

**Port (Defaults):**  8983
**Description:** ESA/Solr
**Type:** Outbound (Used internally doesn't need to be exposed outside the system)

**Port (Defaults):**  8080
**Description:** OpenAM running on Tomcat; Setups on this vary so this just represents one case
**Type:** Outbound  (External application)

----

  All ports are configurable via configuration for the respected applications. Additional ports maybe be used depending on configuration.

#4.  Installation
 -----

##4.1  High level instructions for a fresh install
-------------------------------------------

Prior to the install, setup an ESA or Solr instance and make sure it's
running. Then, perform the following steps:

1.  Setup VM

2.  Install Java JDK 1.8

3.  Install Tomcat 7

4.  Integrate OpenAM Agent

5.  Deploy Application

6.  Configure Application

7.  Restart Tomcat (To pick up configuration changes)

8.  Import data

##4.2  Suggested VM Configuration
--------------------------

The following is the recommended VM configuration:

-   CentOS

-   CPUs: 4

-   RAM: 8GB

-   DISK: 40GB (Increase, if storing a lot of media and resources locally)

-   Minimum:

    -   CPUs: 1

    -   RAM: 2GB (Application should be set to use 1GB)

    -   DISK: 20GB

##4.3  Platform Dependencies
---------------------

The Storefront is dependent upon:

-   Java 8

-   Tomcat 7 v50+

##4.4  External Dependencies
---------------------

The Storefront relies upon the following external dependencies:

-   OpenAM (Optional)

-   Index Search Server (Any of the following can work)
	
	-   Solr 6.x + *Recommended for greater control*

	-   Elasticsearch 2.3.x *Recommended for simple install*
	
*Support for ESA 1.0 and Solr 4.3.1 has been dropped*

**NOTE:** The base Solr package will require some changes to the schema.xml to make
sure all field are available.

###4.4.1 To Use Solr

(The following assumes the application is stop prior to the change. To change on a running application you need to restart the SearchServerManager after making changes.)

Download Version 6.x 
from [solr home](http://lucene.apache.org/solr/), and then perform the
following steps:

1. Unpackage  (setup according to solr instructions if setting up OS integration)

2. Add core
-copy server /doc/solr/openstorefront (From source code) to .../solr-6.1.0/server/solr

3.  Start server
bin/solr start -p 8983 

4.  Configure OpenStorefront to point to Solr by going to:
    /var/openstorefront/config/openstorefront.properties

5.  Edit 
	
	search.server=solr

	solr.server.url=http://localhost:8983/solr/openstorefront

	solr.server.usexml=true

6. Resync data 

	a) Nav->Admin->System->Search Control

        b) Click Re-Index Listings

###4.4.1.1 Installing as service linux
run as sudo 

1) ln -s /usr/local/solr/solr-6.1.0 latest

2) cp /usr/local/solr/solr-6.1.0/bin/init.d/solr /etc/init.d

3) nano /etc/init.d/solr

Edit:

> SOLR_INSTALL_DIR="/usr/local/solr/latest"

> SOLR_ENV="/usr/local/solr/latest/bin/solr.in.sh"
 
> SOLR_INCLUDE="$SOLR_ENV" "$SOLR_INSTALL_DIR/bin/solr" "$SOLR_CMD" "$2"

Save and exit

4)  Add User
> chmod 755 /etc/init.d/solr

> chown root:root /etc/init.d/solr

(debian/ubuntu)

> update-rc.d solr defaults

> update-rc.d solr enable

(centos/redhat) 

> chkconfig solr on

> groupadd solr

> useradd -g solr solr

> chown -R solr:solr /usr/local/solr/solr-6.1.0

> chown solr:solr latest

5) (If lsof is not installed)
> yum install lsof

6) (This will start at port 8983)
> service solr start|stop  


###4.4.3 To Use Elasticsearch 

1. Download
	[elasticsearch home]https://www.elastic.co/ (Apache v2 licensed)

2. Start
	<elasticsearch>/bin/elasticsearch

3. Configure OpenStorefront to point to Solr by going to: /var/openstorefront/config/openstorefront.properties or System admin screen

		Add/Set: (adjust as needed to match url and ports)
	
		search.server=elasticsearch

		elastic.server.host=localhost

		elastic.server.port=9300

4. Resync data 

	a) Nav->Admin->System->Search Control

        b) Click Re-Index Listings
  
###4.4.3.1 Yum install of Elasticsearch 

1. Download and install with YUM 
https://www.elastic.co/downloads/elasticsearch (2.3.x) 
(see https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-repositories.html for yum install instructions) 

2. service elasticsearch start 

3. Configure OpenStorefront to point to elastisearch by going to: /var/openstorefront/config/openstorefront.properties or System admin screen->system properties 

4. Add/Set: (adjust as needed to match url and ports) 

search.server=elasticsearch 
elastic.server.host=localhost 
elastic.server.port=9300 

5. Resync data 

    a) Nav->Admin->Application Data->System->Search Control 
    b) Click Re-Index Listings        


###4.4.4 Updated Search Server at Runtime

1. Use Admin->Application Management->System to set the system config properties 

2. On Managers tab -> Restart Search Server Managers



##4.5  System Setup
------------

Unless otherwise noted, run as sudo.

**NOTE:** You can use Nano or another text editor.

###4.5.1 Install Java

Use the following steps to install Java.

1.  Download JDK from Oracle

2.  Move the downloads to the server home directory

3.  Run Sudo su as root

4.  Extract and move following folders:

    -   mkdir /usr/java

    -   tar -xvf jdk-8u25-linux-x64.tar.gz

    -   mv jdk1.8.0\_25 /usr/java/

    -   chmod 755 -R /usr/java

5.  Create the following links:

    -   ln -s /usr/java/jdk1.8.0\_25 /usr/java/jdk8

    -   ln -s /usr/java/jdk1.8.0\_25 /usr/java/latest

6.  Setup Environment Vars using the instructions at
    http://www.cyberciti.biz/faq/linux-unix-set-java\_home-path-variable/,
    and then perform the following:

	a.  nano /etc/profile

	b.  Add to the bottom:

		-   #Java Path

		-   export PATH=\$PATH:/usr/java/latest/bin

		-   export JAVA\_HOME=/usr/java/latest

	c.  Save/Exit then source /etc/profile

	d.  nano /etc/bash.bashrc

	e.  Add to the bottom:

		-   #Java Path

		-   export PATH=\$PATH:/usr/java/latest/bin

		-   export JAVA\_HOME=/usr/java/latest

7.  Save/Exit then source /etc/bash.bashrc

8.  Confirm that java -version runs

###4.5.2 Install Tomcat Public Package
------

Use the following steps to install the Tomcat public package.

1.  Download and copy to home directory

2.  tar -xvf apache-tomcat-7.0.55.tar.gz

3.  mv apache-tomcat-7.0.55 /usr/local/tomcat

4.  ln -s /usr/local/tomcat/apache-tomcat-7.0.55
    /usr/local/tomcat/latest
	(Use ln -nsf (target) (link) to repoint)

5. nano /usr/local/tomcat/latest/bin/setenv.sh

> Add line: CATALINA\_OPTS=-Xmx1024m

**NOTE:** Memory settings depend on server config. Minimum recommended
memory is 1GB; however, it may be able to run on less. The amount of
memory affects the amount of concurrent users the server can support.

> VM RAM:
>
> 2GB -&gt; -Xmx1024m
>
> 4GB -&gt; -Xmx2048m
>
> 8GB -&gt; -Xmx6144m (Make sure to use a 64bit VM)

**OPTIONAL:** In the conf/server.xml (In Connector section)

-   Add http compression on. 
    Compression="on"
    compressableMimeType="text/html,text/xml,text/plain,application/json,text/css,application/javascript"

- Set to use NIO Protocal
  protocol="org.apache.coyote.http11.Http11NioProtocol"
  maxThreads="1000"

> Also, you may consider setting more aggressive connection settings
> than the default depending on your expected load. See Tomcat
> documentation: <http://tomcat.apache.org/tomcat-7.0-doc/index.html>.
> The default settings will suffice for most deployments.

>6\. Setup Tomcat as a service using the following:

[See Installing tomcat 7 on CentOS](http://www.davidghedini.com/pg/entry/install\_tomcat\_7\_on\_centos)

>a.  nano /etc/init.d/tomcat

> \#!/bin/bash
>
> \# description: Tomcat Start Stop Restart
>
> \# processname: tomcat
>
> \# chkconfig: 234 20 80
>
> \#JAVA\_HOME=/usr/java/jdk8
>
> \#export JAVA\_HOME
>
> \#PATH=\$JAVA\_HOME/bin:\$PATH
>
> \#export PATH
>
> CATALINA\_HOME=/usr/local/tomcat/latest
>
> case \$1 in
>
> start)
>
> sh \$CATALINA\_HOME/bin/startup.sh
>
> ;;
>
> stop)
>
> sh \$CATALINA\_HOME/bin/shutdown.sh
>
> ;;
>
> restart)
>
> sh \$CATALINA\_HOME/bin/shutdown.sh
>
> sh \$CATALINA\_HOME/bin/startup.sh
>
> ;;
>
> esac
>
> exit 0

>b.  chmod 755 /etc/init.d/tomcat

>c.  chkconfig --add tomcat

>d.  chkconfig --level 234 tomcat on

>7\. Open port iptables -I INPUT -p tcp --dport 8080 -j ACCEPT

###4.5.3 Install Tomcat Using JPackage
-----

Use the following steps to install Tomcat using JPackage
[See jpackage.org](http://www.jpackage.org/)

1.  At www.jpackage.org, follow the installation instructions to setup
    the repository for your system install (e.g. yum, apt, and urpmi)

2.  After you have set up the repository, pull down Tomcat7-7.0.54-2
    from the version 6 repo.

**NOTE:** JPackage version has a mistake in the package. The ecj jar
library is old and doesn't work with JDK1.8 ; however, this is easily
corrected. Download 7.0.54 from apache.org and replace ecj-xxx.jar in
tomcat/lib directory with ecj-xxx.jar from the 7.0.54 version.

###4.5.4 Server Control
----

Use the following commands to control the server.

-   service tomcat start

-   service tomcat stop

> **NOTE:** It can take a minute for the application to shut down.
> Please wait before restarting.

-   service tomcat restart

> **NOTE:** Using this is not recommended as it not always successful
> due the script not waiting for shutdown.

##4.6 Deploying application
---------------------

To deploy the application, copy openstorefront.war to
/usr/local/tomcat/latest/webapps

##4.7 Application Configuration
-------------------------

The application configuration and data are stored in
/var/openstorefront/. Make sure the user running the application has r/w
permission for that directory.  All directories are created upon
application startup. The high level directory map is stored under
/var/openstorefront/.

-   config - holds all configurations files. Defaults are created on
    initial startup.

-   db - holds database files

-   import -directory for placing files to be imported.

-   perm - permanent storage location

-   temp - temporary storage that application controls.

NOTE: most application temp storage defaults to the system temp storage
location. The temp directory here holds information that needs to be
persisted for a longer time period. The main configuration file is:
/var/openstorefront/config/openstorefront.properties

On initial setup modify the following:

1.  change the url to point to esa/solr
    "solr.server.url=http://localhost:15000/solr/esa"

2.  For OpenAM Integration:

a.  \#Security Header

> openam.url=&lt;Url to open am something like
> http:/idam.server.com/openam
>
> \#http:/.../openam/UI/Logout (Full URL)
>
> logout.url=&lt;Full url to the logout&gt;
>
> \#Change any header as needed. The defaults likely will work out of
> the box.
>
> openam.header.username=sAMAccountName
>
> openam.header.firstname=givenname
>
> openam.header.lastname=sn
>
> openam.header.email=mail
>
> openam.header.phone=telephonenumber
>
> openam.header.group=memberOf
>
> openam.header.ldapguid=memberid
>
> openam.header.organization=
>
> openam.header.admingroupname=STORE-Admin

b.  Edit shiro.ini under the config directory

> under \[main\]
>
>> uncomment (remove \#)
>
>> headerRealm = edu.usu.sdl.openstorefront.security.HeaderRealm
>
>> securityManager.realms = \$headerRealm
>
> under \[users\]
>
>> comment out
>
>> \#admin = secret, administrator
>
>> \#user = user
>
> under \[roles\]
>
>> comment out
>
>> \#admin = administrator

###4.7.1 Open AM Notes:
-------------
If when setting Open AM up with certificates you may need to use a truststore on openstorefront tomcat.  If so, remember to update the certificates in the truststore when the certificate changes.  Open AM, Openstorefront, and proxy if used will all need to hvae valid matching certificates.


##4.8 Importing Data
--------------

Import data using the following steps.

1.  Export (or import) data from an existing system using application
    > admin tools. (Requires an Admin Login)

2.  When the application is first started, it will load a default set of
    "lookup" types. These can be later changed using the admin tools. On
    a new install, no attributes, articles or components are loaded.
    They can be entered or imported using the application admin tools.

##4.9  Logging Notes
-------------

You can view the logs messages in the Catalina log file:
/usr/local/tomcat/latest/logs

###4.9.1 Log Level Definitions
-------

See the following table for the log definitions.

-  **SEVERE**    -  Something didn't run correctly or as expected
-  **WARN**      - Behavior may not be desired but, the system was able to continue
-  **INFO**   -  System admin message
-  **FINE**    - Developer message
-  **FINER**    - Detailed developer messages
-  **FINEST**    -  Trace information

###4.10 Setup OpenAM
------------

See the example below for OpenAM setup. Your configuration may be
different.

(See https://bugster.forgerock.org/jira/browse/OPENAM-211: J2EE agents
are unable will not work, if the container was started prior to OpenAM).

**NOTE:** When the OpenAM policy agent is installed on Tomcat, the
application server will not start unless the OpenAM server is available.
This is a known issue with the OpenAM Policy agent.

###4.10.1 Versions Used
------

The following versions were used:

-   [Tomcat 7.0.55 64-bit Windows
    zip](http://mirrors.sonic.net/apache/tomcat/tomcat-7/v7.0.55/bin/apache-tomcat-7.0.55-windows-x64.zip)

-   [OpenAM
    11.0.0.0](https://backstage.forgerock.com/downloads/enterprise/openam/openam11/11.0.0/OpenAM-11.0.0.zip):

    -   <http://docs.forgerock.org/en/openam/11.0.0/release-notes/index/index.html>

-   [J2EE Policy Agent 3.3.0 Apache Tomcat 6 and
    7](https://backstage.forgerock.com/downloads/enterprise/openam/j2eeagents/stable/3.3.0/Tomcat-v6-7-Agent-3.3.0.zip):

    -   <http://docs.forgerock.org/en/openam-pa/3.3.0/jee-release-notes/index/index.html>

-   64 bit JRE 1.7.0\_67-b01

###4.10.2 Installation of OpenAM Java EE Policy Agent into Tomcat 7.0.55
--------

Use the following steps to install OpenAM Java EE Policy Agent on
Tomcat.

1.  Make sure the Agent Profile has already been created in OpenAM

2.  Create a *pwd.txt* file at C:\\Temp\\pwd.txt and add your Agent
    Profile password to it

3.  Shutdown the Tomcat server that is going to run your web application

4.  Make sure the Tomcat server that is running OpenAM is still running

5.  Extract Tomcat-v6-7-Agent-3.3.0.zip to a known directory

6.  CD into the j2ee\_agents/tomcat\_v6\_agent/bin directory

7.  Execute agentadmin --install to install the agent

###4.10.3 References
------

-   <http://openam.forgerock.org/openam-documentation/openam-doc-source/doc/jee-install-guide/index/chap-apache-tomcat.html>

-   <http://openam.forgerock.org/openam-documentation/openam-doc-source/doc/jee-install-guide/#chap-apache-tomcat>

###4.10.4 Configuration of OpenAM
------

See
[Open AM Getting Started](http://openam.forgerock.org/openam-documentation/openam-doc-source/doc/getting-started/)
for OpenAM configuration information.

###4.10.5 Configure the Policy in OpenAM
-------

Use the following steps to configure the OpenAM policy.

1.  Open up OpenAM in a web
    browser http://c00788.usurf.usu.edu:8080/openam

2.  Log into OpenAM using amadmin

3.  Click on Access Control -> /(Top Level Realm) -> Policies

4.  Click on New Policy

>>a.  Give the Policy a name of Storefront Policy

>>b.  In the Rules table click New

>>>i.  Select URL Policy Agent and click **Next**

>>>ii. Enter the following in Step 2 of 2: New Rule

>>>-   Name: Allow Storefront Access

>>>-   Resource Name: http://c00788.usurf.usu.edu:8081/agentsample/

>>>-   Check the boxes for GET and POST

>>c.  In the Subjects table click **New**

>>>i.  Select Authenticated Users and click Next

>>>ii. Name the rule All Authenticated Users

>>>iii. Click **Finish**

>>d.  Create a new response provider

>>-   In the Dynamic Attribute make sure uid and isMemberOf is
    selected (ctrl-click)

>>-   Click on **Finish**

###4.10.6 Creating the Agent Profile
-----

Use the following steps to create the agent profile.

1.  Open up OpenAM in a web
    browser http://c00788.usurf.usu.edu:8080/openam

2.  Log into OpenAM using amadmin

3.  Click on **Access Control** &gt; **Top Level Realm** &gt;
    **Agents** &gt; **J2EE**

4.  Create a new J2EE agent by clicking on the **New...** button under
    Agent

5.  Create the agent with the following parameters:

>-   Name: myagent

>-   Password: password

>-   Configuration: Centralized

>-   Server URL: http://c00788.usurf.usu.edu:8080/openam

>-   Agent URL: http://c00788.usurf.usu.edu:8081/agentsample

#5.  Configuration
------

##5.1  Security
--------

###5.1.1 Supported Realms
------

Configure in /var/openstorefront/config/shiro.ini

-   INI (Properties File; Default)

    Users are specified in the users section.

-   LDAP (Example)

> \[main\]

-   ldapRealm = org.apache.shiro.realm.ldap.JndiLdapRealm

-   ldapRealm.userDnTemplate = uid={0},ou=users,dc=mycompany,dc=com

-   ldapRealm.contextFactory.url = ldap://ldapHost:389

-   ldapRealm.contextFactory.authenticationMechanism = DIGEST-MD5

-   ldapRealm.contextFactory.environment\[some.obscure.jndi.key\] = some
    value

####5.1.1.1 Database

See
[Configure JDBC Realm](http://stackoverflow.com/questions/17441019/how-to-configure-jdbcrealm-to-obtain-its-datasource-from-jndi)
for how to configure JDBCRealm to obtain its DataSource from JNDI.

> \[main\]

-  dataSource = org.apache.shiro.jndi.JndiObjectFactory

-  dataSource.resourceName = java://app/jdbc/myDataSource

-  jdbcRealm = org.apache.shiro.realm.jdbc.JdbcRealm

-  jdbcRealm.permissionsLookupEnabled = true

-  jdbcRealm.dataSource = \$dataSource

 \# you can customize the authenticationQuery, userRolesQuery and
permissionsQuery,  if needed.

- securityManager.realms = \$realm

####5.1.1.2 OPENAM (Request Header) 
-----

>\[main\]

\#Also, remember to comment out the users and roles to remove the INIRealm

- headerRealm = edu.usu.sdl.openstorefront.security.HeaderRealm

- securityManager.realms = \$headerRealm

####5.1.1.3 Integration with OpenAM
-----

Configure in: /var/openstorefront/config/openstorefront.properties

( **Property** -description ( **Default** ))
  
-  **openam.url**  -http:/.../openam (Full URL to open am instance)              

-  **logout.url** - http:/.../openam/UI/Logout   (Full URL to logout)   

-  **openam.header.username**  - HTTP Header for Username    ( **sAMAccountName** )   

-  **openam.header.firstname**  - HTTP Header for Firstname    ( **givenname** )        

-  **openam.header.lastname** - HTTP Header for Lastname      ( **sn** )      

-  **openam.header.email**  - HTTP Header for email      ( **mail** )      

-  **openam.header.group**  - HTTP Header for group      ( **memberOf** )      

- **openam.header.ldapguid** - HTTP Header for ldapguid      ( **memberid** )      

-  **openam.header.organization** - HTTP Header for organization    

-  **openam.header.admingroupname** - HTTP Header for Admin Group Name \*Handles multiple values  ( **STORE-Admin** )    

Also, need to adjust the open am agent filter

Change: 

    <filter-mapping>
        <filter-name>Agent</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>INCLUDE</dispatcher>
        <dispatcher>FORWARD</dispatcher>
        <dispatcher>ERROR</dispatcher>
    </filter-mapping>

To 

    <filter-mapping>
        <filter-name>Agent</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>ERROR</dispatcher>
    </filter-mapping>


###5.1.2 User Types
-----

The user types are:

-   User: This is restricted user that constitutes a normal user of
    the application.

-   Administrator: This is an unrestricted user that can use the
    administrator tools in the application.

##5.2  Integration External LDAP (User Syncing)
----------------------------------------

When a user is not located in the external management system then the
user profile in the application will be deactivated.

Warning: This will not prevent login! Upon login the user profile will
be reactivated. To prevent login, refer to the external user management
system that the application is connected to and inactive the user from
there.

Configure in: /var/openstorefront/config/openstorefront.properties

( **Property** -description ( **Default** ))

-   **ldapmanager.url**                       -Full URL to the LDAP (ldap://ldapHost:389 or ldap://localhost:389/o=JNDITutorial)   
-   **ldapmanager.userDnTemplate**   -         uid={0},ou=users,dc=mycompany,dc=com; Reserved, not currently used                  
-   **ldapmanager.authenticationMechanism** -  NONE, SIMPLE, DIGEST-MD5, etc.                                                      ( **SIMPLE** )
-   **ldapmanager.security.sasl.realm** -      May be needed for SASL authentication                                               
-   **ldapmanager.binddn** -                    The LDAP user to use in the connection (Full DN name)                               
-   **ldapmanager.credentials** -               The LDAP credentials                                                                
-   **ldapmanager.contextRoot** -               Root to directory to search                                                         
-   **ldapmanager.attribute.username** -        Attribute to map to username                                                       ( **sAMAccountName** )
-   **ldapmanager.attribute.email** -           Attribute to map to email                                                           ( **mail** )
-   **ldapmanager.attribute.fullname** -        Attribute to map to fullname                                                        ( **name** )
-   **ldapmanager.attribute.organization** -    Attribute to map to organization                                                    ( **company** )
-   **ldapmanager.attribute.guid** -            Attribute to map to guid                                                            ( **objectGUID** )

##5.3 Jira Integration
----------------

Configure in: /var/openstorefront/config/openstorefront.properties

( **Property** -description ( **Default** ))

-  **tools.login.user** -               Login Credentials for Integrations (currently just for jira)   
-  **tools.login.pw** -                 Login Credentials for Integrations (currently just for jira)   
-  **jra.connectionpool.size** -       Resource pool size                                             ( **20** )
-  **jira.connection.wait.seconds** -   Wait time if the pool is empty                                 ( **60** )
-  **jira.server.url** -                Jira server to connect to                                      ( **https://jira.di2e.net** )

##5.4 Mail Server
-----------

Configure in: /var/openstorefront/config/openstorefront.properties

( **Property** -description ( **Default** ))

-  **mail.smtp.url** -        Login Credentials for Integrations (currently just for jira)   ( **localhost** )
-  **mail.server.user** -     Login Credentials for mail server                              
-  **mail.server.pw** -       Login Credentials for mail server                              
-  **mail.smtp.port** -       Mail Port (25 common)                                          
-  **mail.use.ssl** -         Set to true if server requires it                            
-  **mail.use.tls** -         Set to true if server requires it                            
-  **mail.from.name** -       From Name                                                      ( **Storefront Notification** )
-  **mail.from.address** -    From Email Address                                             ( **donotreply@storefront.net** )
-  **mail.reply.name** -      Reply name (usually display at the bottom the message)         ( **Support** )
-  **mail.reply.address** -   Reply email (usually display at the bottom the message)        ( **helpdesk@di2e.net** )
-  **test.email** -           Set for automated testing only; the email to use for testing 

##5.5 Other Application Properties
----------------------------

Configure in: /var/openstorefront/config/openstorefront.properties

( **Property** -description ( **Default** ))

-  **errorticket.max**          -            Max amount of ticket to hold (culls the oldest records upon filling)                                                                                                       ( **5000** )
-  **trackingrecords.max.age.days**  -       Max age of tracking records                                                                                                                                                ( **365** )
-  **solr.server.url**     -                 URL to the SOLR instance to use. ; it should point to the appropriate collection.                                                                               ( **http://localhost:8983/solr/esa** )
-  **db.connectionpool.min**    -            DB min pool size                                                                                                                                                           ( **5** )
-  **db.connectionpool.max** -               DB max pool size                                                                                                                                                           ( **40** )
-  **db.user**             -                 Should match orientdb-server-config.xml                                                                                                                                    
-  **db.pw**            -                    Should match orientdb-server-config.xml                                                                                                                                    
-  **job.working.state.override.minutes** -  Max job running time. Use for Integrations. To determine if a job got stuck.                                                                                               ( **30** )
-  **message.archive.days**        -         User message max age of archives                                                                                                                                           ( **30** )
-  **message.queue.minmintues**    -         User message queue time or the time the message waits before sending.                                                                                                      ( **10** )
-  **message.maxretires**    -               Max times the user message will try to send if unable to deliver.                                                                                                          ( **5** )
-  **message.recentchanges.days**   -        Time between "recent changes" messages from being sent.                                                                                                                    ( **28** )
-  **app.title**   -                         Title of the application. Used in emails but, also other places.                                                                                                           ( **DI2E Storefront** )
-  **external.usermanager**   -              Specifies the manager that is used for external user management. The manager must be supported by the application. ( IniRealmManager or LdapUserManager)                   ( **IniRealmManager** )
-  **external.sync.activate**  -             Set to 'true' to run the sync                                                                                                                                              (**False**)
-  **dblog.on**        -                     Activates logging records to the database; Note: All log record are still logged in the server logs regardless of setting this. This just controls the database logging.   ( **false** )
-  **dblog.maxrecords**     -                Maximum database records to store                                                                                                                                          ( **50000** )
-  **dblog.logSecurityFilter**  -            Log security API audit records; Note: setting this to true can cause noise when using the application log viewer.                                                          ( **False** )
-  **jirafeedback.show** - Allows users to provide jira feedback (True/False) ( **True** )
-  **filehistory.max.days** - Sets the max days to keep file history ( **180** )
-  **notification.max.days** - Set the max days to keep nofitication messages ( **7** )
-  **feedback.email** - Email address to send feedback to
-  **ui.idletimeout.minutes** - Set to a value > 1 to have the UI popup a idle warning about there session (Default is the application tries to keep the session alive.)
-  **ui.idlegraceperiod.minutes** -Set this to configure the grace period for the tdle timeout. After the message appears.

#6. Database Management
-----

The application handles all database interaction transparently, so
direct database access and manipulation is not needed.  

See the following for information on outside control (should rarely be
needed/used).

##6.1 Refreshing the Database
-----------------------

**CAUTION:** This will wipe out all data in the application. Data, such
as User profiles, cannot be recovered. Component user data can be
preserved by performing an export from the component admin tool.

Make a backup by copying all of the files in the /var/openstorefront/db
directory or use the following console tools steps:

1.  Stop the Tomcat server  (e.g. service tomcat stop)

2.  Remove the folder /var/openstorefront/db
    (rm -rf /var/openstorefront/db)

3.  Start the tomcat server

When the application loads it will create a new database and populate
the data from whatever is currently in the import folders (lookups only; attributes, component, articles will need to be manually
trigger or uploaded via the Admin Tools UI).

The initial load of the application may take a few minutes. If the
import directories are empty, the application will load default lookup
files that are packaged with the application.

##6.2 Installing Database Console
----------------------------

**CAUTION:** Viewing (Querying) information is fine; however, use
extreme caution when modifying any records as all logic is handled by
the application.

1.  Download Orient DB (Currently using the 1.7.x series) at
    [Orient DB.org](http://www.orientechnologies.com/download/)

2.  Extract the archive

3.  Run the console ./bin/console.sh 

4.  Connect to the DB: connect remote: localhost/openstorefront
    (user) (password) (see the
    /var/openstorefront/config/openstorefront.properties for
    connection information)

The database supports an SQL like interface and then adds other
functionality on top.

-   See [Orient DB Backup](http://www.orientechnologies.com/docs/last/orientdb.wiki/Backup-and-Restore.html) for
    information about backup

-   See [Orient DB Export/Import](http://www.orientechnologies.com/docs/last/orientdb.wiki/Export-and-Import.html) for
    export and imports.

#7. External Application API
-----

The API document is directly reflected from the live code so it is
always current for the running version of the application. The API
documentation can be accessed by login in as an admin and following the
link from the admin tools. A print view of the API can be generated form
there as well.

#8  Development
 ------

##8.1 Key Components Used

The following components were used in the development:

-   JDK 8

-   ESA/Solr

-   OpenAM (Configurable)

The application is a JEE webapp, so any JEE 6 (web-profile) compliant
server should work with some server configuration. The current
deployment target is Tomcat 7.


##8.2 Key Libraries Used

The following key libraries were used in the development:

-   JAX-RS- heavily used for REST API. (Jersey with Moxy for
    data binding)

-   Stripes- Action based web framework

-   Jackson- JSON Handling/Binding

-   Apache Shiro- Security

-   Orient DB- No SQL/Multi-Model database

-   Ext.js and tinymce
  
##8.3 Notes for Redhat/Centos users:
-----

See the following note for Redhat/Centos users.

-   yum install maven

##8.4 Building with Maven

run "mvn install" from \$PROJECT\_HOME/server/openstorefront

(Skip tests)\
Mav -Dmaven.test.skip=true or -DskipTests=true install

##8.5 Deploying

Copy the war artifact to the webapp directory for Tomcat. Some IDEs can
handle this for you. See application server documentation for other deployment
mechanisms.

##8.6  Running
-------

The application is targeted to run in Tomcat 7; however, it may run in
other compatible containers with little or no changes.

**NOTE:** Searching requires an external ESA/(Solr) instance setup.

##8.7 Setting up Solr
---------------

ESA uses Solr 4.3.1, so the application is setup to use that specific
version.

Download Version 4.3.1
from [Solr](http://archive.apache.org/dist/lucene/solr/) and perform the
following steps:

1.  Unpackage

2.  Replace (solr install dir) /example/solr/collection1/conf/schema.xml
    with the scheme.xml include in this project's doc folder.

3.  configure openstorefront to point to Solr

 >\a.  /var/openstorefront/config/openstorefront.properties

4.  edit solr.server.url to
    solr.server.url=http://localhost:8983/solr/collection1

5.  Start Solr from (solr install dir)/example - java -jar start.jar

##8.8 Testing
-------

-   Unit test run as part of the Maven install.

-   Container/Integration tests login as admin go to
    <http://localhost:8080/openstorefront/test/ServiceTest.action>


##8.9  Contributing Patches
--------------------

The code is hosted on the public GitHub
[https://github.com/di2e/openstorefront](<https://github.com/di2e/openstorefront>). Create a pull request to the
current release branch. A pull request will be reviewed prior to merge.
Please file bugs or enhancement by submitting a ticket to:

<https://jira.di2e.net/browse/STORE> (Login required)

If you are unable to obtain a login account then submit an issue ticket
on the GitHub site.

##8.10 Versioning Strategy
-------------------

The software is versioned based on the following:

Major.Minor.Patch

A major version change is represents a major change in functionality or
an addition of a major new feature. A minor version change represent
minor features and improvement along with bug fixes. A patch release is
done only for bug fixes and needed improvements to existing features.

The REST API versioning follows <http://semver.org/> where major version
represents incompatible API changes. The REST endpoints include a
version number which represents the major version. The version in the
URL doesn't change with minor versions. However, the API follows with
the version of the application.
