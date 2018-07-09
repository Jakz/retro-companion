package com.github.jakz.retrocompanion.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.TransferHandler;

import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.ui.FileTransferHandler;

class ThumbnailBoxDropListener implements FileTransferHandler.Listener
{    
  Mediator mediator;
  Predicate<Path[]> condition;
  Supplier<Path> destination;
  Runnable onSuccess;
  
  ThumbnailBoxDropListener(Mediator mediator, Predicate<Path[]> condition, Supplier<Path> destination, Runnable onSuccess)
  {
    this.mediator = mediator;
    this.condition = condition;
    this.destination = destination;
    this.onSuccess = onSuccess;
  }
  
  @Override
  public void filesDropped(TransferHandler.TransferSupport info, Path[] files)
  {
    try
    {
      if (condition.test(files))
      {
        Path source = files[0];
      
        if (source.getFileName().toString().toLowerCase().endsWith(".png"))
        {
          Path dest = destination.get();
          
          if (Files.exists(dest))
          {
            if (!mediator.options().overwriteThumbnailWithoutConfirmation)
            {
              boolean confirm = Tasks.askForConfirmation(mediator, "Do you want to overwrite existing thumbnail?");
              
              if (!confirm)
                return;
            }
            
            Files.delete(dest);
          }
          
          Files.createDirectories(dest.getParent());
          
          if (mediator.options().thumbnailMoveInsteadThanCopy)
            Files.move(source, dest);
          else
            Files.copy(source, dest);
          
          onSuccess.run();
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    } 
  }
}