package org.jboss.resteasy.cdi;

import org.jboss.resteasy.spi.metadata.JaxrsResourceBuilder;
import org.jboss.resteasy.spi.metadata.ResourceClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:mstefank@redhat.com">Martin Stefanko</a>
 * @version $Revision: 1 $
 */
public class CdiResourceBuilder extends JaxrsResourceBuilder
{

   private Map<Class<?>, Set<Annotation>> stereotypedClasses = CdiInjectorFactory.lookupResteasyCdiExtension().getStereotypedClasses();

   @Override
   protected <T extends Annotation> T getAnnotationFromResource(Class<T> annotationClass, Method method, ResourceClass resourceClass)
   {
      Logger.getAnonymousLogger().info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
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
