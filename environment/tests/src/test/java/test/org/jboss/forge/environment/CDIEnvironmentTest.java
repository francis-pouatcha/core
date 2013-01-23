/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package test.org.jboss.forge.environment;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.Addon;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.container.AddonDependency;
import org.jboss.forge.container.AddonId;
import org.jboss.forge.environment.Environment;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CDIEnvironmentTest
{
   @Deployment
   @Dependencies(@Addon(name = "org.jboss.forge:environment", version = "2.0.0-SNAPSHOT"))
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addPackages(true, CDIEnvironmentTest.class.getPackage())
               .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
               .addAsAddonDependencies(
                        AddonDependency.create(AddonId.from("org.jboss.forge:environment", "2.0.0-SNAPSHOT")));
      return archive;
   }

   @Inject
   private Environment environment;

   @Test
   public void testNotNull() throws Exception
   {
      Assert.assertNotNull(environment);
   }

   @Test
   public void testGetCategory() throws Exception
   {
      Map<Object, Object> mapTest = environment.get(TestCategory.class);
      Map<Object, Object> mapUI = environment.get(UserInterfaceCategory.class);

      Assert.assertNotNull(mapTest);
      Assert.assertNotNull(mapUI);

      Assert.assertNotSame(mapTest, mapUI);

      mapTest.put("Key", "Value");
      Assert.assertFalse(mapTest.isEmpty());

      Map<Object, Object> newMap = environment.get(TestCategory.class);

      Assert.assertSame(mapTest, newMap);
      Assert.assertEquals("Value", newMap.get("Key"));
   }
}