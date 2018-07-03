package org.jboss.resteasy.cdi;

import org.jboss.logging.Logger;
import org.jboss.resteasy.util.GetRestful;

import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Stereotypes
{
   private static final String JAVAX_WS_RS = "javax.ws.rs";
   private static final Logger log = Logger.getLogger(Stereotypes.class);

   private static final Map<Class<? extends Annotation>, Set<Annotation>> stereotypes = new HashMap<>();
   private static final List<Class<?>> stereotypedResources = new ArrayList<>();

   public static void addStereotype(Class<? extends Annotation> stereotypeClass, Class<?> clazz, BeanManager beanManager)
   {
      log.warn("Add stereotype - " + stereotypeClass);
      GetRestful.setAnnotationResolver(new CDIAnnotationResolver());

      Set<Annotation> jaxRsAnnotations = new HashSet<>(collectJaxRsAnnotations(beanManager.getStereotypeDefinition(stereotypeClass), beanManager));

      if (Utils.getAnnotation(Path.class, jaxRsAnnotations) != null)
      {
         stereotypedResources.add(clazz);
      }

      stereotypes.put(stereotypeClass, jaxRsAnnotations);
      log.error(stereotypes);
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
   
   public static Map<Class<? extends Annotation>, Set<Annotation>> getStereotypes()
   {
      return Collections.unmodifiableMap(stereotypes);
   } 
   
}
