package org.jboss.resteasy.spi.metadata;

import org.jboss.resteasy.spi.ResourceBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:mstefank@redhat.com">Martin Stefanko</a>
 */
public class ResourceBuilderSupplier
{
   private static ResourceBuilder defaultBuilder = new JaxrsResourceBuilder();

   public static ResourceBuilder getBuilder() {
      ResourceBuilder resourceBuilder = (ResourceBuilder) ResteasyProviderFactory.getContextDataMap().get(ResourceBuilder.class);

      if (resourceBuilder != null)
      {
         return resourceBuilder;
      }

      ServiceLoader<ResourceBuilder> loader = ServiceLoader.load(ResourceBuilder.class, Thread.currentThread().getContextClassLoader());

      Iterator<ResourceBuilder> it = loader.iterator();

      if (it.hasNext())
      {
         return it.next();
      }

      return defaultBuilder;
   }
}
