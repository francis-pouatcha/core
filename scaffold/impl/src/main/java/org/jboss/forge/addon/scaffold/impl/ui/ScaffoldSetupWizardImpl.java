/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.scaffold.impl.ui;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.scaffold.spi.ScaffoldContext;
import org.jboss.forge.addon.scaffold.spi.ScaffoldProvider;
import org.jboss.forge.addon.scaffold.ui.ScaffoldSetupWizard;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Scaffold wizard
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class ScaffoldSetupWizardImpl extends AbstractProjectCommand implements ScaffoldSetupWizard
{
   @Inject
   @WithAttributes(label = "Provider", required = true)
   private UISelectOne<ScaffoldProvider> provider;

   @Inject
   @WithAttributes(label = "Target Directory", required = true)
   private UIInput<DirectoryResource> target;

   @Inject
   @WithAttributes(label = "Overwrite existing files?", defaultValue = "false")
   private UIInput<Boolean> overwrite;

   @Inject
   @WithAttributes(label = "Install Templates in src/main/templates?", defaultValue = "false")
   private UIInput<Boolean> installTemplates;

   @Inject
   private ProjectFactory factory;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      overwrite.setEnabled(new Callable<Boolean>()
      {
         @Override
         public Boolean call() throws Exception
         {
            ScaffoldProvider selectedProvider = provider.getValue();
            ScaffoldContext scaffoldContext = newScaffoldContext();
            return selectedProvider != null && selectedProvider.needsOverwriteConfirmation(scaffoldContext);
         }
      });
      builder.add(provider).add(target).add(overwrite).add(installTemplates);
   }

   private ScaffoldContext newScaffoldContext()
   {
      return new ScaffoldContext(target.getValue(), overwrite.getValue(), installTemplates.getValue());
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      provider.getValue().validate(validator);
   }

   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Scaffold: Setup")
               .description("Setups the scaffold")
               .category(Categories.create("Scaffold", "Setup"));
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success();
   }

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return Results.navigateTo(provider.getValue().getSetupFlow());
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return factory;
   }
}
