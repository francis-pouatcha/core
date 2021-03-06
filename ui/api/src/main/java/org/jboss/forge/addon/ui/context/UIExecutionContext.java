/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.ui.context;

import org.jboss.forge.addon.ui.UIProgressMonitor;

/**
 * A {@link UIExecutionContext} is created when the execution phase is requested
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public interface UIExecutionContext extends UIContextProvider
{
   /**
    * Returns the {@link UIProgressMonitor} for this execution
    */
   UIProgressMonitor getProgressMonitor();

}
