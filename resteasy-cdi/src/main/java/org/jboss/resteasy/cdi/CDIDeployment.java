package org.jboss.resteasy.cdi;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CDIDeployment
{
   private final Map<Class<? extends Annotation>, Set<Annotation>> stereotypes = new HashMap<>();

   public void addStereotype(Class<? extends Annotation> stereotypeClass, Set<Annotation> annotations)
   {
      stereotypes.put(stereotypeClass, annotations);
   }

   public Map<Class<? extends Annotation>, Set<Annotation>> getStereotypes()
   {
      return Collections.unmodifiableMap(stereotypes);
   }

   @Override
   public String toString()
   {
      return "CDIDeployment{" +
            "stereotypes=" + stereotypes +
            '}';
   }
}
