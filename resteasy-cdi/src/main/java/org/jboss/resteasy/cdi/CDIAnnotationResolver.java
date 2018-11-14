package org.jboss.resteasy.cdi;

import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.util.AnnotationResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CDIAnnotationResolver extends AnnotationResolver
{

   @Override
   public Class getClassWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation)
   {
      Class annotatedClass = super.getClassWithAnnotation(clazz, annotation);

      if (annotatedClass == null && findAnnotationInStereotypes(annotation, null, clazz) != null)
      {
         return clazz;
      }

      return annotatedClass;
   }

   @Override
   public <T extends Annotation> T getAnnotationFromClass(Class<T> annotatationClass, Class<?> clazz)
   {
      T annotation = super.getAnnotationFromClass(annotatationClass, clazz);

      if (annotation == null) annotation = findAnnotationInStereotypes(annotatationClass, null, clazz);
      

      return annotation;
   }

   @Override
   public <T extends Annotation> T getAnnotationFromResourceMethod(Class<T> annotationClass, Method method, ResourceClass resourceClass)
   {
      T annotation = super.getAnnotationFromResourceMethod(annotationClass, method, resourceClass);

      if (annotation == null) annotation = findAnnotationInStereotypes(annotationClass, method, resourceClass.getClazz());

      return annotation;
   }

   @Override
   public <T extends Annotation> T getAnnotationFromMethod(Class<T> annotationClass, Method method)
   {
      T annotation = super.getAnnotationFromMethod(annotationClass, method);
      
      if (annotation == null) annotation = findAnnotationInStereotypes(annotationClass, method, null);

      return annotation;
   }

   private <T extends Annotation> T findAnnotationInStereotypes(Class<T> annotationClass, Method method, Class<?> clazz)
   {
      Map<Class<? extends Annotation>, Set<Annotation>> stereotypes = Stereotypes.getInstance().getStereotypes();

      if (stereotypes.isEmpty())
      {
         return null;
      }

      Set<Class<T>> resourceStereotypes = collectStereotypes(method, clazz, stereotypes);

      for (Class<T> stereotype : resourceStereotypes)
      {
         T annotation = Utils.getAnnotation(annotationClass, stereotypes.get(stereotype));
         if (annotation != null)
         {
            return annotation;
         }
      }
      
      return null;
   }

   @SuppressWarnings("unchecked")
   private <T extends Annotation> Set<Class<T>> collectStereotypes(Method method, Class<?> clazz, Map<Class<? extends Annotation>, Set<Annotation>> stereotypes)
   {
      Set<Annotation> annotations = new HashSet<>();

      if (method != null)
      {
         annotations.addAll(Arrays.asList(method.getAnnotations()));
         annotations.addAll(Arrays.asList(method.getDeclaringClass().getAnnotations()));
      }

      if (clazz != null)
      {
         annotations.addAll(Arrays.asList(clazz.getAnnotations()));
      }

      Set<Class<T>> resourceStereotypes = new HashSet<>();

      for (Annotation annotation : annotations)
      {
         if (stereotypes.containsKey(annotation.annotationType()))
         {
            resourceStereotypes.add((Class<T>) annotation.annotationType());
         }
      }

      return resourceStereotypes;
   }
}
