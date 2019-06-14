package test;
/**
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 */
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.dns.Dns;
import com.oracle.bmc.dns.DnsClient;
import com.oracle.bmc.dns.model.*;
import com.oracle.bmc.dns.requests.*;
import com.oracle.bmc.dns.responses.*;
import com.oracle.bmc.model.BmcException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a basic example of how to add an A record to the OCI DNS service in the Java SDK. The main() method
 * in this class can take in four parameters:
 *
 *      - The first is the OCID of the compartment where the zone exists
 *      - The second is the name of the DNS zone (e.g. my-example-zone.com) to create the A record in
 *      - The third is the name of the A record.  For instance, if the record is 'myArecord', 
 *        the A record will be created  myArecord.my-example-zone.com.
 *        The fourth is the RDATA value of the A record, typically an IPv4 address as a string. 
 */
public class AddDNSARecord {
    public static void main(String[] args) throws Exception {
        final String configurationFilePath = "~/.oci/config";
        final String profile = "DEFAULT";

        final AuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(configurationFilePath, profile);

        final Dns client = new DnsClient(provider);
        //client.setRegion(Region.US_PHOENIX_1);
        client.setRegion(Region.US_ASHBURN_1);

        // TODO: Pass in the compartment ID as an argument, or enter the value directly here (if known)
        final String compartmentId = args[0];
        final String zoneName = args[1];
        final String aRecordName = args[2];
        final String targetValue = args[3];

        System.out.println();

        System.out.println("Patching zone " + zoneName + " with A Record: " + aRecordName);
        

        // Now we prepare our update via PATCH operation 
        // for the top level domain and an A record for a subdomain
        final List<RecordOperation> items = new ArrayList<>();

        items.add(
         RecordOperation.builder()
         .domain(aRecordName+ "." + zoneName)
         .ttl(1800)
         .rtype("A")
         .rdata(targetValue)
         .build());
        		

       
		client.patchZoneRecords(
                PatchZoneRecordsRequest.builder()
                        .zoneNameOrId(zoneName)
                        .compartmentId(compartmentId)
                        .patchZoneRecordsDetails(
                                PatchZoneRecordsDetails.builder()
                                .items(items)
                                        .items(items)
                                        .build())
                        .build());
        
        System.out.println("==========Done!================");

    }
   
}
