/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.shell.ui;

import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.shell.Shell;
import org.jboss.forge.addon.ui.context.AbstractUIContext;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextListener;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.util.Selections;

/**
 * Implementation of {@link UIContext}
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class ShellContextImpl extends AbstractUIContext implements ShellContext
{
   private Shell shell;
   private UISelection<?> initialSelection;
   private final Iterable<UIContextListener> listeners;

   @SuppressWarnings("unchecked")
   public ShellContextImpl(Shell shell, FileResource<?> initialSelection, Iterable<UIContextListener> listeners)
   {
      this.shell = shell;
      this.initialSelection = Selections.from(initialSelection);
      this.listeners = listeners;
      init();
   }

   @SuppressWarnings("unchecked")
   @Override
   public UISelection<?> getInitialSelection()
   {
      return initialSelection;
   }

   @Override
   public Shell getProvider()
   {
      return shell;
   }

   public void init()
   {
      for (UIContextListener listener : listeners)
      {
         listener.contextInitialized(this);
      }
   }

   public void destroy()
   {
      for (UIContextListener listener : listeners)
      {
         listener.contextDestroyed(this);
      }
   }
}
