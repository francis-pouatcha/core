package org.jboss.forge.addon.shell.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.TerminalSize;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.shell.Shell;
import org.jboss.forge.addon.shell.ui.AbstractShellCommand;
import org.jboss.forge.addon.shell.util.PathspecParser;
import org.jboss.forge.addon.shell.util.ShellUtil;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class LsCommand extends AbstractShellCommand
{

   @Inject
   ResourceFactory resourceFactory;

   @Inject
   @WithAttributes(label = "Arguments", type = InputType.DIRECTORY_PICKER)
   private UIInputMany<String> arguments;

   @Inject
   @WithAttributes(label = "all", shortName = 'a', description = "do not ignore entries starting with .", type = InputType.CHECKBOX, defaultValue = "false")
   private UIInput<Boolean> all;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("ls").description("List files");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(arguments).add(all);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Shell shell = (Shell) context.getUIContext().getProvider();
      FileResource<?> currentResource = shell.getCurrentResource();
      Iterator<String> it = arguments.getValue() == null ? Collections.<String> emptyList().iterator() : arguments
               .getValue()
               .iterator();
      final Result result;
      final FileResource<?> newResource = (it.hasNext()) ? new PathspecParser(
               resourceFactory, currentResource, it.next()).resolve().get(0).reify(FileResource.class)
               : currentResource;
      if (!newResource.exists())
      {
         result = Results.fail(newResource.getName() + ": No such file or directory");
      }
      else
      {
         UIOutput output = shell.getOutput();
         output.out().println(listMany(newResource.listResources(), shell));
         result = Results.success();
      }
      return result;
   }

   private String listMany(Iterable<Resource<?>> files, Shell shell)
   {
      TerminalSize terminalSize = shell.getConsole().getShell().getSize();
      List<String> display = new ArrayList<String>();
      boolean showAll = all.getValue();
      if (files != null)
      {
         for (Resource<?> file : files)
         {
            String name;
            if (file instanceof FileResource)
            {
               FileResource<?> fileResource = (FileResource<?>) file;
               if (!showAll && fileResource.getName().startsWith("."))
               {
                  continue;
               }
               name = ShellUtil.colorizeResource(fileResource);
            }
            else
            {
               name = file.getName();
            }
            display.add(name);
         }
      }
      return Parser.formatDisplayList(display, terminalSize.getHeight(), terminalSize.getWidth());
   }
}
