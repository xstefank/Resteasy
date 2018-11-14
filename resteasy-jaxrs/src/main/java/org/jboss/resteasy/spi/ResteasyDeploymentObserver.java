package org.jboss.resteasy.spi;

public interface ResteasyDeploymentObserver
{
   void deploymentCreated(ResteasyDeployment deployment);

   void deploymentDestroyed(ResteasyDeployment deployment);
}
