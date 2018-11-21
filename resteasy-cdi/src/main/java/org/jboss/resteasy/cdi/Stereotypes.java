package org.jboss.resteasy.cdi;

import org.jboss.resteasy.util.AnnotationResolver;

import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Stereotypes
{
   private static final String JAVAX_WS_RS = "javax.ws.rs";

   private static final Map<Class<? extends Annotation>, Set<Annotation>> stereotypes = new HashMap<>();

   private Stereotypes() {
      AnnotationResolver.setInstance(new CDIAnnotationResolver());
   }

   private static final Stereotypes instance = new Stereotypes();

   public static Stereotypes getInstance()
   {
      return instance;
   }

   public void addStereotype(Class<? extends Annotation> stereotypeClass, Class<?> clazz, BeanManager beanManager)
   {
      Set<Annotation> jaxRsAnnotations = collectJaxRsAnnotations(beanManager.getStereotypeDefinition(stereotypeClass), beanManager);
      stereotypes.put(stereotypeClass, jaxRsAnnotations);
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
      return Collections.unmodifiableMap(stereotypes);
   }
}
