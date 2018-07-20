package com.github.jakz.retrocompanion.tasks;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.parsers.PlaylistParser;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.ui.UIUtils;
import com.pixbits.lib.ui.UIUtils.OperatingSystem;
import com.pixbits.lib.ui.elements.ProgressDialog;

public class Tasks
{
  public static BatchTask LaunchRetroarch = mediator -> {
    try
    {
      OperatingSystem os = UIUtils.getOperatingSystem();
      String executableName = os.isWindows() ? "retroarch.exe" : "retroarch";
      Path executablePath = mediator.options().retroarchPath.resolve(executableName);   
      Runtime.getRuntime().exec(executablePath.toString());//, null, executablePath.getParent().toFile());
      return true;
    }
    catch (Exception e)
    {
      throw new TaskException("Exception while launching RetroArch", e);
    }
  };
  
  public static BatchTask DeleteFileFromDisk(Supplier<Path> supplier)
  {
    return mediator -> {
      Path path = supplier.get();

      try
      { 
        //TODO: confirmation?
        if (path != null && Files.exists(path))
        {
          Files.delete(path);
          return true;
        }
        
        return false;
      }
      catch (IOException ex)
      {
        throw new TaskException("Exception while deleting " + path.toString() + " from disk", ex);
      }
    };
  }
  
  public static BatchTask OpenFileInExplorer(Supplier<Path> supplier)
  {
    return mediator -> {
      Path path = supplier.get();
      try
      {
        //TODO: highlight the file
        if (path != null && Files.exists(path))
        {
          Desktop.getDesktop().open(path.getParent().toFile());
          return true;
        }
        
        return false;
      }
      catch (IOException ex)
      {
        throw new TaskException("Exception while opening folder for " + path.toString(), ex);
      }
    };
  }
  
  public static class Standalone
  {
    public static void sortPlaylistAlphabetically(Playlist playlist)
    {
      List<Entry> entries = playlist.stream()
          .sorted((e1, e2) -> e1.name().compareToIgnoreCase(e2.name()))
          .collect(Collectors.toList());
      
      playlist.clear();
      entries.forEach(playlist::add);
      
      //TODO: we assume order is changed, not correct but efficient
      playlist.markDirty();
    }
    
    public static void makePathsRelative(Playlist playlist, Path path)
    {
      playlist.stream().forEach(entry -> entry.relativizePath(path));
    }
    
    public static void makePathsAbsolute(Playlist playlist, Path path)
    {
      playlist.stream().forEach(entry -> entry.makeAbsolutePath(path));
    }
    
    public static List<Playlist> loadPlaylistsFromFolder(Path folder, Options options)
    {
      try
      {
        FolderScanner scanner = new FolderScanner(FileSystems.getDefault().getPathMatcher("glob:*.lpl"), false);
        
        Set<Path> playlists = scanner.scan(folder);
        PlaylistParser parser = new PlaylistParser(options);
        
        return playlists.stream().map(parser::parse).collect(Collectors.toList());
      }
      catch (IOException e)
      {
        e.printStackTrace();
        return Collections.emptyList();
      }
    }
  }
  
  public static void removeSelectedEntriesFromPlaylist(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    List<Entry> entries = mediator.getSelectedEntries();
    
    if (playlist != null && !entries.isEmpty())
    {
      //TODO: localize
      boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
          mediator.modalTarget(),
          "Warning",
          "This can't be undone, are you sure you want to proceed?"
      );
      
      if (confirmed)
        mediator.removeEntriesFromPlaylist(entries);
    }
    
  }

  public static void relativizePathsToRetroarch(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    Path retroarchPath = mediator.options().retroarchPath;
    
    if (Files.exists(retroarchPath))
    {
      if (playlist != null)
        Standalone.makePathsRelative(playlist, retroarchPath);
      
      mediator.refreshPlaylist();
    }
    else
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "Retroarch path doesn't exist");
  }
  
  public static void makePathsAbsolute(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    Path retroarchPath = mediator.options().retroarchPath;
    
    if (Files.exists(retroarchPath))
    {
      if (playlist != null)
        Standalone.makePathsAbsolute(playlist, retroarchPath);
      
      mediator.refreshPlaylist();
    }
    else
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "Retroarch path doesn't exist");
  }
  
  public static boolean askForConfirmation(Mediator mediator, String message)
  {
    boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
        mediator.modalTarget(),
        "Warning",
        message
    );
    
    return confirmed;
  }
  
  public static boolean executeEntryTask(Mediator mediator, EntryTask task, Entry entry) throws TaskException
  {
    if (task.process(mediator, entry))
    {
      if (mediator.entry() == entry)
        mediator.selectEntry(entry);
      
      entry.playlist().markDirty();
      
      return true;
    }
    
    return false;
  }
  
  public static boolean executePlaylistTask(Mediator mediator, PlaylistTask task, Playlist playlist) throws TaskException
  {
    if (playlist != null && task.process(mediator, playlist))
    {
      if (mediator.playlist() == playlist)
      {
        Entry selectedEntry = mediator.entry();
        mediator.refreshPlaylist();
        mediator.selectEntry(selectedEntry);
      }
      
      return true;
    }
    
    return false;
  }
    
  public static void executeEntryTaskOnPlaylistUI(Mediator mediator, EntryTask task)
  {
    executeTaskUI(mediator, PlaylistTask.of(task));
  }
  
  public static void executeTaskOnEntryUI(Mediator mediator, EntryTask task, Entry entry)
  {
    try
    {
      task.process(mediator, entry);
    }
    catch (TaskException e)
    {
      mediator.handleException(e);
    }
  }
  
  public static boolean executeTaskUI(Mediator mediator, PlaylistTask task)
  {
    try
    {
      return Tasks.executePlaylistTask(mediator, task, mediator.playlist());
    }
    catch (TaskException e)
    {
      mediator.handleException(e);
      return false;
    }
  }
  
  public static void executeTaskUI(Mediator mediator, BatchTask task)
  {
    try
    {
      task.process(mediator);
    }
    catch (TaskException e)
    {
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", e.dialogMessage);
    }
  }
  
  public static void executeLongEntryTaskOnPlaylist(Mediator mediator, EntryTask task, String title, String progressFormat)
  {
    ProgressDialog.Manager progress = mediator.progress();
    
    AtomicBoolean canceled = new AtomicBoolean();
    Runnable onCancel = () -> canceled.set(true);
    
    progress.show(title, onCancel);
    
    Runnable threadTask = () -> {
      Playlist playlist = mediator.playlist();
      
      try
      {   
        final float total = playlist.size();
        
        for (int i = 0; i < total; ++i)
        {
          Entry entry = playlist.get(i);
          
          if (canceled.get())
            return;
          
          final float percent = i / total;
          SwingUtilities.invokeLater(() -> progress.update(percent, String.format(progressFormat, entry.name())));
          task.process(mediator, entry);
        }  
        
        SwingUtilities.invokeLater(() -> progress.finished());
      }
      catch (TaskException e)
      {
        SwingUtilities.invokeLater(() ->
          mediator.handleException(e));
      }
    };
    
    new Thread(threadTask).start();
  }
}
