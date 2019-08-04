package org.jboss.resteasy.spi;

/**
 * Observer interface for {@link ResteasyDeployment} life cycle.
 */
public interface ResteasyDeploymentObserver
{
   void onStart(ResteasyDeployment deployment);

   void onStop(ResteasyDeployment deployment);
}
