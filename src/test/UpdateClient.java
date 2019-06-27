package test;
/*
* @version 1.0.1
*
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>19.0</version>
</dependency>
 */

import java.util.Timer;


public class UpdateClient {
	
    /* Starts timer to check IP every 5 seconds
     * 
     */
  
    public static void main(String[] args)  {

    	DDNSTimer te1 = new DDNSTimer("CheckIP");
    	Timer t = new Timer();
    	t.scheduleAtFixedRate(te1, 0,5*1000);
     
    }

}