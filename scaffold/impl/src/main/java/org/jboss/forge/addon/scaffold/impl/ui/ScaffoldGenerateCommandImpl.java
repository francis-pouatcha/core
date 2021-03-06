/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.scaffold.impl.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.scaffold.spi.ScaffoldProvider;
import org.jboss.forge.addon.scaffold.ui.ScaffoldGenerateCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Scaffold wizard
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class ScaffoldGenerateCommandImpl extends AbstractProjectCommand implements ScaffoldGenerateCommand
{
   @Inject
   @WithAttributes(label = "Scaffold Type", required = true, enabled = false)
   private UISelectOne<ScaffoldProvider> scaffoldType;

   @Inject
   @WithAttributes(label = "Target Directory", required = true)
   private UIInput<DirectoryResource> targetDirectory;

   @Inject
   @WithAttributes(label = "Overwrite existing files?")
   private UIInput<Boolean> overwrite;

   @Inject
   @WithAttributes(label = "Resources")
   private UISelectMany<FileResource<?>> resources;

   @Inject
   private ProjectFactory factory;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      // TODO
      builder.add(scaffoldType).add(resources).add(targetDirectory).add(overwrite);
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      ScaffoldProvider scaffold = scaffoldType.getValue();
      scaffold.validate(validator);
   }

   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Scaffold: Generate")
               .description("Generates the scaffold")
               .category(Categories.create("Scaffold", "Generate"));
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success();
   }

   @Override
   protected boolean isProjectRequired()
   {
      return false;
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return factory;
   }
}
