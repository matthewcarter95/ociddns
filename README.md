# ociddns
Oracle Cloud Infrastructure Dynamic DNS Components

Overview

Oracle is retiring its Dynamic DNS (DDNS) capability.  This document describes how you can implement your own DDNS within Oracle Cloud Infrastructure (OCI).  The picture shows the high-level approach.  A zone can be configured to accept updates from clients that have determined that their IP has changed.   CheckIP service can be a dedicated CheckIP service in OCI or a 3rd party checkIP service.

The DDNS tool kit has samples of how to connect to DNS service at OCI and patch the A record.  The toolkit also has a sample polling client that compares the result of the checkIP service with its current IPv4 address and updates when a change occurs.
DDNS Client Requirements
•	Must be able to store and retrieve a private key
•	Must be able to store config file for OCI tenancy
•	Must be able to store and write to file for IP persistence
•	Must be able to make HTTPS call outbound
Registering a Client

1.	Onboard to Oracle Cloud Infrastructure.

2.	Best practice is to have a Security Credential for every DDNS Client.  

3.	Create the keypair and config for the tenancy in ~/.oci.

4.	Install the DDNS toolkit and configure your checkIP endpoint.

