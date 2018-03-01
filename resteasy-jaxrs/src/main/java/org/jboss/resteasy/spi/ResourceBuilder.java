package org.jboss.resteasy.spi;

import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;

import java.lang.reflect.Method;

public interface ResourceBuilder
{
   /**
    * Picks a constructor from an annotated resource class based on spec rules
    *
    * @param annotatedResourceClass
    * @return
    */
   ResourceConstructor constructor(Class<?> annotatedResourceClass);

   /**
    * Build metadata from annotations on classes and methods
    *
    * @return
    */
   ResourceClass rootResourceFromAnnotations(Class<?> clazz);

   ResourceClass locatorFromAnnotations(Class<?> clazz);

   /**
    * Find the annotated resource method or sub-resource method / sub-resource locator in the class hierarchy.
    *
    * @param root The root resource class.
    * @param implementation The resource method or sub-resource method / sub-resource locator implementation
    * @return The annotated resource method or sub-resource method / sub-resource locator.
    */
   Method findAnnotatedMethod(final Class<?> root, final Method implementation);

}
