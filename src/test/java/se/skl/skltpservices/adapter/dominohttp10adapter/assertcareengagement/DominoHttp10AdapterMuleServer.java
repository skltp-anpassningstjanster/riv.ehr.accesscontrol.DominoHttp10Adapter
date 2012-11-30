package se.skl.skltpservices.adapter.dominohttp10adapter.assertcareengagement;

import org.soitoolkit.commons.mule.test.StandaloneMuleServer;


public class DominoHttp10AdapterMuleServer {


	public static final String MULE_SERVER_ID   = "DominoHttp10Adapter";
 
	public static final String MULE_CONFIG      = "DominoHttp10Adapter-config.xml";

	public static void main(String[] args) throws Exception {
		
		StandaloneMuleServer muleServer = new StandaloneMuleServer(MULE_SERVER_ID, MULE_CONFIG);
 
		muleServer.run();
	}

}