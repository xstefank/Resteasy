package org.jboss.resteasy.test.cdi.stereotype.resource.provider;

import org.jboss.resteasy.test.cdi.stereotype.resource.stereotypes.ProviderStereotype;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@ProviderStereotype
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class DummyProvider implements MessageBodyWriter<Dummy>
{

   @Override
   public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return true;
   }

   @Override
   public void writeTo(Dummy dummy, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException
   {
      outputStream.write("Dummy provider foo".getBytes());
   }
}
