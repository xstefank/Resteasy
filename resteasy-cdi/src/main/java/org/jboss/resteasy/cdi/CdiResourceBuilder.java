package org.jboss.resteasy.cdi;

import org.jboss.resteasy.spi.metadata.JaxrsResourceBuilder;
import org.jboss.resteasy.spi.metadata.ResourceClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mstefank@redhat.com">Martin Stefanko</a>
 */
public class CdiResourceBuilder extends JaxrsResourceBuilder
{

   private Map<Class<?>, Set<Annotation>> stereotypedClasses ;

   public CdiResourceBuilder()
   {
      try
      {
         stereotypedClasses = CdiInjectorFactory.lookupResteasyCdiExtension().getStereotypedClasses();
      } catch (Exception ex)
      {
         // the lookup may fail when there is CDI provider on the classpath
         stereotypedClasses = Collections.emptyMap();
      }
   }

   @Override
   protected <T extends Annotation> T getAnnotationFromResource(Class<T> annotationClass, Method method, ResourceClass resourceClass)
   {
      T annotation = super.getAnnotationFromResource(annotationClass, method, resourceClass);

      if (annotation == null) {
         Set<Annotation> stereotypeAnnotations = stereotypedClasses.get(resourceClass.getClazz());
         if (stereotypeAnnotations != null)
         {
            return Utils.getAnnotation(annotationClass, stereotypeAnnotations);
         }
      }

      return annotation;
   }
}
