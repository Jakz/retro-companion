package com.github.jakz.retrocompanion.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.archive.ArchiveFormat;
import com.pixbits.lib.io.archive.Compressible;
import com.pixbits.lib.io.archive.Compressor;
import com.pixbits.lib.io.archive.CompressorOptions;

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
    String newName = FileUtils.fileNameWithoutExtension(entry.path());
    
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
  
  public static EntryTask CompressEntry(ArchiveFormat format)
  {
    return (mediator, entry) -> {
      try
      {
        if (!format.canWrite)
          throw new TaskException("Format %s can't be compressed", format.extension());
        
        if (!Files.exists(entry.absolutePath(mediator)))
          throw new TaskException("Entry file doesn't exist");

        boolean isAlreadyArchive = ArchiveFormat.guessFormat(entry.path()) != null; 
        
        if (!isAlreadyArchive)
        {
          Path destPath = entry.absolutePath(mediator).getParent().resolve(FileUtils.fileNameWithoutExtension(entry.path()) + format.dottedExtension());
          
          Compressor<Compressible> compressor = new Compressor<>(new CompressorOptions(format, false, 9));
          compressor.createArchive(destPath, Collections.singletonList(Compressible.ofPath(entry.absolutePath(mediator))));
          
          //Files.delete(entry.path);
          entry.setPath(entry.path().getParent().resolve(destPath.getFileName()));       
        }
        
        return true;
      }
      catch (IOException ex)
      {
        throw new TaskException("Error while creating archive for "+entry.name(), ex);
      }
    };
    
  }
  
  public static final EntryTask MakeEntryPathAbsolute = (mediator, entry) ->
  {
    //TODO: finish
    return true;
  };
}
