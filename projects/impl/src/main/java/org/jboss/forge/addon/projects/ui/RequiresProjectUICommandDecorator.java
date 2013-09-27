/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.projects.ui;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.annotations.RequiresProject;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;

/**
 * This decorator is invoked for {@link UICommand} instances that requires a project
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@Priority(1000)
@Decorator
public abstract class RequiresProjectUICommandDecorator implements UICommand
{
   @Inject
   @Delegate
   @Any
   UICommand command;

   @Inject
   ProjectFactory projectFactory;

   @Override
   public boolean isEnabled(UIContext context)
   {
      boolean result;
      if (command.getClass().getAnnotation(RequiresProject.class) != null)
      {
         result = containsProject(context);
      }
      else
      {
         result = command.isEnabled(context);
      }
      return result;
   }

   /**
    * Returns <code>true</code> if a {@link Project} exists in the current {@link UISelection}.
    */
   private boolean containsProject(UIContext context)
   {
      UISelection<FileResource<?>> initialSelection = context.getInitialSelection();
      if (!initialSelection.isEmpty())
      {
         return projectFactory.containsProject(initialSelection.get());
      }
      return false;
   }

}
