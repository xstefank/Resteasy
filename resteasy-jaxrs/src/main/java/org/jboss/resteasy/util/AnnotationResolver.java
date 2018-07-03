package org.jboss.resteasy.util;

import org.jboss.resteasy.spi.metadata.ResourceClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;

public class AnnotationResolver
{
   
   public static AnnotationResolver newInstance()
   {
      ServiceLoader<AnnotationResolver> loader = ServiceLoader.load(AnnotationResolver.class, Thread.currentThread().getContextClassLoader());

      Iterator<AnnotationResolver> it = loader.iterator();
      if (it.hasNext())
      {
         return it.next();
      }
      
      return new AnnotationResolver();
   }
   
   @SuppressWarnings(value = "unchecked")
   public <T extends Annotation> Class getClassWithAnnotation(Class<?> clazz, Class<T> annotation)
   {
      if (clazz.isAnnotationPresent(annotation))
      {
         return clazz;
      }
      for (Class intf : clazz.getInterfaces())
      {
         if (intf.isAnnotationPresent(annotation))
         {
            return intf;
         }
      }
      Class superClass = clazz.getSuperclass();
      if (superClass != Object.class && superClass != null)
      {
         return getClassWithAnnotation(superClass, annotation);
      }
      return null;

   }

   public <T extends Annotation> T getAnnotationFromClass(Class<T> annotationClass, Class<?> clazz)
   {
      return clazz.getAnnotation(annotationClass);
   }

   public <T extends Annotation> T getAnnotationFromResourceMethod(Class<T> annotationClass, Method method, ResourceClass resourceClass)
   {
      T annotation = method.getAnnotation(annotationClass);
      if (annotation == null) annotation = resourceClass.getClazz().getAnnotation(annotationClass);
      if (annotation == null) annotation = method.getDeclaringClass().getAnnotation(annotationClass);
      
      return annotation;
   }
   
}
