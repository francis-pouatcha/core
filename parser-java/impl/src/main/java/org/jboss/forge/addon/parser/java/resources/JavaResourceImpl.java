/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.parser.java.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jboss.forge.addon.parser.java.JavaSourceFactory;
import org.jboss.forge.addon.resource.AbstractFileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceException;
import org.jboss.forge.addon.resource.ResourceFacet;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.parser.ParserException;
import org.jboss.forge.parser.java.EnumConstant;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaEnum;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.java.Member;
import org.jboss.forge.parser.java.Method;

/**
 * @author Mike Brock
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class JavaResourceImpl extends AbstractFileResource<JavaResource> implements JavaResource
{
   private final JavaSourceFactory parser;

   public JavaResourceImpl(final ResourceFactory factory, JavaSourceFactory parser, final File file)
   {
      super(factory, file);
      this.parser = parser;
   }

   @Override
   public Resource<?> getChild(final String name)
   {
      List<Resource<?>> children = doListResources();
      List<Resource<?>> subset = new ArrayList<Resource<?>>();

      for (Resource<?> child : children)
      {
         if ((name != null) && (child instanceof AbstractJavaMemberResource<?>))
         {
            String childName = child.getName();
            if (((Member<?, ?>) child.getUnderlyingResourceObject()).getName().equals(name.trim())
                     || childName.equals(name))
            {
               subset.add(child);
            }
         }
      }

      if (subset.size() == 1)
      {
         return subset.get(0);
      }
      else if (subset.size() > 1)
      {
         throw new ResourceException("Ambiguous name [" + name + "], full type signature required");
      }
      else
      {
         return null;
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   protected synchronized List<Resource<?>> doListResources()
   {
      try
      {
         List<Resource<?>> list = new LinkedList<Resource<?>>();

         for (Member<?, ?> member : getJavaSource().getMembers())
         {
            if (member instanceof Field)
            {
               list.add(new JavaFieldResourceImpl(getResourceFactory(), this, (Field<? extends JavaSource<?>>) member));
            }
            else if (member instanceof Method)
            {
               list.add(new JavaMethodResourceImpl(getResourceFactory(), this, (Method<? extends JavaSource<?>>) member));
            }
            else
            {
               throw new UnsupportedOperationException("Unknown member type: " + member);
            }
         }

         if (getJavaSource() instanceof JavaEnum)
         {
            for (EnumConstant<JavaEnum> e : ((JavaEnum) getJavaSource()).getEnumConstants())
            {
               list.add(new EnumConstantResourceImpl(getResourceFactory(), this, e));
            }
         }

         return list;
      }
      catch (ParserException e)
      {
         return Collections.emptyList();
      }
      catch (FileNotFoundException e)
      {
         return Collections.emptyList();
      }
   }

   @Override
   public JavaResourceImpl setContents(final JavaSource<?> source)
   {
      setContents(source.toString());
      return this;
   }

   /**
    * Attempts to perform cast automatically. This can lead to problems.
    */
   @Override
   public JavaSource<?> getJavaSource() throws FileNotFoundException
   {
      return parser.parse(getResourceInputStream());
   }

   @Override
   public JavaResourceImpl createFrom(final File file)
   {
      return new JavaResourceImpl(resourceFactory, parser, file);
   }

   @Override
   public String toString()
   {
      try
      {
         return getJavaSource().getQualifiedName();
      }
      catch (FileNotFoundException e)
      {
         throw new ResourceException(e);
      }
      catch (Exception e)
      {
         return getName();
      }
   }

   @Override
   public boolean supports(ResourceFacet type)
   {
      return false;
   }
}
