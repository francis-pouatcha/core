/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.maven.facets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.Annotation;
import org.jboss.forge.parser.java.JavaAnnotation;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.util.Assert;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaIndexFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.resources.DependencyResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

/**
 * Indexes the classes in the dependencies so that it is possible to fetch information like:
 *
 * - classes with a specific annotation: Eg. @Entity
 *
 * - subclasses of a specific class
 *
 * - classes that implement a specific interface
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 *
 */
@Dependent
@Alias(MavenJavaIndexFacet.FACET_ALIAS)
@RequiresFacet(DependencyFacet.class)
public class MavenJavaIndexFacet extends BaseFacet implements JavaIndexFacet
{
   static final String FACET_ALIAS = "forge.maven.ClassIndexFacet";

   private DependencyResolver dependencyResolver;

   @Inject
   public MavenJavaIndexFacet(DependencyResolver dependencyResolver)
   {
      this.dependencyResolver = dependencyResolver;
   }

   /**
    * Jandex index produced in another thread
    */
   private Future<IndexView> indexView;

   @Override
   public boolean install()
   {
      generateIndex();
      return true;
   }

   @Override
   public boolean isInstalled()
   {
      return indexView != null;
   }

   @Override
   public boolean uninstall()
   {
      indexView = null;
      return super.uninstall();
   }

   /**
    * Generates the index in another thread
    */
   private void generateIndex()
   {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      // Index generation is slow. Executing in a separate thread
      indexView = executor.submit(new Callable<IndexView>()
      {
         @Override
         public IndexView call() throws Exception
         {
            return generateProjectDependenciesIndex();
         }
      });
      executor.shutdown();
   }

   /**
    * Calculates an {@link IndexView} from all the project dependencies
    *
    * @return a {@link CompositeIndex} from all the dependencies associated with the current project
    */
   private IndexView generateProjectDependenciesIndex()
   {
      List<Dependency> dependencies = getProject().getFacet(DependencyFacet.class).getDependencies();
      List<IndexView> indexes = new ArrayList<IndexView>();
      for (Dependency dependency : dependencies)
      {
         if (PackagingType.JAR == dependency.getPackagingTypeEnum())
         {
            try
            {
               for (DependencyResource resource : dependencyResolver.resolveArtifacts(dependency))
               {
                  indexes.add(createIndexFromJar(resource.getUnderlyingResourceObject()));
               }
            }
            catch (IOException io)
            {
               // FIXME: Log the exception
               io.printStackTrace();
            }
         }
      }
      return CompositeIndex.create(indexes);
   }

   @Override
   public Collection<JavaClass> getKnownClasses()
   {
      assertIndexIsInitialized();
      Collection<JavaClass> result = new ArrayList<JavaClass>();
      Collection<ClassInfo> knownClasses = getIndexView().getKnownClasses();
      for (ClassInfo classInfo : knownClasses)
      {
         result.add(toJavaClass(classInfo));
      }
      return result;
   }

   @Override
   public JavaClass getClassByName(final String className)
   {
      assertIndexIsInitialized();
      Assert.notNull(className, "Class name cannot be null");
      return null;
   }

   @Override
   public Collection<JavaClass> getKnownDirectSubclasses(final String className)
   {
      assertIndexIsInitialized();
      Assert.notNull(className, "Class name cannot be null");
      Collection<JavaClass> result = new ArrayList<JavaClass>();
      Collection<ClassInfo> knownClasses = getIndexView().getKnownDirectSubclasses(DotName.createSimple(className));
      for (ClassInfo classInfo : knownClasses)
      {
         result.add(toJavaClass(classInfo));
      }
      return result;
   }

   @Override
   public Collection<JavaClass> getAllKnownSubclasses(final String className)
   {
      assertIndexIsInitialized();
      Assert.notNull(className, "Class name cannot be null");
      Collection<JavaClass> result = new ArrayList<JavaClass>();
      Collection<ClassInfo> knownClasses = getIndexView().getAllKnownSubclasses(DotName.createSimple(className));
      for (ClassInfo classInfo : knownClasses)
      {
         result.add(toJavaClass(classInfo));
      }
      return result;
   }

   @Override
   public Collection<JavaClass> getKnownDirectImplementors(final String className)
   {
      assertIndexIsInitialized();
      Assert.notNull(className, "Class name cannot be null");
      Collection<JavaClass> result = new ArrayList<JavaClass>();
      Collection<ClassInfo> knownClasses = getIndexView().getKnownDirectImplementors(DotName.createSimple(className));
      for (ClassInfo classInfo : knownClasses)
      {
         result.add(toJavaClass(classInfo));
      }
      return result;
   }

   @Override
   public Collection<JavaClass> getAllKnownImplementors(final String interfaceName)
   {
      assertIndexIsInitialized();
      Assert.notNull(interfaceName, "Interface name cannot be null");
      Collection<JavaClass> result = new ArrayList<JavaClass>();
      Collection<ClassInfo> knownClasses = getIndexView().getAllKnownImplementors(DotName.createSimple(interfaceName));
      for (ClassInfo classInfo : knownClasses)
      {
         result.add(toJavaClass(classInfo));
      }
      return result;
   }

   @Override
   public Collection<Annotation<JavaClass>> getAnnotations(String annotationName)
   {
      assertIndexIsInitialized();
      Assert.notNull(annotationName, "Annotation name cannot be null");
      Collection<Annotation<JavaClass>> result = new ArrayList<Annotation<JavaClass>>();
      Collection<AnnotationInstance> annotations = getIndexView().getAnnotations(DotName.createSimple(annotationName));
      for (AnnotationInstance annotationInstance : annotations)
      {
         result.add(toAnnotation(annotationInstance));
      }
      return result;
   }

   private void assertIndexIsInitialized()
   {
      Assert.notNull(indexView, "Index is not initialized yet. Facet installed?");
   }

   /**
    * Creates an {@link IndexView} based on a jar file
    *
    * @param jarFile the jar file.
    * @return {@link IndexView} instance
    * @throws IOException
    */
   private IndexView createIndexFromJar(File jarFile)
            throws IOException
   {
      Indexer indexer = new Indexer();
      JarFile jar = new JarFile(jarFile);
      try
      {
         Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements())
         {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class"))
            {
               indexer.index(jar.getInputStream(entry));
            }
         }
      }
      finally
      {
         try
         {
            jar.close();
         }
         catch (IOException ignore)
         {
         }
      }
      return indexer.complete();
   }

   private IndexView getIndexView()
   {
      try
      {
         return indexView.get();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Index is not generated yet", e);
      }
   }

   // Converter Methods
   Annotation<JavaClass> toAnnotation(AnnotationInstance annotationInstance)
   {
      JavaAnnotation annotation = JavaParser.create(JavaAnnotation.class);

      annotation.setName(annotationInstance.name().toString());
      return null;
   }

   JavaClass toJavaClass(ClassInfo classInfo)
   {
      JavaClass javaClass = JavaParser.create(JavaClass.class);
      javaClass.setName(classInfo.name().toString());
      // Add Interfaces
      for (DotName iface : classInfo.interfaces())
      {
         javaClass.addInterface(iface.toString());
      }
      // Add Super class
      javaClass.setSuperType(classInfo.superName().toString());

      return javaClass;
   }
}
