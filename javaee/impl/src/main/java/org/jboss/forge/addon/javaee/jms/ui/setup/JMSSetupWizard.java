/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.javaee.jms.ui.setup;

import javax.inject.Inject;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.javaee.facets.JMSFacet;
import org.jboss.forge.addon.javaee.ui.AbstractJavaEECommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Setups JMS in a {@link Project}
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@FacetConstraint(DependencyFacet.class)
public class JMSSetupWizard extends AbstractJavaEECommand
{

   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("JMS: Setup")
               .description("Setup JMS in your project")
               .category(Categories.create(super.getMetadata(context).getCategory(), "JMS"));
   }

   @Inject
   private FacetFactory facetFactory;

   @Inject
   @WithAttributes(required = true, label = "JMS Version")
   private UISelectOne<JMSFacet> jmsVersion;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      jmsVersion.setItemLabelConverter(new Converter<JMSFacet, String>()
      {
         @Override
         public String convert(JMSFacet source)
         {
            return source.getSpecVersion().toString();
         }
      });

      for (JMSFacet choice : jmsVersion.getValueChoices())
      {
         if (jmsVersion.getValue() == null
                  || choice.getSpecVersion().compareTo(jmsVersion.getValue().getSpecVersion()) >= 1)
         {
            jmsVersion.setDefaultValue(choice);
         }
      }

      builder.add(jmsVersion);
   }

   @Override
   public Result execute(final UIExecutionContext context) throws Exception
   {
      if (facetFactory.install(getSelectedProject(context), jmsVersion.getValue()))
      {
         return Results.success("JMS has been installed.");
      }
      return Results.fail("Could not install JMS.");
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }
}