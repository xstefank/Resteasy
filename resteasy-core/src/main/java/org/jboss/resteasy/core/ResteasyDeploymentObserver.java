package org.jboss.resteasy.core;

import org.jboss.resteasy.spi.ResteasyDeployment;

/**
 * Observer interface for {@link ResteasyDeployment} life cycle.
 */
public interface ResteasyDeploymentObserver
{
   void onStart(ResteasyDeployment deployment);

   void onStop(ResteasyDeployment deployment);
}
