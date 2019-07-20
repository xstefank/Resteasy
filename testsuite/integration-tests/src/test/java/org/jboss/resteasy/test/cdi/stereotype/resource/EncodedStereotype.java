package org.jboss.resteasy.test.cdi.stereotype.resource;

import javax.enterprise.inject.Stereotype;
import javax.ws.rs.Encoded;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Stereotype
@Encoded
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncodedStereotype
{
}
