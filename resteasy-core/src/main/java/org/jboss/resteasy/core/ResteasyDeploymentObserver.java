package org.jboss.resteasy.core;

import org.jboss.resteasy.spi.ResteasyDeployment;

public interface ResteasyDeploymentObserver
{
   void deploymentCreated(ResteasyDeployment deployment);

   void deploymentDestroyed(ResteasyDeployment deployment);
}
