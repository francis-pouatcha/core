/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.project.facets;

import java.util.Collection;

import org.jboss.forge.parser.java.Annotation;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Facet;

/**
 * Retrieves information from the index of the current project
 *
 * The index is built based on the JARs provided by the project's pom.xml dependencies
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 *
 */
public interface JavaIndexFacet extends Facet
{

   /**
    * Gets all known classes by this index (those which were scanned).
    *
    * @return a collection of known classes
    */
   public Collection<JavaClass> getKnownClasses();

   /**
    * Gets the class (or interface, or annotation) that was scanned during the indexing phase.
    *
    * @param className the name of the class
    * @return information about the class or null if it is not known
    */
   public JavaClass getClassByName(final String className);

   /**
    * Gets all known direct subclasses of the specified class name. A known direct subclass is one which was found
    * during the scanning process; however, this is often not the complete universe of subclasses, since typically
    * indexes are constructed per jar. It is expected that several indexes will need to be searched when analyzing a jar
    * that is a part of a complex multi-module/classloader environment (like an EE application server).
    * <p/>
    * Note that this will only pick up direct subclasses of the class. It will not pick up subclasses of subclasses.
    *
    * @param className the super class of the desired subclasses
    * @return a non-null list of all known subclasses of className
    */
   public Collection<JavaClass> getKnownDirectSubclasses(final String className);

   /**
    * Returns all known (including non-direct) sub classes of the given class. I.e., returns all known classes that are
    * assignable to the given class.
    *
    * @param className The class
    *
    * @return All known subclasses
    */
   public Collection<JavaClass> getAllKnownSubclasses(final String className);

   /**
    * Gets all known direct implementors of the specified interface name. A known direct implementor is one which was
    * found during the scanning process; however, this is often not the complete universe of implementors, since
    * typically indexes are constructed per jar. It is expected that several indexes will need to be searched when
    * analyzing a jar that is a part of a complex multi-module/classloader environment (like an EE application server).
    * <p/>
    * The list of implementors may also include other interfaces, in order to get a complete list of all classes that
    * are assignable to a given interface it is necessary to recursively call
    * {@link #getKnownDirectImplementors(DotName)} for every implementing interface found.
    *
    * @param className the super class of the desired subclasses
    * @return a non-null list of all known subclasses of className
    */
   public Collection<JavaClass> getKnownDirectImplementors(final String className);

   /**
    * Returns all known classes that implement the given interface, directly and indirectly. This will all return
    * classes that implement sub interfaces of the interface, and sub-classes of classes that implement the interface.
    * (In short, it will return every class that is assignable to the interface that is found in the index)
    * <p/>
    * This will only return classes, not interfaces.
    *
    * @param interfaceName The interface
    * @return All known implementors of the interface
    */
   public Collection<JavaClass> getAllKnownImplementors(final String interfaceName);

   /**
    * Obtains a list of instances for the specified annotation. This is done using an O(1) lookup. Valid instance
    * targets include field, method, parameter, and class.
    *
    * @param annotationName the name of the annotation to look for
    * @return a non-null list of annotation instances
    */
   public Collection<Annotation<JavaClass>> getAnnotations(final String annotationName);
}
