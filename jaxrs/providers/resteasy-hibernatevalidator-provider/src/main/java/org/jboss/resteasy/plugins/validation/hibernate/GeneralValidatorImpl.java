package org.jboss.resteasy.plugins.validation.hibernate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodValidator;
import org.jboss.resteasy.api.validation.ConstraintType.Type;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.providers.validation.ConstraintTypeUtil;
import org.jboss.resteasy.plugins.providers.validation.ViolationsContainer;
import org.jboss.resteasy.plugins.validation.hibernate.i18n.LogMessages;
import org.jboss.resteasy.plugins.validation.hibernate.i18n.Messages;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ResteasyConfiguration;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.validation.GeneralValidatorCDI;
import org.jboss.resteasy.util.FindAnnotation;

/**
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright May 23, 2013
 * 
 * @deprecated Use resteasy-validator-provider-11.
 */
@Deprecated
@SuppressWarnings("serial")
public class GeneralValidatorImpl implements GeneralValidatorCDI
{  
   private Validator validator;
   private MethodValidator methodValidator;
   private ConstraintTypeUtil util = new ConstraintTypeUtil10();
   private boolean suppressPath;
   private boolean cdiActive;
   
   public static final String SUPPRESS_VIOLATION_PATH = "resteasy.validation.suppress.path";
   
   public abstract static class S1 extends AnnotationLiteral<Stateless> implements Stateless { }
   public static final Annotation STATELESS = new S1() 
   {
      @Override public String name() {return null;}
      @Override public String mappedName() {return null;}
      @Override public String description() {return null;}
   };

   public abstract static class S2 extends AnnotationLiteral<Stateful> implements Stateful { }
   public static final Annotation STATEFUL = new S2() 
   {
      @Override public String name() {return null;}
      @Override public String mappedName() {return null;}
      @Override public String description() {return null;}
   };

   public abstract static class S3 extends AnnotationLiteral<Stateful> implements Stateful { }
   public static final Annotation SINGLETON = new S3() 
   {
      @Override public String name() {return null;}
      @Override public String mappedName() {return null;}
      @Override public String description() {return null;}
   };
   
   public abstract static class S4 extends AnnotationLiteral<Local> implements Local { }
   public static final Annotation LOCAL = new S4() 
   {
      @Override
      public Class<?>[] value() {return null;}
   };

   public abstract static class S5 extends AnnotationLiteral<Remote> implements Remote { }
   public static final Annotation REMOTE = new S5() 
   {
      @Override
      public Class<?>[] value() {return null;}
   };
   
   public GeneralValidatorImpl(Validator validator, MethodValidator methodValidator)
   {
      this.validator = validator;
      this.methodValidator = methodValidator;
      
      try
      {
         cdiActive = ResteasyCdiExtension.isCDIActive();
      }
      catch (Throwable t)
      {
         // In case ResteasyCdiExtension is not on the classpath.
         LogMessages.LOGGER.debug(Messages.MESSAGES.cdiExtensionNotOnClasspath());
      }
      
      ResteasyConfiguration context = ResteasyProviderFactory.getContextData(ResteasyConfiguration.class);
      if (context != null)
      {
         String s = context.getParameter(SUPPRESS_VIOLATION_PATH);
         if (s != null)
         {
            suppressPath = Boolean.parseBoolean(s);
         }
      }
   }

