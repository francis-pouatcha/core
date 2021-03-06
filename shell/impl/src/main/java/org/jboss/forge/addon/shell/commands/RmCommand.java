/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.shell.commands;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.shell.Shell;
import org.jboss.forge.addon.shell.ui.AbstractShellCommand;
import org.jboss.forge.addon.shell.util.PathspecParser;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Implementation of the "rm" command
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class RmCommand extends AbstractShellCommand
{

   @Inject
   ResourceFactory resourceFactory;

   @Inject
   @WithAttributes(label = "Arguments", type = InputType.FILE_PICKER, required = true)
   private UIInputMany<String> arguments;

   @Inject
   @WithAttributes(label = "force", shortName = 'f', description = "ignore nonexistent files and arguments, never prompt", type = InputType.CHECKBOX, defaultValue = "false")
   private UIInput<Boolean> force;

   @Inject
   @WithAttributes(label = "force", shortName = 'r', description = "remove directories and their contents recursively", type = InputType.CHECKBOX, defaultValue = "false")
   private UIInput<Boolean> recursive;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("rm")
               .description("Remove (unlink) the FILE(s).");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(arguments).add(force).add(recursive);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Shell shell = (Shell) context.getUIContext().getProvider();
      FileResource<?> currentResource = shell.getCurrentResource();
      for (String file : arguments.getValue())
      {
         List<Resource<?>> resources = new PathspecParser(resourceFactory, currentResource, file).resolve();
         for (Resource<?> resource : resources)
         {
            if (!resource.exists())
            {
               return Results.fail(file + ": No such file or directory");
            }
         }
      }
      boolean forceOption = force.getValue();
      boolean recurse = recursive.getValue();
      for (String file : arguments.getValue())
      {
         List<Resource<?>> resources = new PathspecParser(resourceFactory, currentResource, file).resolve();
         for (Resource<?> resource : resources)
         {
            // TODO: Prompt for removal
            if (forceOption)
            {
               if (!resource.delete(recurse))
               {
                  return Results.fail("rm: cannot remove ‘" + resource.getFullyQualifiedName()
                           + "’: No such file or directory");
               }
            }
         }
      }
      while (!currentResource.exists())
      {
         currentResource = currentResource.getParent();
      }
      shell.setCurrentResource(currentResource);

      return Results.success();
   }
}
