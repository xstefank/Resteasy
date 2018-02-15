package org.jboss.resteasy.test.stereotype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.test.cdi.basic.resource.SingletonLocalIF;
import org.jboss.resteasy.test.cdi.basic.resource.SingletonRootResource;
import org.jboss.resteasy.test.cdi.basic.resource.SingletonSubResource;
import org.jboss.resteasy.test.cdi.basic.resource.SingletonTestBean;
import org.jboss.resteasy.test.stereotype.resource.MediaTypeResource;
import org.jboss.resteasy.test.stereotype.resource.MediaTypeStereotype;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @tpSubChapter CDI
 * @tpChapter Integration tests
 * @tpTestCaseDetails Test for @Produces and @Consumes on @Stereotype beans
 * @tpSince RESTEasy 3.0.26
 */
@RunWith(Arquillian.class)
@RunAsClient
public class MediaTypeStereotypeTest
{
    static Client client;
    protected static final Logger logger = LogManager.getLogger(MediaTypeStereotypeTest.class.getName());

    @BeforeClass
    public static void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterClass
    public static void close() {
        client.close();
    }

    @Deployment
    public static Archive<?> deploy() {
        WebArchive war = TestUtil.prepareArchive(MediaTypeStereotypeTest.class.getSimpleName());
        war.addClasses(MediaTypeStereotype.class);
        return TestUtil.finishContainerPrepare(war, null, MediaTypeResource.class);
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, MediaTypeStereotypeTest.class.getSimpleName());
    }


    @Test
    public void testProduces() throws Exception {
        WebTarget base = client.target(generateURL("/mediatype"));
        Response response = base.path("produces").request().get();

        Assert.assertEquals("Wrong content of response", "{}", response.readEntity(String.class));
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    }

}