   public void checkViolations(HttpRequest request)
   {
      // Called from resteasy-jaxrs only if two argument version of isValidatable() returns true.
      ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, null);
      Object target = violationsContainer.getTarget();
      if (target != null && !isWeldProxy(target.getClass()))
      {
         if (violationsContainer != null && violationsContainer.size() > 0)
         {
            throw new ResteasyViolationException(violationsContainer, request.getHttpHeaders().getAcceptableMediaTypes());
         }
      }
   }

   @Override
   public void checkViolationsfromCDI(HttpRequest request)
   {
      if (request == null)
      {
         return;
      }
      
      @SuppressWarnings("unchecked")
      ViolationsContainer<Object> violationsContainer = ViolationsContainer.class.cast(request.getAttribute(ViolationsContainer.class.getName()));
      if (violationsContainer != null && violationsContainer.size() > 0)
      {
         throw new ResteasyViolationException(violationsContainer, request.getHttpHeaders().getAcceptableMediaTypes());
      }
   }
   
   @Override
   public void validate(HttpRequest request, Object object, Class<?>... groups)
   {
      Set<ResteasyConstraintViolation> rcvs = new HashSet<ResteasyConstraintViolation>();
      try
      {
         Set<ConstraintViolation<Object>> cvs = validator.validate(object, groups);
         for (Iterator<ConstraintViolation<Object>> it = cvs.iterator(); it.hasNext(); )
         {
            ConstraintViolation<Object> cv = it.next();
            Type ct = util.getConstraintType(cv);
            String path = (suppressPath ? "*" : cv.getPropertyPath().toString());
            rcvs.add(new ResteasyConstraintViolation(ct, path, cv.getMessage(), (cv.getInvalidValue() == null ? "null" :cv.getInvalidValue().toString())));
         }
      }
      catch (Exception e)
      {
         ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, object);
         violationsContainer.setException(e);
         throw new ResteasyViolationException(violationsContainer);
      }
      ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, object);
      violationsContainer.addViolations(rcvs);
      /*
      if (rcvs.size() > 0)
      {
         throw new ResteasyViolationException(violationsContainer);
      }
      */
   }

   @Override
   public void validateAllParameters(HttpRequest request, Object object, Method method, Object[] parameterValues, Class<?>... groups)
   {
      if (isSessionBean(method.getDeclaringClass()) || isSessionBean(object.getClass()))
      {
         try
         {
            // This hack is for Hibernate Validator 4.x, which looks for the method in the bean interface.
            method = object.getClass().getMethod(method.getName(), method.getParameterTypes());
         } 
         catch (NoSuchMethodException e1)
         {
            // 
         }
         
         if (!isWeldProxy(object.getClass()))
         {
            Class<?>[] interfaces = getInterfaces(method.getDeclaringClass());
            if (interfaces.length > 0)
            {
//               object = getProxy(method.getDeclaringClass(), interfaces, object);
               object = getProxy(object.getClass(), interfaces, object);
            }
         }
      }
      
      Set<ResteasyConstraintViolation> rcvs = new HashSet<ResteasyConstraintViolation>();
      ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, object);
      try
      {
         Set<MethodConstraintViolation<Object>> cvs = methodValidator.validateAllParameters(object, method, parameterValues, groups);
         for (Iterator<MethodConstraintViolation<Object>> it = cvs.iterator(); it.hasNext(); )
         {
            ConstraintViolation<Object> cv = it.next();
            Type ct = util.getConstraintType(cv);
            Object o = cv.getInvalidValue();
            String value = (o == null ? "" : o.toString());
            String path = (suppressPath ? "*" : cv.getPropertyPath().toString());
            rcvs.add(new ResteasyConstraintViolation(ct, path, cv.getMessage(), value));
         }
      }
      catch (Exception e)
      {
         violationsContainer.setException(e);
         throw new ResteasyViolationException(violationsContainer);
      }
      violationsContainer.addViolations(rcvs);
      if (!isWeldProxy(object.getClass()) && violationsContainer.size() > 0) // ???
      {
         throw new ResteasyViolationException(violationsContainer, request.getHttpHeaders().getAcceptableMediaTypes());
      }
   }

   @Override
   public void validateReturnValue(HttpRequest request, Object object, Method method, Object returnValue, Class<?>... groups)
   {
      if (isSessionBean(method.getDeclaringClass()))
      {
         try
         {
            // This hack is for Hibernate Validator 4.x, which looks for the method in the bean interface.
            method = object.getClass().getMethod(method.getName(), method.getParameterTypes());
         } 
         catch (NoSuchMethodException e1)
         {
            // 
         }
      }
      
      Set<ResteasyConstraintViolation> rcvs = new HashSet<ResteasyConstraintViolation>();
      ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, object);
      try
      {
         Set<MethodConstraintViolation<Object>> cvs = methodValidator.validateReturnValue(object, method, returnValue, groups);
         for (Iterator<MethodConstraintViolation<Object>> it = cvs.iterator(); it.hasNext(); )
         {
            ConstraintViolation<Object> cv = it.next();
            Type ct = util.getConstraintType(cv);
            Object o = cv.getInvalidValue();
            String value = (o == null ? "" : o.toString());
            String path = (suppressPath ? "*" : cv.getPropertyPath().toString());
            rcvs.add(new ResteasyConstraintViolation(ct, path, cv.getMessage(), value));
         }
      }
      catch (Exception e)
      {
         violationsContainer.setException(e);
         throw new ResteasyViolationException(violationsContainer);
      }
      violationsContainer.addViolations(rcvs);
      if (violationsContainer.size() > 0)
      {
         throw new ResteasyViolationException(violationsContainer, request.getHttpHeaders().getAcceptableMediaTypes());
      }
   }
   
   @Override
   public boolean isValidatable(Class<?> clazz)
   {
      // Called from resteasy-jaxrs.
      if (cdiActive)
      {
         return false;
      }
      return checkIsValidatable(clazz);
   }

   public boolean isValidatable(Class<?> clazz, InjectorFactory injectorFactory)
   {
      try
      {
         // Called from resteasy-jaxrs.
         if (cdiActive && injectorFactory instanceof CdiInjectorFactory)
         {
            return false;
         }
      }
      catch (NoClassDefFoundError e)
      {
         // Shouldn't get here. Deliberately empty.
      }
      return checkIsValidatable(clazz);
   }
   
   @Override
   public boolean isValidatableFromCDI(Class<?> clazz)
   {
      return checkIsValidatable(clazz);
   }
   
   protected boolean checkIsValidatable(Class<?> clazz)
   {
      ValidateRequest resourceValidateRequest = FindAnnotation.findAnnotation(clazz.getAnnotations(), ValidateRequest.class);
      DoNotValidateRequest doNotValidateRequest = FindAnnotation.findAnnotation(clazz.getAnnotations(), DoNotValidateRequest.class);
      return resourceValidateRequest != null && doNotValidateRequest == null; 
   }
   
   @Override
   public boolean isMethodValidatable(Method m)
   {
      return checkIsMethodValidatable(m);
   }
   
   protected boolean checkIsMethodValidatable(Method m)
   {
      ValidateRequest resourceValidateRequest = FindAnnotation.findAnnotation(m.getDeclaringClass().getAnnotations(), ValidateRequest.class);
      ValidateRequest methodValidateRequest = FindAnnotation.findAnnotation(m.getAnnotations(), ValidateRequest.class);
      DoNotValidateRequest doNotValidateRequest = FindAnnotation.findAnnotation(m.getAnnotations(), DoNotValidateRequest.class);
      return (resourceValidateRequest != null || methodValidateRequest != null) && doNotValidateRequest == null;
   }
   
   @Override
   public void checkForConstraintViolations(HttpRequest request, Exception e)
   {
      if (e instanceof ConstraintViolationException)
      {
         ConstraintViolationException cve = ConstraintViolationException.class.cast(e);
         Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
         Set<ResteasyConstraintViolation> rcvs = new HashSet<ResteasyConstraintViolation>();
         try
         {
            for (Iterator<ConstraintViolation<?>> it = cvs.iterator(); it.hasNext(); )
            {
               ConstraintViolation<?> cv = it.next();
               Type ct = util.getConstraintType(cv);
               String path = (suppressPath ? "*" : cv.getPropertyPath().toString());
               rcvs.add(new ResteasyConstraintViolation(ct, path, cv.getMessage(), (cv.getInvalidValue() == null ? "null" :cv.getInvalidValue().toString())));
            }
         }
         catch (Exception e1)
         {
            ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, null);
            violationsContainer.setException(e);
            throw new ResteasyViolationException(violationsContainer);
         }
         if (rcvs.size() > 0)
         {
            ViolationsContainer<Object> violationsContainer = getViolationsContainer(request, null);
            violationsContainer.addViolations(rcvs);
            throw new ResteasyViolationException(violationsContainer);
         }
      }
      
      Throwable t = e.getCause();
      while (t != null && !(t instanceof ResteasyViolationException))
      {
         t = t.getCause();         
      }
      if (t instanceof ResteasyViolationException)
      {
         throw ResteasyViolationException.class.cast(t);
      }
   }
   
   protected ViolationsContainer<Object> getViolationsContainer(HttpRequest request, Object target)
   {
      if (request == null)
      {
         return new ViolationsContainer<Object>(target);
      }
      ViolationsContainer<Object> violationsContainer = ViolationsContainer.class.cast(request.getAttribute(ViolationsContainer.class.getName()));
      if (violationsContainer == null)
      {
         violationsContainer = new ViolationsContainer<Object>(target);
         request.setAttribute(ViolationsContainer.class.getName(), violationsContainer);
      }
      return violationsContainer;
   }
   
   private boolean isSessionBean(Class<?> clazz)
   {
      if (clazz.getName().indexOf("$$$view") >= 0)
      {
         return true;
      }
      while (clazz != null)
      {
         if (clazz.getAnnotation(STATELESS.annotationType()) != null 
               || clazz.getAnnotation(STATEFUL.annotationType()) != null
               || clazz.getAnnotation(SINGLETON.annotationType()) != null
               || clazz.getAnnotation(LOCAL.annotationType()) != null
               || clazz.getAnnotation(REMOTE.annotationType()) != null)
         {
            return true;
         }
         clazz = clazz.getSuperclass();
      }
      return false;
   }
   
   private Class<?>[] getInterfaces(Class<?> clazz)
   {
      ArrayList<Class<?>> list = new ArrayList<Class<?>>();
      getInterfaces(list, clazz);
      return list.toArray(new Class<?>[] {});
   }
   
   private void getInterfaces(ArrayList<Class<?>> list, Class<?> clazz)
   {
      Class<?>[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; i++)
      {
         list.add(interfaces[i]);
         getInterfaces(list, interfaces[i]);
      }
   }
   
   private Object getProxy(Class<?> clazz, Class<?>[] interfaces, final Object delegate)
   {
      InvocationHandler handler = new InvocationHandler()
      {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            return method.invoke(delegate, args);
         }
      };
      Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, handler);
      return proxy;
   }
   
   private static final String PROXY_OBJECT_INTERFACE_NAME = "javassist.util.proxy.ProxyObject";
   private static final String TARGET_INSTANCE_INTERFACE_NAME = "org.jboss.interceptor.util.proxy.TargetInstanceProxy";

   /**
    * Whether the given class is a proxy created by Weld or not. This is
    * the case if the given class implements the interface
    * {@code org.jboss.weld.bean.proxy.ProxyObject}.
    * 
    * Borrowed from org.jboss.resteasy.spi.metadata.ResourceBuilder.
    *
    * @param clazz the class of interest
    *
    * @return {@code true} if the given class is a Weld proxy,
    * {@code false} otherwise
    */
   private static boolean isWeldProxy(Class<?> clazz)
   {
      boolean foundProxyObject = false;
      boolean foundTargetInstance = false;
      
      for ( Class<?> implementedInterface : clazz.getInterfaces() )
      {
         if ( implementedInterface.getName().equals( PROXY_OBJECT_INTERFACE_NAME ) )
         {
            foundProxyObject = true;
         }
         else if ( implementedInterface.getName().equals( TARGET_INSTANCE_INTERFACE_NAME ) )
         {
            foundTargetInstance = true;
         }
         if (foundProxyObject && foundTargetInstance)
         {
            return true;
         }
      }
      
      if (clazz.getName().contains("_$$_Weld"))
      {
         return true;
      }

      return false;
   }
}
