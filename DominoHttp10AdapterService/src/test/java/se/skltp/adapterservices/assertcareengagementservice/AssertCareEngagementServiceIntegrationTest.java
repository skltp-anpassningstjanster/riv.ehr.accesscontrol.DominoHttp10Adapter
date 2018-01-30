/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.adapterservices.assertcareengagementservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.confluex.mock.http.ClientRequest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.transport.http.HttpsConnector;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import com.confluex.mock.http.MockHttpsServer;

import static com.confluex.mock.http.matchers.HttpMatchers.anyRequest;
import static junit.framework.Assert.assertEquals;

/**
 * Created by gorerk on 1/25/2018.
 */
public class AssertCareEngagementServiceIntegrationTest  extends AbstractTestCase {

    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("DominoHttp10Adapter-config","DominoHttp10Adapter-config-override");
    private static final String INBOUND1  = rb.getString("inbound.endpoint.dominohttpadapter.1");

    public static final String X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID = "x-rivta-original-serviceconsumer-hsaid";
    public static final String X_VP_SENDER_ID = "x-vp-sender-id";
    public static final String X_VP_INSTANCE_ID = "x-vp-instance-id";
    public static final String HEADER1_VALUE = "111";
    public static final String HEADER2_VALUE = "222";
    public static final String HEADER3_VALUE = "333";
    public static final int OUTBOUND_ENDPOINT_PORT = 15002;
    public static final String RESPONSE_RESOURCE = "testfiles/AssertCareEngagement-response.xml";
    public static final String REQUEST_RESOURCE = "testfiles/AssertCareEngagement-request.xml";

    MockHttpsServer server;

    public AssertCareEngagementServiceIntegrationTest() {
      setDisposeContextPerClass(true);
    }

    @Override
    protected String getConfigResources() {
        return
                "soitoolkit-mule-jms-connector-activemq-embedded.xml," +
                        "AssertCareEngagement-domino-service.xml," +
                        "DominoHttp10Adapter-common.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.doSetUp();

        // Set Mule https connector to use same trust manager as the MockHttpsServer.
        // Then we don't need to change anything for the https connector in the flow.
        HttpsConnector connector = muleContext.getRegistry().lookupObject(HttpsConnector.class);
        connector.setTrustManagerFactory(MockHttpsServer.getTrustManagerFactory());

        String response = getResourceAsString(RESPONSE_RESOURCE);
        server = new MockHttpsServer(OUTBOUND_ENDPOINT_PORT);
        server.respondTo(anyRequest()).withBody(response);
    }

    @After
    public void tearDown() {
        if( server != null) {
            server.stop();
        }
    }

    @Test
    public void testThatHeadersAreSentToOutboundServer() throws IOException, URISyntaxException, MuleException {

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID, HEADER1_VALUE);
        headers.put(X_VP_SENDER_ID, HEADER2_VALUE);
        headers.put(X_VP_INSTANCE_ID, HEADER3_VALUE);

        String payload = getResourceAsString(REQUEST_RESOURCE);

        MuleClient client = new MuleClient(muleContext);
        client.send(INBOUND1, payload, headers);

        assertEquals(1, server.getRequests().size());

        ClientRequest request = server.getRequests().get(0);
        assertEquals("Header missing or wrong:" + X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID, HEADER1_VALUE, request.getHeaders().get(X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID));
        assertEquals("Header missing or wrong:" + X_VP_SENDER_ID, HEADER2_VALUE, request.getHeaders().get(X_VP_SENDER_ID));
        assertEquals("Header missing or wrong:" + X_VP_INSTANCE_ID , HEADER3_VALUE, request.getHeaders().get(X_VP_INSTANCE_ID));
    }

    private String getResourceAsString(String resourceName) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        return IOUtils.toString(inputStream,"UTF-8");
    }

}
