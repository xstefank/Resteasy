package org.jboss.resteasy.plugins.validation.hibernate;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.validation.GeneralValidator;

/**
 * 
 * @author Leandro Ferro Luzia
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright May 23, 2013
 * 
 * @deprecated Use resteasy-validator-provider-11.
 */
@Deprecated
@Provider
public class ValidatorContextResolver extends AbstractValidatorContextResolver implements ContextResolver<GeneralValidator>
{
}
