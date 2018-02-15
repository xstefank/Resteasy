package org.jboss.resteasy.spi.metadata;

/**
 * @author <a href="mailto:mstefank@redhat.com">Martin Stefanko</a>
 * @version $Revision: 1 $
 */
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

}
