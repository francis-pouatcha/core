/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.jboss.forge.container.util.Assert;

/**
 * Represents a {@link URL} resource
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class URLResource extends VirtualResource<URL>
{
   private final URL resource;

   public URLResource(ResourceFactory factory, URL resource)
   {
      super(factory, null);
      Assert.notNull(resource, "URL resource cannot be null");
      this.resource = resource;
   }

   protected URLResource(ResourceFactory factory, Resource<?> parent, URL resource)
   {
      super(factory, parent);
      Assert.notNull(resource, "URL resource cannot be null");
      this.resource = resource;
   }

   @Override
   public boolean delete() throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("Cannot delete URL resources.");
   }

   @Override
   public boolean delete(boolean recursive) throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("Cannot delete URL resources.");
   }

   @Override
   public String getName()
   {
      return resource.toExternalForm();
   }

   @Override
   public URL getUnderlyingResourceObject()
   {
      return resource;
   }

   @Override
   public InputStream getResourceInputStream()
   {
      try
      {
         return resource.openStream();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not open stream", e);
      }
   }

   @Override
   public boolean exists()
   {
      HttpURLConnection connection;
      try
      {
         connection = (HttpURLConnection) resource.openConnection();
         connection.setRequestMethod("HEAD");
         int responseCode = connection.getResponseCode();
         if (responseCode != 200)
         {
            return false;
         }
      }
      catch (IOException e)
      {
         return false;
      }
      return true;
   }

   @Override
   protected List<Resource<?>> doListResources()
   {
      return Collections.emptyList();
   }

   @Override
   public boolean supports(ResourceFacet type)
   {
      return false;
   }
}
