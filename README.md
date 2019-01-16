# ft-at-cc

FutureTrust BRZ Pilot Web Client

Use this client to sign and send electronic invoices to `test.e-rechnung.gv.at` where the FutureTrust validation service (ValS) is used to validate them.

# Prerequisites

* Java JRE or JDK version 8 or later must be installed
* The `JAVA_HOME` environment variable must be set to the matching path of the JRE/JDK
* A certificate key must be available in an "PKCS12" or "JKS" keystore file format

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

