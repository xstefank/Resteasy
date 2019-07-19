package org.jboss.resteasy.core;

import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.jboss.resteasy.microprofile.config.ResteasyConfigProvider;
import org.jboss.resteasy.plugins.interceptors.RoleBasedSecurityFeature;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.ServerFormUrlEncodedProvider;
import org.jboss.resteasy.plugins.server.resourcefactory.JndiComponentResourceFactory;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.resteasy_jaxrs.i18n.LogMessages;
import org.jboss.resteasy.resteasy_jaxrs.i18n.Messages;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyConfiguration;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GetRestful;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is used to configure and initialize the core components of RESTEasy.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResteasyDeploymentImpl implements ResteasyDeployment
{
   private boolean widerRequestMatching;
   private boolean useContainerFormParams = false;
   private boolean deploymentSensitiveFactoryEnabled = false;
   private boolean asyncJobServiceEnabled = false;
   private boolean addCharset = true;
   private int asyncJobServiceMaxJobResults = 100;
   private long asyncJobServiceMaxWait = 300000;
   private int asyncJobServiceThreadPoolSize = 100;
   private String asyncJobServiceBasePath = "/asynch/jobs";
   private String applicationClass;
   private String injectorFactoryClass;
   private InjectorFactory injectorFactory;
   private Application application;
   private boolean registerBuiltin = true;
   private List<String> scannedResourceClasses = new ArrayList<String>();
   private List<String> scannedProviderClasses = new ArrayList<String>();
   private List<String> scannedJndiComponentResources = new ArrayList<String>();
   private List<String> jndiComponentResources = new ArrayList<String>();
   private List<String> providerClasses = new ArrayList<String>();
   private List<Class> actualProviderClasses = new ArrayList<Class>();
   private List<Object> providers = new ArrayList<Object>();
   private boolean securityEnabled = false;
   private List<String> jndiResources = new ArrayList<String>();
   private List<String> resourceClasses = new ArrayList<String>();
   private List<String> unwrappedExceptions = new ArrayList<String>();
   private List<Class> actualResourceClasses = new ArrayList<Class>();
   private List<ResourceFactory> resourceFactories = new ArrayList<ResourceFactory>();
   private List<Object> resources = new ArrayList<Object>();
   private Map<String, String> mediaTypeMappings = new HashMap<String, String>();
   private Map<String, String> languageExtensions = new HashMap<String, String>();
   private Map<Class, Object> defaultContextObjects = new HashMap<Class, Object>();
   private Map<String, String> constructedDefaultContextObjects = new HashMap<String, String>();
   private Registry registry;
   private Dispatcher dispatcher;
   private ResteasyProviderFactory providerFactory;
   private ThreadLocalResteasyProviderFactory threadLocalProviderFactory;
   private String paramMapping;
   private Map<String, Object> properties = new TreeMap<String, Object>();
   private static List<ResteasyDeploymentObserver> deploymentObservers = new ArrayList<>();

   public void start()
   {
      try
      {
         startInternal();
      }
      finally
      {
         ThreadLocalResteasyProviderFactory.pop();
      }
   }

   @SuppressWarnings(value = {"unchecked", "deprecation"})
   private void startInternal()
   {
      // it is very important that each deployment create their own provider factory
      // this allows each WAR to have their own set of providers
      if (providerFactory == null) providerFactory = ResteasyProviderFactory.newInstance();
      providerFactory.setRegisterBuiltins(registerBuiltin);

      Object tracingText;
      Object thresholdText;

      Config config = ResteasyConfigProvider.getConfig();
      tracingText = config.getOptionalValue(ResteasyContextParameters.RESTEASY_TRACING_TYPE, String.class).orElse(null);
      thresholdText = config.getOptionalValue(ResteasyContextParameters.RESTEASY_TRACING_THRESHOLD, String.class).orElse(null);
      Object context = getDefaultContextObjects().get(ResteasyConfiguration.class);

      if (tracingText != null) {
         providerFactory.property(ResteasyContextParameters.RESTEASY_TRACING_TYPE, tracingText);
      } else {
         if (context != null) {
            tracingText = ((ResteasyConfiguration) context).getParameter(ResteasyContextParameters.RESTEASY_TRACING_TYPE);
            if (tracingText != null) {
               providerFactory.property(ResteasyContextParameters.RESTEASY_TRACING_TYPE, tracingText);
            }
         }
      }

      if (thresholdText != null) {
         providerFactory.getMutableProperties().put(ResteasyContextParameters.RESTEASY_TRACING_THRESHOLD, thresholdText);
      } else {

         if (context != null) {
            thresholdText = ((ResteasyConfiguration) context).getInitParameter(ResteasyContextParameters.RESTEASY_TRACING_THRESHOLD);
            if (thresholdText != null) {
               providerFactory.getMutableProperties().put(ResteasyContextParameters.RESTEASY_TRACING_THRESHOLD, thresholdText);
            }
         }
      }

      if (deploymentSensitiveFactoryEnabled)
      {
         // the ThreadLocalResteasyProviderFactory pushes and pops this deployments parentProviderFactory
         // on a ThreadLocal stack.  This allows each application/WAR to have their own parentProviderFactory
         // and still be able to call ResteasyProviderFactory.getInstance()
         if (!(providerFactory instanceof ThreadLocalResteasyProviderFactory))
         {
            if (ResteasyProviderFactory.peekInstance() == null || !(ResteasyProviderFactory.peekInstance() instanceof ThreadLocalResteasyProviderFactory))
            {

               threadLocalProviderFactory = new ThreadLocalResteasyProviderFactory(providerFactory);
               ResteasyProviderFactory.setInstance(threadLocalProviderFactory);
            }
            else
            {
               ThreadLocalResteasyProviderFactory.push(providerFactory);
            }
         }
         else
         {
            ThreadLocalResteasyProviderFactory.push(providerFactory);
         }
      }
      else
      {
         ResteasyProviderFactory.setInstance(providerFactory);
      }


      if (asyncJobServiceEnabled)
      {
         AsynchronousDispatcher asyncDispatcher;
         if (dispatcher == null) {
            asyncDispatcher = new AsynchronousDispatcher(providerFactory);
            dispatcher = asyncDispatcher;
         } else {
            asyncDispatcher = (AsynchronousDispatcher) dispatcher;
         }
         asyncDispatcher.setMaxCacheSize(asyncJobServiceMaxJobResults);
         asyncDispatcher.setMaxWaitMilliSeconds(asyncJobServiceMaxWait);
         asyncDispatcher.setThreadPoolSize(asyncJobServiceThreadPoolSize);
         asyncDispatcher.setBasePath(asyncJobServiceBasePath);
         asyncDispatcher.getUnwrappedExceptions().addAll(unwrappedExceptions);
         asyncDispatcher.start();
      }
      else
      {
         SynchronousDispatcher dis;
         if (dispatcher == null) {
            dis = new SynchronousDispatcher(providerFactory);
            dispatcher = dis;
         } else {
            dis = (SynchronousDispatcher) dispatcher;
         }
         dis.getUnwrappedExceptions().addAll(unwrappedExceptions);
      }
      registry = dispatcher.getRegistry();
      if (widerRequestMatching)
      {
         ((ResourceMethodRegistry)registry).setWiderMatching(widerRequestMatching);
      }


      dispatcher.getDefaultContextObjects().putAll(defaultContextObjects);
      dispatcher.getDefaultContextObjects().put(Configurable.class, providerFactory);
      dispatcher.getDefaultContextObjects().put(Configuration.class, providerFactory);
      dispatcher.getDefaultContextObjects().put(Providers.class, providerFactory);
      dispatcher.getDefaultContextObjects().put(Registry.class, registry);
      dispatcher.getDefaultContextObjects().put(Dispatcher.class, dispatcher);
      dispatcher.getDefaultContextObjects().put(InternalDispatcher.class, InternalDispatcher.getInstance());
      dispatcher.getDefaultContextObjects().put(ResteasyDeployment.class, this);

      // push context data so we can inject it
      Map contextDataMap = ResteasyContext.getContextDataMap();
      contextDataMap.putAll(dispatcher.getDefaultContextObjects());

      try
      {
         if (injectorFactory == null && injectorFactoryClass != null)
         {
            try
            {
               Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(injectorFactoryClass);
               injectorFactory = (InjectorFactory) clazz.newInstance();
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException(Messages.MESSAGES.unableToFindInjectorFactory(), cnfe);
            }
            catch (Exception e)
            {
               throw new RuntimeException(Messages.MESSAGES.unableToInstantiateInjectorFactory(), e);
            }
         }
         if (injectorFactory != null)
         {
            providerFactory.setInjectorFactory(injectorFactory);
         }
         // feed context data map with constructed objects
         // see ResteasyContextParameters.RESTEASY_CONTEXT_OBJECTS
         if (constructedDefaultContextObjects != null && constructedDefaultContextObjects.size() > 0)
         {
            for (Map.Entry<String, String> entry : constructedDefaultContextObjects.entrySet())
            {
               Class<?> key = null;
               try
               {
                  key = Thread.currentThread().getContextClassLoader().loadClass(entry.getKey());
               }
               catch (ClassNotFoundException e)
               {
                  throw new RuntimeException(Messages.MESSAGES.unableToInstantiateContextObject(entry.getKey()), e);
               }
               Object obj = createFromInjectorFactory(entry.getValue(), providerFactory);
               LogMessages.LOGGER.creatingContextObject(entry.getKey(), entry.getValue());
               defaultContextObjects.put(key, obj);
               dispatcher.getDefaultContextObjects().put(key, obj);
               contextDataMap.put(key, obj);

            }
         }

         if (securityEnabled)
         {
            providerFactory.register(RoleBasedSecurityFeature.class);
         }


         if (registerBuiltin)
         {
            providerFactory.setRegisterBuiltins(true);
            RegisterBuiltin.register(providerFactory);

            // having problems using form parameters from container for a couple of TCK tests.  I couldn't figure out
            // why, specifically:
            // com/sun/ts/tests/jaxrs/spec/provider/standardhaspriority/JAXRSClient.java#readWriteMapProviderTest_from_standalone                                               Failed. Test case throws exception: [JAXRSCommonClient] null failed!  Check output for cause of failure.
            // com/sun/ts/tests/jaxrs/spec/provider/standardwithjaxrsclient/JAXRSClient.java#mapElementProviderTest_from_standalone                                             Failed. Test case throws exception: returned MultivaluedMap is null
            providerFactory.registerProviderInstance(new ServerFormUrlEncodedProvider(useContainerFormParams), null, null, true);
         }
         else
         {
            providerFactory.setRegisterBuiltins(false);
         }

         if (applicationClass != null)
         {
            application = createApplication(applicationClass, dispatcher, providerFactory);

         }

         // register all providers
         registration();

         if (paramMapping != null)
         {
            providerFactory.getContainerRequestFilterRegistry().registerSingleton(new AcceptParameterHttpPreprocessor(paramMapping));
         }

         AcceptHeaderByFileSuffixFilter suffixNegotiationFilter = null;
         if (mediaTypeMappings != null)
         {
            Map<String, MediaType> extMap = new HashMap<String, MediaType>();
            for (Map.Entry<String, String> ext : mediaTypeMappings.entrySet())
            {
               String value = ext.getValue();
               extMap.put(ext.getKey().trim(), MediaType.valueOf(value.trim()));
            }

            if (suffixNegotiationFilter == null)
            {
               suffixNegotiationFilter = new AcceptHeaderByFileSuffixFilter();
               providerFactory.getContainerRequestFilterRegistry().registerSingleton(suffixNegotiationFilter);
            }
            suffixNegotiationFilter.setMediaTypeMappings(extMap);
         }


         if (languageExtensions != null)
         {
            if (suffixNegotiationFilter == null)
            {
               suffixNegotiationFilter = new AcceptHeaderByFileSuffixFilter();
               providerFactory.getContainerRequestFilterRegistry().registerSingleton(suffixNegotiationFilter);
            }
            suffixNegotiationFilter.setLanguageMappings(languageExtensions);
         }

         for (ResteasyDeploymentObserver observer : deploymentObservers)
         {
            observer.onStart(this);
         }
      }
      finally
      {
         ResteasyContext.removeContextDataLevel();
      }
   }

   public void merge(ResteasyDeployment other)
   {
      scannedResourceClasses.addAll(other.getScannedResourceClasses());
      scannedProviderClasses.addAll(other.getScannedProviderClasses());
      scannedJndiComponentResources.addAll(other.getScannedJndiComponentResources());

      jndiComponentResources.addAll(other.getJndiComponentResources());
      providerClasses.addAll(other.getProviderClasses());
      actualProviderClasses.addAll(other.getActualProviderClasses());
      providers.addAll(other.getProviders());

      jndiResources.addAll(other.getJndiResources());
      resourceClasses.addAll(other.getResourceClasses());
      unwrappedExceptions.addAll(other.getUnwrappedExceptions());
      actualResourceClasses.addAll(other.getActualResourceClasses());
      resourceFactories.addAll(other.getResourceFactories());
      resources.addAll(other.getResources());

      mediaTypeMappings.putAll(other.getMediaTypeMappings());
      languageExtensions.putAll(other.getLanguageExtensions());

      defaultContextObjects.putAll(other.getDefaultContextObjects());
      constructedDefaultContextObjects.putAll(other.getConstructedDefaultContextObjects());
   }

   public static Application createApplication(String applicationClass, Dispatcher dispatcher, ResteasyProviderFactory providerFactory)
   {
      Class<?> clazz = null;
      try
      {
         clazz = Thread.currentThread().getContextClassLoader().loadClass(applicationClass);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }

      Application app = (Application)providerFactory.createProviderInstance(clazz);
      dispatcher.getDefaultContextObjects().put(Application.class, app);
      ResteasyContext.pushContext(Application.class, app);
      PropertyInjector propertyInjector = providerFactory.getInjectorFactory().createPropertyInjector(clazz, providerFactory);
      propertyInjector.inject(app, false);
      return app;
   }

   private static Object createFromInjectorFactory(String classname, ResteasyProviderFactory providerFactory)
   {
      Class<?> clazz = null;
      try
      {
         clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }

      Object obj = providerFactory.injectedInstance(clazz);

      return obj;
   }

   public void registration()
   {
      boolean useScanning = true;
      if (application != null)
      {
         dispatcher.getDefaultContextObjects().put(Application.class, application);
         ResteasyContext.getContextDataMap().put(Application.class, application);
         if (processApplication(application))
         {
            // Application class registered something so don't use scanning data.  See JAX-RS spec for more detail.
            useScanning = false;
         }
      }

      if (useScanning && scannedProviderClasses != null)
      {
         for (String provider : scannedProviderClasses)
         {
            registerProvider(provider);
         }
      }

      if (providerClasses != null)
      {
         for (String provider : providerClasses)
         {
            registerProvider(provider);
         }
      }
      if (providers != null)
      {
         for (Object provider : providers)
         {
            providerFactory.registerProviderInstance(provider);
         }
      }

      for (Class actualProviderClass : actualProviderClasses)
      {
         providerFactory.registerProvider(actualProviderClass);
      }

      // All providers should be registered before resources because of interceptors.
      // interceptors must exist as they are applied only once when the resource is registered.

      if (useScanning && scannedJndiComponentResources != null)
      {
         for (String resource : scannedJndiComponentResources)
         {
            registerJndiComponentResource(resource);
         }
      }
      if (jndiComponentResources != null)
      {
         for (String resource : jndiComponentResources)
         {
            registerJndiComponentResource(resource);
         }
      }
      if (jndiResources != null)
      {
         for (String resource : jndiResources)
         {
            registry.addJndiResource(resource.trim());
         }
      }

      if (useScanning && scannedResourceClasses != null)
      {
         for (String resource : scannedResourceClasses)
         {
            Class clazz = null;
            try
            {
               clazz = Thread.currentThread().getContextClassLoader().loadClass(resource.trim());
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
            registry.addPerRequestResource(clazz);
         }
      }
      if (resourceClasses != null)
      {
         for (String resource : resourceClasses)
         {
            Class clazz = null;
            try
            {
               clazz = Thread.currentThread().getContextClassLoader().loadClass(resource.trim());
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
            registry.addPerRequestResource(clazz);
         }
      }

      if (resources != null)
      {
         for (Object obj : resources)
         {
            registry.addSingletonResource(obj);
         }
      }

      for (Class actualResourceClass : actualResourceClasses)
      {
         registry.addPerRequestResource(actualResourceClass);
      }

      for (ResourceFactory factory : resourceFactories)
      {
         registry.addResourceFactory(factory);
      }
      registry.checkAmbiguousUri();
   }

   private void registerJndiComponentResource(String resource)
   {
      String[] config = resource.trim().split(";");
      if (config.length < 3)
      {
         throw new RuntimeException(Messages.MESSAGES.jndiComponentResourceNotSetCorrectly());
      }
      String jndiName = config[0];
      Class clazz = null;
      try
      {
         clazz = Thread.currentThread().getContextClassLoader().loadClass(config[1]);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(Messages.MESSAGES.couldNotFindClassJndi(config[1]), e);
      }
      boolean cacheRefrence = Boolean.valueOf(config[2].trim());
      JndiComponentResourceFactory factory = new JndiComponentResourceFactory(jndiName, clazz, cacheRefrence);
      getResourceFactories().add(factory);

   }

   public void stop()
   {
      if (asyncJobServiceEnabled)
      {
         ((AsynchronousDispatcher) dispatcher).stop();
      }

      ResteasyProviderFactory.clearInstanceIfEqual(threadLocalProviderFactory);
      ResteasyProviderFactory.clearInstanceIfEqual(providerFactory);

      for (ResteasyDeploymentObserver observer : deploymentObservers)
      {
         observer.onStop(this);
      }
   }

   /**
    * @param config application
    * @return whether application class registered anything. i.e. whether scanning metadata should be used or not
    */
   private boolean processApplication(Application config)
   {
      LogMessages.LOGGER.deployingApplication(Application.class.getName(), config.getClass());
      boolean registered = false;
      Set<Class<?>> classes = config.getClasses();
      if (classes != null)
      {
         for (Class clazz : classes)
         {
            if (GetRestful.isRootResource(clazz))
            {
               LogMessages.LOGGER.addingClassResource(clazz.getName(), config.getClass());
               actualResourceClasses.add(clazz);
               registered = true;
            }
            else
            {
               LogMessages.LOGGER.addingProviderClass(clazz.getName(), config.getClass());
               actualProviderClasses.add(clazz);
               registered = true;
            }
         }
      }
      Set<Object> singletons = config.getSingletons();
      if (singletons != null)
      {
         for (Object obj : singletons)
         {
            if (GetRestful.isRootResource(obj.getClass()))
            {
               if (actualResourceClasses.contains(obj.getClass()))
               {
                  LogMessages.LOGGER.singletonClassAlreadyDeployed("resource", obj.getClass().getName());
               }
               else
               {
                  LogMessages.LOGGER.addingSingletonResource(obj.getClass().getName(), config.getClass());
                  resources.add(obj);
                  registered = true;
               }
            }
            else
            {
               if (actualProviderClasses.contains(obj.getClass()))
               {
                  LogMessages.LOGGER.singletonClassAlreadyDeployed("provider", obj.getClass().getName());
               }
               else
               {
                  LogMessages.LOGGER.addingProviderSingleton(obj.getClass().getName(), config.getClass());
                  providers.add(obj);
                  registered = true;
               }
            }
         }
      }
      final Map<String, Object> properties = config.getProperties();
      if (properties != null && !properties.isEmpty())
      {
         Feature applicationPropertiesRegistrationFeature = new Feature()
         {
            @Override
            public boolean configure(FeatureContext featureContext)
            {
               for (Map.Entry<String, Object> property : properties.entrySet())
               {
                  featureContext = featureContext.property(property.getKey(), property.getValue());
               }
               return true;
            }
         };
         this.providers.add(0, applicationPropertiesRegistrationFeature);
      }
      return registered;
   }

   private void registerProvider(String clazz)
   {
      Class provider = null;
      try
      {
         provider = Thread.currentThread().getContextClassLoader().loadClass(clazz.trim());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      providerFactory.registerProvider(provider);
   }

   public boolean isUseContainerFormParams()
   {
      return useContainerFormParams;
   }

   public void setUseContainerFormParams(boolean useContainerFormParams)
   {
      this.useContainerFormParams = useContainerFormParams;
   }

   public List<String> getJndiComponentResources()
   {
      return jndiComponentResources;
   }

   public void setJndiComponentResources(List<String> jndiComponentResources)
   {
      this.jndiComponentResources = jndiComponentResources;
   }

   public String getApplicationClass()
   {
      return applicationClass;
   }

   public void setApplicationClass(String applicationClass)
   {
      this.applicationClass = applicationClass;
   }

   public String getInjectorFactoryClass()
   {
      return injectorFactoryClass;
   }

   public void setInjectorFactoryClass(String injectorFactoryClass)
   {
      this.injectorFactoryClass = injectorFactoryClass;
   }

   public boolean isDeploymentSensitiveFactoryEnabled()
   {
      return deploymentSensitiveFactoryEnabled;
   }

   public void setDeploymentSensitiveFactoryEnabled(boolean deploymentSensitiveFactoryEnabled)
   {
      this.deploymentSensitiveFactoryEnabled = deploymentSensitiveFactoryEnabled;
   }

   public boolean isAsyncJobServiceEnabled()
   {
      return asyncJobServiceEnabled;
   }

   public void setAsyncJobServiceEnabled(boolean asyncJobServiceEnabled)
   {
      this.asyncJobServiceEnabled = asyncJobServiceEnabled;
   }

   public int getAsyncJobServiceMaxJobResults()
   {
      return asyncJobServiceMaxJobResults;
   }

   public void setAsyncJobServiceMaxJobResults(int asyncJobServiceMaxJobResults)
   {
      this.asyncJobServiceMaxJobResults = asyncJobServiceMaxJobResults;
   }

   public long getAsyncJobServiceMaxWait()
   {
      return asyncJobServiceMaxWait;
   }

   public void setAsyncJobServiceMaxWait(long asyncJobServiceMaxWait)
   {
      this.asyncJobServiceMaxWait = asyncJobServiceMaxWait;
   }

   public int getAsyncJobServiceThreadPoolSize()
   {
      return asyncJobServiceThreadPoolSize;
   }

   public void setAsyncJobServiceThreadPoolSize(int asyncJobServiceThreadPoolSize)
   {
      this.asyncJobServiceThreadPoolSize = asyncJobServiceThreadPoolSize;
   }

   public String getAsyncJobServiceBasePath()
   {
      return asyncJobServiceBasePath;
   }

   public void setAsyncJobServiceBasePath(String asyncJobServiceBasePath)
   {
      this.asyncJobServiceBasePath = asyncJobServiceBasePath;
   }

   public Application getApplication()
   {
      return application;
   }

   public void setApplication(Application application)
   {
      this.application = application;
   }

   public boolean isRegisterBuiltin()
   {
      return registerBuiltin;
   }

   public void setRegisterBuiltin(boolean registerBuiltin)
   {
      this.registerBuiltin = registerBuiltin;
   }

   public List<String> getProviderClasses()
   {
      return providerClasses;
   }

   public void setProviderClasses(List<String> providerClasses)
   {
      this.providerClasses = providerClasses;
   }

   public List<Object> getProviders()
   {
      return providers;
   }

   public void setProviders(List<Object> providers)
   {
      this.providers = providers;
   }

   public List<Class> getActualProviderClasses()
   {
      return actualProviderClasses;
   }

   public void setActualProviderClasses(List<Class> actualProviderClasses)
   {
      this.actualProviderClasses = actualProviderClasses;
   }

   public List<Class> getActualResourceClasses()
   {
      return actualResourceClasses;
   }

   public void setActualResourceClasses(List<Class> actualResourceClasses)
   {
      this.actualResourceClasses = actualResourceClasses;
   }

   public boolean isSecurityEnabled()
   {
      return securityEnabled;
   }

   public void setSecurityEnabled(boolean securityEnabled)
   {
      this.securityEnabled = securityEnabled;
   }

   public List<String> getJndiResources()
   {
      return jndiResources;
   }

   public void setJndiResources(List<String> jndiResources)
   {
      this.jndiResources = jndiResources;
   }

   public List<String> getResourceClasses()
   {
      return resourceClasses;
   }

   public void setResourceClasses(List<String> resourceClasses)
   {
      this.resourceClasses = resourceClasses;
   }

   public Map<String, String> getMediaTypeMappings()
   {
      return mediaTypeMappings;
   }

   public void setMediaTypeMappings(Map<String, String> mediaTypeMappings)
   {
      this.mediaTypeMappings = mediaTypeMappings;
   }

   public List<Object> getResources()
   {
      return resources;
   }

   public void setResources(List<Object> resources)
   {
      this.resources = resources;
   }

   public Map<String, String> getLanguageExtensions()
   {
      return languageExtensions;
   }

   public void setLanguageExtensions(Map<String, String> languageExtensions)
   {
      this.languageExtensions = languageExtensions;
   }

   public Registry getRegistry()
   {
      return registry;
   }

   public void setRegistry(Registry registry)
   {
      this.registry = registry;
   }

   public Dispatcher getDispatcher()
   {
      return dispatcher;
   }

   public void setDispatcher(Dispatcher dispatcher)
   {
      this.dispatcher = dispatcher;
   }

   public ResteasyProviderFactory getProviderFactory()
   {
      return providerFactory;
   }

   public void setProviderFactory(ResteasyProviderFactory providerFactory)
   {
      this.providerFactory = providerFactory;
   }

   public void setMediaTypeParamMapping(String paramMapping)
   {
      this.paramMapping = paramMapping;
   }

   public List<ResourceFactory> getResourceFactories()
   {
      return resourceFactories;
   }

   public void setResourceFactories(List<ResourceFactory> resourceFactories)
   {
      this.resourceFactories = resourceFactories;
   }

   public List<String> getUnwrappedExceptions()
   {
      return unwrappedExceptions;
   }

   public void setUnwrappedExceptions(List<String> unwrappedExceptions)
   {
      this.unwrappedExceptions = unwrappedExceptions;
   }

   public Map<String, String> getConstructedDefaultContextObjects()
   {
      return constructedDefaultContextObjects;
   }

   public void setConstructedDefaultContextObjects(Map<String, String> constructedDefaultContextObjects)
   {
      this.constructedDefaultContextObjects = constructedDefaultContextObjects;
   }

   public Map<Class, Object> getDefaultContextObjects()
   {
      return defaultContextObjects;
   }

   public void setDefaultContextObjects(Map<Class, Object> defaultContextObjects)
   {
      this.defaultContextObjects = defaultContextObjects;
   }

   public List<String> getScannedResourceClasses()
   {
      return scannedResourceClasses;
   }

   public void setScannedResourceClasses(List<String> scannedResourceClasses)
   {
      this.scannedResourceClasses = scannedResourceClasses;
   }

   public List<String> getScannedProviderClasses()
   {
      return scannedProviderClasses;
   }

   public void setScannedProviderClasses(List<String> scannedProviderClasses)
   {
      this.scannedProviderClasses = scannedProviderClasses;
   }

   public List<String> getScannedJndiComponentResources()
   {
      return scannedJndiComponentResources;
   }

   public void setScannedJndiComponentResources(List<String> scannedJndiComponentResources)
   {
      this.scannedJndiComponentResources = scannedJndiComponentResources;
   }

   public boolean isWiderRequestMatching()
   {
      return widerRequestMatching;
   }

   public void setWiderRequestMatching(boolean widerRequestMatching)
   {
      this.widerRequestMatching = widerRequestMatching;
   }

   public boolean isAddCharset()
   {
      return addCharset;
   }

   public void setAddCharset(boolean addCharset)
   {
      this.addCharset = addCharset;
   }

   public InjectorFactory getInjectorFactory()
   {
      return injectorFactory;
   }

   public void setInjectorFactory(InjectorFactory injectorFactory)
   {
      this.injectorFactory = injectorFactory;
   }

   @Override
   public Object getProperty(String key) {
      return properties.get(key);
   }

   @Override
   public void setProperty(String key, Object value) {
      properties.put(key, value);
   }

   public static void registerObserver(ResteasyDeploymentObserver observer)
   {
      deploymentObservers.add(observer);
   }
}
