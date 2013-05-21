/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.ui.context;

import org.jboss.forge.addon.ui.UICommand;
import org.jboss.forge.addon.ui.input.InputComponent;

/**
 * Allows {@link UICommand} objects to specify the order of the displayed fields
 *
 * Each wizard page receives an unique instance of {@link UIBuilder}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 *
 */
public interface UIBuilder extends UIContextProvider
{
   UIBuilder add(InputComponent<?, ?> input);
}