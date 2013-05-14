/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.maven.projects.facets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.jboss.forge.container.exception.ContainerException;
import org.jboss.forge.container.util.Strings;
import org.jboss.forge.facets.AbstractFacet;
import org.jboss.forge.maven.projects.MavenFacet;
import org.jboss.forge.maven.projects.util.Packages;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.parser.java.resources.JavaResource;
import org.jboss.forge.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.projects.Project;
import org.jboss.forge.resource.DirectoryResource;
import org.jboss.forge.resource.Resource;
import org.jboss.forge.resource.ResourceFilter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Dependent
public class MavenJavaSourceFacet extends AbstractFacet<Project> implements JavaSourceFacet
{
   @Override
   public List<DirectoryResource> getSourceFolders()
   {
      List<DirectoryResource> result = new ArrayList<DirectoryResource>();
      result.add(getSourceFolder());
      result.add(getTestSourceFolder());
      return result;
   }

   @Override
   public String calculateName(final JavaResource resource)
   {
      String fullPath = Packages.fromFileSyntax(resource.getFullyQualifiedName());
      String pkg = calculatePackage(resource);
      String name = fullPath.substring(fullPath.lastIndexOf(pkg) + pkg.length() + 1);
      name = name.substring(0, name.lastIndexOf(".java"));
      return name;
   }

   @Override
   public String calculatePackage(final JavaResource resource)
   {
      List<DirectoryResource> folders = getSourceFolders();
      String pkg = null;
      for (DirectoryResource folder : folders)
      {
         String sourcePrefix = folder.getFullyQualifiedName();
         pkg = resource.getParent().getFullyQualifiedName();
         if (pkg.startsWith(sourcePrefix))
         {
            pkg = pkg.substring(sourcePrefix.length() + 1);
            break;
         }
      }
      pkg = Packages.fromFileSyntax(pkg);

      return pkg;
   }

   @Override
   public String getBasePackage()
   {
      return Packages.toValidPackageName(getOrigin().getFacet(MavenFacet.class).getPOM().getGroupId());
   }

   @Override
   public DirectoryResource getBasePackageResource()
   {
      return getSourceFolder().getChildDirectory(Packages.toFileSyntax(getBasePackage()));
   }

   @Override
   public DirectoryResource getSourceFolder()
   {
      MavenFacet mavenFacet = getOrigin().getFacet(MavenFacet.class);
      Build build = mavenFacet.getPOM().getBuild();
      String srcFolderName;
      if (build != null && build.getSourceDirectory() != null)
      {
         srcFolderName = build.getSourceDirectory();
      }
      else
      {
         srcFolderName = "src" + File.separator + "main" + File.separator + "java";
      }
      DirectoryResource projectRoot = getOrigin().getProjectRoot();
      return projectRoot.getChildDirectory(srcFolderName);
   }

   @Override
   public DirectoryResource getTestSourceFolder()
   {
      MavenFacet mavenFacet = getOrigin().getFacet(MavenFacet.class);
      Build build = mavenFacet.getPOM().getBuild();
      String srcFolderName;
      if (build != null && build.getTestSourceDirectory() != null)
      {
         srcFolderName = build.getTestSourceDirectory();
      }
      else
      {
         srcFolderName = "src" + File.separator + "test" + File.separator + "java";
      }
      DirectoryResource projectRoot = getOrigin().getProjectRoot();
      return projectRoot.getChildDirectory(srcFolderName);
   }

   @Override
   public boolean isInstalled()
   {
      return getSourceFolder().exists();
   }

