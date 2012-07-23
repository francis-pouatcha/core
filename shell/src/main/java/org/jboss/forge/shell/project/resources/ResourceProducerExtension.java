/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.forge.shell.project.resources;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.plugins.Current;
import org.jboss.solder.reflection.annotated.AnnotatedTypeBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ResourceProducerExtension implements Extension
{
   private final Map<Class<?>, AnnotatedType<?>> typeOverrides = new HashMap<Class<?>, AnnotatedType<?>>();

   /**
    * Create a class to lazy load the builder, so it is not created unless needed. (Performance Fix) -- Mike Brock
    * (h/t to Stuart Douglas)
    */
   private class BuilderHolder<T>
   {
      private AnnotatedTypeBuilder<T> builder;
      private ProcessAnnotatedType<T> event;
      
      public BuilderHolder(AnnotatedTypeBuilder<T> builder, ProcessAnnotatedType<T> event) {
         this.builder = builder;
         this.event = event;
      }
      
      public AnnotatedTypeBuilder<T> getBuilder()
      {
         if (builder == null) {
            builder = new AnnotatedTypeBuilder<T>();
            builder.readFromType(event.getAnnotatedType());
         }
         return builder;
      }
   }
   public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event)
   {

      final BuilderHolder<T> builderHolder = new BuilderHolder<T>(null,event);

      boolean modifiedType = false;

      for (AnnotatedConstructor<T> c : event.getAnnotatedType().getConstructors())
      {
         if (c.isAnnotationPresent(Current.class))
         {
            for (AnnotatedParameter<?> p : c.getParameters())
            {
               if (p.getTypeClosure().contains(Resource.class))
               {
                  builderHolder.getBuilder().overrideConstructorParameterType(c.getJavaMember(), p.getPosition(),
                           Resource.class);
                  modifiedType = true;
               }
            }
         }
      }

      for (AnnotatedField<?> f : event.getAnnotatedType().getFields())
      {
         if (f.isAnnotationPresent(Current.class))
         {
            builderHolder.getBuilder().overrideFieldType(f.getJavaMember(), Resource.class);
            modifiedType = true;
         }
      }

      if (modifiedType)
      {
         AnnotatedType<T> replacement = builderHolder.getBuilder().create();
         typeOverrides.put(replacement.getJavaClass(), replacement);
         event.setAnnotatedType(replacement);
      }
   }
}
