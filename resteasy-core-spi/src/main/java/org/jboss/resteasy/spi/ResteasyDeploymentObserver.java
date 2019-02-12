package org.jboss.resteasy.spi;

public interface ResteasyDeploymentObserver
{
   void start(ResteasyDeployment deployment);

   void stop(ResteasyDeployment deployment);
}
