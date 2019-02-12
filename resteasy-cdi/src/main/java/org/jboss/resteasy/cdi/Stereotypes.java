package org.jboss.resteasy.cdi;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyDeploymentObserver;
import org.jboss.resteasy.util.AnnotationResolver;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Stereotypes implements ResteasyDeploymentObserver
{
   private static final String JAVAX_WS_RS = "javax.ws.rs";

   private Map<Integer, CDIDeployment> cdiDeployments = new HashMap<>();

   private Stereotypes() {
      AnnotationResolver.setInstance(new CDIAnnotationResolver());
      ResteasyDeploymentImpl.registerObserver(this);
   }

   private static final Stereotypes instance = new Stereotypes();

   public static Stereotypes getInstance()
   {
      return instance;
   }

   public void addStereotype(Class<? extends Annotation> stereotypeClass, Class<?> clazz, BeanManager beanManager)
   {
      Set<Annotation> jaxRsAnnotations = collectJaxRsAnnotations(beanManager.getStereotypeDefinition(stereotypeClass), beanManager);
      CDIDeployment cdiDeployment = getCDIDeployment();
      if (cdiDeployment != null)
      {
         cdiDeployment.addStereotype(stereotypeClass, jaxRsAnnotations);
      }
   }

   private CDIDeployment getCDIDeployment()
   {
      Integer key = getCDIKey();

      if (key == null)
      {
         return null;
      }

      cdiDeployments.computeIfAbsent(key, k -> new CDIDeployment());

      return cdiDeployments.get(key);
   }

   private static Set<Annotation> collectJaxRsAnnotations(Set<Annotation> annotations, BeanManager beanManager)
   {
      Set<Annotation> jaxRsAnnotations = new HashSet<>();

      for (Annotation annotation : annotations)
      {
         if (isJaxRsAnnotation(annotation))
         {
            jaxRsAnnotations.add(annotation);
         } else if (beanManager.isStereotype(annotation.annotationType()))
         {
            Set<Annotation> stereotypeDefinition = beanManager.getStereotypeDefinition(annotation.annotationType());
            jaxRsAnnotations.addAll(collectJaxRsAnnotations(stereotypeDefinition, beanManager));
         }
      }

      return jaxRsAnnotations;
   }

   private static boolean isJaxRsAnnotation(Annotation annotation)
   {
      return annotation.annotationType().getName().startsWith(JAVAX_WS_RS);
   }

   public Map<Class<? extends Annotation>, Set<Annotation>> getStereotypes()
   {
      CDIDeployment cdiDeployment = getCDIDeployment();
      if (cdiDeployment != null)
      {
         return Collections.unmodifiableMap(cdiDeployment.getStereotypes());
      }

      return Collections.emptyMap();
   }

   @Override
   public void start(ResteasyDeployment deployment)
   {
      Integer key = getCDIKey();
      if (key != null)
      {
         cdiDeployments.computeIfAbsent(key, k -> new CDIDeployment());
      }
   }

   @Override
   public void stop(ResteasyDeployment deployment)
   {
      cdiDeployments.remove(getCDIKey());
   }

   private Integer getCDIKey()
   {
      try
      {
         return CDI.current().hashCode();
      } catch (IllegalStateException e)
      {
         // CDI context is not available
         return null;
      }
   }
}
