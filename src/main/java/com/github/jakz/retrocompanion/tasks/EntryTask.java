package com.github.jakz.retrocompanion.tasks;

import java.util.Optional;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.io.FileUtils;

@FunctionalInterface
public interface EntryTask
{
  public boolean process(Mediator mediator, Entry entry) throws TaskException;
  
  public static final EntryTask RemoveTagsFromName = (mediator, entry) ->
  {
    String name = entry.name();
    int firstTagIndex = name.indexOf('(');
    
    if (firstTagIndex != -1 && firstTagIndex > 0)
    {
      String newName = name.substring(0, firstTagIndex-1);
      
      entry.rename(newName, mediator.options());
      return true;
    }
    
    return false;
  };
  
  public static final EntryTask RenameEntryToMatchFileName = (mediator, entry) ->
  {
    String name = entry.name();
    String newName = FileUtils.fileNameWithoutExtension(entry.path);
    
    if (!name.equals(newName))     
    {
      entry.rename(newName, mediator.options());
      return true;
    }
    
    return false;
  };
  
  public static EntryTask AssignCore(Optional<Core.Ref> core) 
  {
    return (mediator, entry) -> {
      entry.setCore(core.map(Core.Ref::dupe));
      return true;
    };
  }
  
  public static final EntryTask MakeEntryPathAbsolute = (mediator, entry) ->
  {
    //TODO: finish
    return true;
  };
}
