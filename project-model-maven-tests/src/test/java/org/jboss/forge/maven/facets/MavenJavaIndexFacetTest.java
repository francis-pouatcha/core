/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.maven.facets;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaIndexFacet;
import org.jboss.forge.test.AbstractShellTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MavenJavaIndexFacetTest extends AbstractShellTest
{
   @Test
   public void testFacetInstalled() throws Exception
   {
      Project project = initializeJavaProject();
      installIndexFacet();
      Assert.assertTrue(project.hasFacet(JavaIndexFacet.class));
   }

   @Test
   public void testDependencyInfo() throws Exception
   {
      Project project = initializeJavaProject();
      project.getFacet(DependencyFacet.class).addDirectDependency(
               DependencyBuilder.create().setGroupId("org.hibernate").setArtifactId("hibernate-core")
                        .setVersion("4.2.0.Final"));
      installIndexFacet();
      JavaIndexFacet facet = getProject().getFacet(JavaIndexFacet.class);
      Collection<JavaClass> knownDirectImplementors = facet
               .getKnownDirectImplementors(EntityManager.class.getName());
      Assert.assertThat(knownDirectImplementors.size(), equalTo(1));
   }

   private void installIndexFacet() throws Exception
   {
      getShell().execute("project install-facet " + MavenJavaIndexFacet.FACET_ALIAS);
   }

}
