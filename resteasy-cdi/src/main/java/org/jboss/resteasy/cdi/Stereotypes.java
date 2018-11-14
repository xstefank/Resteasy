package org.jboss.resteasy.cdi;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyDeploymentObserver;
import org.jboss.resteasy.util.AnnotationResolver;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Stereotypes implements ResteasyDeploymentObserver
{
   private static final String JAVAX_WS_RS = "javax.ws.rs";
   private static final Logger log = Logger.getLogger(Stereotypes.class);

   private static final Map<Class<? extends Annotation>, Set<Annotation>> stereotypes = new HashMap<>();
   private static Map<Integer, CDIDeployment> stereotypedDeployments = new HashMap<>();
   
   private Stereotypes() {
      ResteasyDeployment.registerObserver(this);
      AnnotationResolver.setInstance(new CDIAnnotationResolver());
   }

   private static final Stereotypes instance = new Stereotypes();

   public static Stereotypes getInstance()
   {
      return instance;
   }

   public void addStereotype(Class<? extends Annotation> stereotypeClass, Class<?> clazz, BeanManager beanManager)
   {
      log.warn("Add stereotype - " + stereotypeClass);

      Set<Annotation> jaxRsAnnotations = new HashSet<>(collectJaxRsAnnotations(beanManager.getStereotypeDefinition(stereotypeClass), beanManager));

      CDIDeployment cdiDeployment = getCDIDeployment();

      if (Utils.getAnnotation(Path.class, jaxRsAnnotations) != null && clazz != null)
      {
         cdiDeployment.addResource(clazz);
      } else if (Utils.getAnnotation(Provider.class, jaxRsAnnotations) != null && clazz != null)
      {
         cdiDeployment.addProvider(clazz);
      }

      stereotypes.put(stereotypeClass, jaxRsAnnotations);
      log.error(stereotypes);
   }
   
   private CDIDeployment getCDIDeployment()
   {
      if (stereotypedDeployments.get(CDI.current().hashCode()) == null)
      {
         stereotypedDeployments.put(CDI.current().hashCode(), new CDIDeployment());
      }
      return stereotypedDeployments.get(CDI.current().hashCode());
   }

   private Set<Annotation> collectJaxRsAnnotations(Set<Annotation> annotations, BeanManager beanManager)
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

   private boolean isJaxRsAnnotation(Annotation annotation)
   {
      return annotation.annotationType().getName().startsWith(JAVAX_WS_RS);
   }
   
   public Map<Class<? extends Annotation>, Set<Annotation>> getStereotypes() {
      return Collections.unmodifiableMap(stereotypes);
   }

   @Override
   public void deploymentCreated(ResteasyDeployment deployment)
   {
      CDIDeployment cdiDeployment = stereotypedDeployments.get(CDI.current().hashCode());
      List<Class<?>> resourceClasses = cdiDeployment.getResourceClasses();

      if (resourceClasses != null)
      {
         for (Class<?> resource : resourceClasses)
         {
            deployment.getRegistry().addPerRequestResource(resource);
         }
      }

      List<Class<?>> providerClasses = cdiDeployment.getProviderClasses();
      if (providerClasses != null)
      {
         for (Class<?> provider : providerClasses)
         {
            deployment.getProviderFactory().registerProvider(provider);
         }
      }
   }

   @Override
   public void deploymentDestroyed(ResteasyDeployment deployment)
   {
      stereotypedDeployments.get(CDI.current().hashCode()).close();
   }
}
