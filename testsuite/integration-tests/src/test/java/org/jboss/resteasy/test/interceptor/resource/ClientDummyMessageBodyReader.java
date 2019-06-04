package org.jboss.resteasy.test.interceptor.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes(ClientDummyMessageBodyReader.CUSTOM_MEDIA_TYPE)
public class ClientDummyMessageBodyReader implements MessageBodyReader<String>
{
   public static final String CUSTOM_MEDIA_TYPE = "application/custom";
   
   public static final String MESSAGE = "Custom changed message";

   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return true;
   }

   @Override
   public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      return MESSAGE;
   }
}
