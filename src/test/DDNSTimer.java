package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.dns.Dns;
import com.oracle.bmc.dns.DnsClient;
import com.oracle.bmc.dns.model.PatchZoneRecordsDetails;
import com.oracle.bmc.dns.model.RecordDetails;
import com.oracle.bmc.dns.model.RecordOperation;
import com.oracle.bmc.dns.model.UpdateRRSetDetails;
import com.oracle.bmc.dns.model.UpdateZoneRecordsDetails;
import com.oracle.bmc.dns.requests.PatchZoneRecordsRequest;
import com.oracle.bmc.dns.requests.UpdateRRSetRequest;
import com.oracle.bmc.dns.requests.UpdateZoneRecordsRequest;

public class DDNSTimer extends TimerTask {
	private String name ;
	private static String protocol = "https://";
    private static String host = "checkip.amazonaws.com";
    private static String compartmentOcid = "ocid1.compartment.oc1..aaaaaaaaxzrpumrq5ir2nfocjpcagelltpjhfi3atkynzbp5a3indkohatyq";
    private static Region region = Region.US_ASHBURN_1;
    private static String zoneName = "test22.com";
    private static String aRecordName = "mkcarter-Mac";
    private static String stateFile = "/Users/mattcarter/software/ddns/ddns_state.txt";
    private static String domain = "";
    
    private static CloseableHttpClient httpclient = null;
    
	public DDNSTimer(String n){
	  this.name=n;
	}
	
	public void run() {
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream("/Users/mattcarter/workspace/DDNS/src/test/ddns.properties");
			
			
			prop.load(input);
			
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		host = prop.getProperty("checkip.host");
		compartmentOcid = prop.getProperty("compartment.ocid");
		zoneName = prop.getProperty("zone.name");
		aRecordName = prop.getProperty("record.name");
		domain = aRecordName + "." + zoneName;
		

		
		
		String ip="";
		File exportedPolicy = new File(stateFile); 
  	  
  	  try {
		BufferedReader publicBr = new BufferedReader(new FileReader(exportedPolicy));
		ip = publicBr.readLine();
		//System.out.println("IP from state file: " + ip);
	} catch (FileNotFoundException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
  	
		
		String newIp = "";
		try {
			newIp = getCurrentIp(protocol+host);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | URISyntaxException
				| IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    
	    HashMap<String, String> ociTenant = new HashMap<String, String>();
	    ociTenant.put("compartment",compartmentOcid);
	    ociTenant.put("zoneName", zoneName);
	    ociTenant.put("aRecordName", aRecordName);
	    ociTenant.put("ip", newIp);
	    
	    
	    System.out.println("checkIP: " + newIp + " compared to current: "+ip);
	    if (!ip.equals(newIp)) {
	    	//Update A Record
	    	try {
				//Patch (update or create) DNS Record
	    		patchRecord(ociTenant, region);
				
				//Save state locally
				BufferedWriter writer = new BufferedWriter(new FileWriter(stateFile));
		        writer.write(newIp); 
		        writer.close();
		        
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	    
	    
	    if("CheckIP".equalsIgnoreCase(name)) {
	      try {
	      Thread.sleep(10000);
	      } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
	    }
	 }
	
	private String getCurrentIp(String url) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, URISyntaxException, ClientProtocolException, IOException { 
    	String currentIp = "";
        //Allow client to trusted self-signed certificates
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (certificate, authType) -> true).build();
       
         httpclient = HttpClients.custom()
        	.setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
        
         String uri = protocol+host;
         
         
         URIBuilder builder = new URIBuilder(uri);
         
		  //builder.setParameter("compartmentId", compartmentOcid);
		  
		  HttpGet request = new HttpGet(builder.build());
		
	
       
       //CloseableHttpClient httpclient = HttpClients.createDefault();
       
       CloseableHttpResponse response1 = httpclient.execute(request);
       try {
         //System.out.println(response1.getStatusLine());
         HttpEntity entity1 = response1.getEntity();
        
         
         BufferedReader reader = new BufferedReader(new InputStreamReader(entity1.getContent()));
        
         currentIp = reader.readLine(); 
         //System.out.println(strResponse);
       
    } finally {
        response1.close();
    }
       return currentIp;
   
    }
	
    public static void patchRecord(HashMap<String,String> hm, Region dnsRegion) throws Exception {
        final String configurationFilePath = "~/.oci/config";
        final String profile = "DEFAULT";

        final AuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(configurationFilePath, profile);

        final Dns client = new DnsClient(provider);
        //client.setRegion(Region.US_PHOENIX_1);
        client.setRegion(dnsRegion);

        // TODO: Pass in the compartment ID as an argument, or enter the value directly here (if known)
        final String compartmentId = hm.get("compartment");
        final String zoneName =  hm.get("zoneName");
        final String aRecordName = hm.get("aRecordName");
        final String targetValue = hm.get("ip");

        System.out.println();

        System.out.println("Updating zone " + zoneName + " A Record: " + aRecordName + "with "+ targetValue);
        

        // Now we prepare our update via UPDATE operation 
        // for the top level domain and an A record for a subdomain
        final List<RecordDetails> items = new ArrayList<>();
        
        items.add(
         RecordDetails.builder()
         .domain(aRecordName+ "." + zoneName)
         .ttl(1800)
         .rtype("A")
         .rdata(targetValue)
         .build());
   

        client.updateRRSet(
		
                UpdateRRSetRequest.builder()
                        .zoneNameOrId(zoneName)
                        .compartmentId(compartmentId)
                        .domain(domain)
                        .rtype("A")
                        .updateRRSetDetails(
                                UpdateRRSetDetails.builder()
                                .items(items)
                                .items(items)
                                .build())
                        .build());
                        
        
        System.out.println("==========Done!================");
        
    }
   

}
	
	
