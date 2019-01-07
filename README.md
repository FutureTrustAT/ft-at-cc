# ft-at-cc

FutureTrust Austria Commandline Client

Use this client to sign and send electronic invoices to `e-rechnung.gv.at` where the FutureTrust validation service (ValS) is used to validate them.

# Prerequisites

* Java JRE or JDK version 8 or later must be installed
* The `JAVA_HOME` environment variable must be set to the matching path of the JRE/JDK
* A certificate key must be available in an "PKCS12" or "JKS" keystore file format
* A writable "outgoing" directory for eInvoices to be signed and sent must be present
* A writable "success" directory for the responses of successful eInvoice transmissions 
* A writable "error" directory for responses of failed eInvoices transmissions 
* A writable "logs" directory for log files 

# Configuration

The configuration file `config.properties` contains all settings available for the client to run.

* **`keystore.type`**: the type of the keystore. Must be either `JKS` (Java Key Store) or `PKCS12` (PKCS12 Key Store). The default is `JKS`.
* **`keystore.path`**: the absolute path to the keystore file.
* **`keystore.password`**: the password needed to read the keystore. Empty values are possible but not recommended.
* **`keystore.key.alias`**: the alias of the private key within the keystore.
* **`keystore.key.password`**: the password needed to read the private key within the keystore.
* **`directory.log`**: the writable directory where the logs should be written to
* **`directory.outgoing`**: the writable directory where outgoing, unsigned eInvoices should be placed
* **`directory.response.success`**: the writable directory where successful responses should be stored
* **`directory.response.error`**: the writable directory where failed responses should be stored

# e-Invoices

This client supports only e-Invoices in the formats ebInterface 4.x. Later versions are not supported, because they don't support electronic signatures anymore. Respective XML Schemas and documentation (mainly in German) is available at https://www.wko.at/service/netzwerke/ebinterface-fruehere-version-xml-rechnungsstandard.html - this project contains example invoices, so you don't need to start from scratch.

Before transmission it is recommended to verify the consistency of the eInvoice manually at https://test.erechnung.gv.at/erb?locale=en_GB&p=tec_test_upload - only if the eInvoice is deemed valid there, it will be accepted and validated.

