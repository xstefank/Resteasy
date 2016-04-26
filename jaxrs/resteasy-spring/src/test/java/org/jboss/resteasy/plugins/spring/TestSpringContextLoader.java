package org.jboss.resteasy.plugins.spring;

import static org.junit.Assert.assertEquals;

import org.jboss.resteasy.spi.ResteasyDeployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * Tests that SpringContextLoader does proper validations and adds an application listener.
 * This was used to extract the code into SpringContextLoaderSupport, so that it could be re-used
 * without having to extend ContextLoader.  Probably should move these tests to directly
 * test SpringContextLoaderSupport and replace this test with a test that simply asserts
 * that SpringContextLoaderSupport was callled.
 */
public class TestSpringContextLoader
{

   private SpringContextLoader contextLoader;

   @Before
   public void setupEditor()
   {
      contextLoader = new SpringContextLoader();
   }

   @Test(expected=RuntimeException.class)
   public void testThatDeploymentIsRequired()
   {
      contextLoader.customizeContext(
              mockServletContext(null),
              mockWebApplicationContext());
   }

   @Test
   public void testThatWeAddedAnApplicationListener() 
   {
      StaticWebApplicationContext context = mockWebApplicationContext();
      int numListeners = context.getApplicationListeners().size();
      contextLoader.customizeContext(
            mockServletContext(someDeployment()),
            context);
      int numListenersNow = context.getApplicationListeners().size();
      assertEquals("Expected to add exactly one new listener; in fact added " + (numListenersNow - numListeners),
         numListeners + 1,numListenersNow);
   }

   private StaticWebApplicationContext mockWebApplicationContext() 
   {
      return new StaticWebApplicationContext();
   }

   private ServletContext mockServletContext(ResteasyDeployment deployment)
   {
      MockServletContext context = new MockServletContext();

      if (deployment != null)
         context.setAttribute(ResteasyDeployment.class.getName(), deployment);

      return context;
   }

   private ResteasyDeployment someDeployment()
   {
      return new ResteasyDeployment();
   }
}
