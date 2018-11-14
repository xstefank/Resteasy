package org.jboss.resteasy.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CDIDeployment
{
   private List<Class<?>> resourceClasses = new ArrayList<>();
   private List<Class<?>> providerClasses = new ArrayList<>();

   void addResource(Class<?> clazz)
   {
      resourceClasses.add(clazz);
   }

   void addProvider(Class<?> clazz)
   {
      providerClasses.add(clazz);
   }
   
   List<Class<?>> getResourceClasses()
   {
      return Collections.unmodifiableList(resourceClasses);
   }
   
   List<Class<?>> getProviderClasses()
   {
      return Collections.unmodifiableList(providerClasses);
   }
   
   void close()
   {
      resourceClasses = new ArrayList<>();
      providerClasses = new ArrayList<>();
   }
   
}