   @Override
   public boolean install()
   {
      if (!this.isInstalled())
      {
         for (DirectoryResource folder : this.getSourceFolders())
         {
            folder.mkdirs();
         }

         // FIXME WOW this needs to be simplified somehow...
         MavenFacet maven = getOrigin().getFacet(MavenFacet.class);
         Model pom = maven.getPOM();
         Build build = pom.getBuild();
         if (build == null)
         {
            build = new Build();
         }
         List<Plugin> plugins = build.getPlugins();
         Plugin javaSourcePlugin = null;
         for (Plugin plugin : plugins)
         {
            if ("org.apache.maven.plugins".equals(plugin.getGroupId())
                     && "maven-compiler-plugin".equals(plugin.getArtifactId()))
            {
               javaSourcePlugin = plugin;
            }
         }

         if (javaSourcePlugin == null)
         {
            javaSourcePlugin = new Plugin();
            // FIXME this should find the most recent version using DependencyResolver
            javaSourcePlugin.setGroupId("org.apache.maven.plugins");
            javaSourcePlugin.setArtifactId("maven-compiler-plugin");
            javaSourcePlugin.setVersion("2.3.2");

            try
            {
               Xpp3Dom dom = Xpp3DomBuilder.build(
                        new ByteArrayInputStream(
                                 ("<configuration>" +
                                          "<source>1.6</source>" +
                                          "<target>1.6</target>" +
                                          "<encoding>UTF-8</encoding>" +
                                          "</configuration>").getBytes()),
                        "UTF-8");

               javaSourcePlugin.setConfiguration(dom);
            }
            catch (Exception e)
            {
               throw new ContainerException(e);
            }
         }

         build.addPlugin(javaSourcePlugin);
         pom.setBuild(build);
         maven.setPOM(pom);

      }
      return true;
   }

   @Override
   public JavaResource getJavaResource(final JavaSource<?> javaClass) throws FileNotFoundException
   {
      String pkg = Strings.isNullOrEmpty(javaClass.getPackage()) ? "" : javaClass.getPackage() + ".";
      return getJavaResource(pkg + javaClass.getName());
   }

   @Override
   public JavaResource getTestJavaResource(final JavaSource<?> javaClass) throws FileNotFoundException
   {
      String pkg = Strings.isNullOrEmpty(javaClass.getPackage()) ? "" : javaClass.getPackage() + ".";
      return getTestJavaResource(pkg + javaClass.getName());
   }

   @Override
   public JavaResource getJavaResource(final String relativePath) throws FileNotFoundException
   {
      return getJavaResource(getSourceFolder(), relativePath);
   }

   @Override
   public JavaResource getTestJavaResource(final String relativePath) throws FileNotFoundException
   {
      return getJavaResource(getTestSourceFolder(), relativePath);
   }

   private JavaResource getJavaResource(final DirectoryResource sourceDir, final String relativePath)
   {
      String path = relativePath.trim().endsWith(".java")
               ? relativePath.substring(0, relativePath.lastIndexOf(".java")) : relativePath;

      path = Packages.toFileSyntax(path) + ".java";
      JavaResource target = sourceDir.getChildOfType(JavaResource.class, path);
      return target;
   }

   @Override
   public JavaResource saveJavaSource(final JavaSource<?> source) throws FileNotFoundException
   {
      return getJavaResource(source.getQualifiedName()).setContents(source);
   }

   @Override
   public JavaResource saveTestJavaSource(final JavaSource<?> source) throws FileNotFoundException
   {
      return getTestJavaResource(source.getQualifiedName()).setContents(source);
   }

   @Override
   public void visitJavaSources(final JavaResourceVisitor visitor)
   {
      visitSources(getSourceFolder(), visitor);
   }

   @Override
   public void visitJavaTestSources(final JavaResourceVisitor visitor)
   {
      visitSources(getTestSourceFolder(), visitor);
   }

   private void visitSources(final Resource<?> searchFolder, final JavaResourceVisitor visitor)
   {
      if (searchFolder instanceof DirectoryResource)
      {

         searchFolder.listResources(new ResourceFilter()
         {
            @Override
            public boolean accept(Resource<?> resource)
            {
               if (resource instanceof DirectoryResource)
               {
                  visitSources(resource, visitor);
               }
               if (resource instanceof JavaResource)
               {
                  visitor.visit((JavaResource) resource);
               }

               return false;
            }
         });
      }
   }
}