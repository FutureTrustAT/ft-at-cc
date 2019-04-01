# ft-at-cc

FutureTrust BRZ Pilot Web Client

Use this client to sign and send electronic invoices to `test.e-rechnung.gv.at` where the FutureTrust validation service (ValS) is used to validate them.

# Prerequisites

* Java JRE or JDK version 8 or later must be installed
* The `JAVA_HOME` environment variable must be set to the matching path of the JRE/JDK
* A certificate key must be available in an "PKCS12" or "JKS" keystore file format
* An example e-Invoice must be available. Example invoices are shipped with this project (filename `example-ebi41.xml`, `example-ebi42.xml` and `example-ebi43.xml`)

# Installation

* Download the latest release from https://github.com/FutureTrustAT/ft-at-cc/releases
* Unzip in a local folder on your computer
* Modify the file `standalone-config.properties` and adopt according to the [rules below](#configuration)

## Linux Support
* Use the following command on the console: `java -cp "ft-at-cc-1.0.0-SNAPSHOT.jar:jars/*" at.gv.brz.futuretrust.brzclient.MainStart`

## Windows Support 
* Run the file `start.cmd` - a new shell/console window should open

## Start the eInvoice client UI
* Startup a browser and navigate to http://127.0.0.1:8080
* Sign and send invoices via your browser:
    * Choose an example XML file from the release (e.g. `example-ebi41.xml`) via the "Browse" button
    * Press "Sign and send"
    * Upon success, a green box should appear over the input field
    
## Error handling

* If the keystore is misconfigured, than the application startup will most likely fail. Use `start-dbg.cmd` to avoid opening a new shell/console window and try to identify based on the log messages, what went wrong. If you cannot find the error yourself, don't panic - send it by email to us (you will receive the contact email address in a separate way).
* If your local port 8080 is already in use, try running with the commandline parameter `-p 8081` to specify a different port. Than of course you need to change the URL in your browser to use the changed port (e.g. http://127.0.0.1:8081).

## Things not implemented so far

* No proxy server support present - the default Java system properties can be used for this (https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)
* The scripts for startup and shutdown are only available for Window - dear Linux users, any PR is welcome.

# Configuration

The configuration file `config.properties` contains all settings available for the client to run.
Alternatively you can specify the path to the configuration file via the environment variable `FT_AT_CONFIG` or via the system property `ft-at-cc-configuration-file` (use e.g. as `-Dft-at-cc-configuration-file=/path/to/configfile` on the Java commandline). The environment variable has highest precedence, than the system property and finally the local predefined filename `config.properties`.

* **`keystore.type`**: the type of the keystore. Must be either `JKS` (Java Key Store) or `PKCS12` (PKCS12 Key Store). The default is `JKS`.
* **`keystore.path`**: the absolute path to the keystore file.
* **`keystore.password`**: the password needed to read the keystore. Empty values are possible but not recommended.
* **`keystore.key.alias`**: the alias of the private key within the keystore.
* **`keystore.key.password`**: the password needed to read the private key within the keystore.

# e-Invoices

This client supports only e-Invoices in the formats ebInterface 4.1, v2 and v4.3. Later versions are not supported, because they don't support electronic signatures anymore. Respective XML Schemas and documentation (mainly in German) is available at https://www.wko.at/service/netzwerke/ebinterface-fruehere-version-xml-rechnungsstandard.html - this project contains example invoices, so you don't need to start from scratch.

Before transmission it is recommended to verify the consistency of the eInvoice manually at https://test.erechnung.gv.at/erb?locale=en_GB&p=tec_test_upload - only if the eInvoice is deemed valid there, it will be accepted and validated.

