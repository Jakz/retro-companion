package com.github.jakz.retrocompanion.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.tasks.EntryTask;
import com.github.jakz.retrocompanion.tasks.PlaylistTask;
import com.github.jakz.retrocompanion.tasks.TaskException;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.ui.UIUtils;

public class Toolbar extends JToolBar 
{
  private final Mediator mediator;
  
  public Toolbar(Mediator mediator)
  {
    this.mediator = mediator;
    
    JButton newPlaylist = new JButton(Icon.NEW_PLAYLIST.icon(24));
    newPlaylist.setToolTipText("New playlist"); //TODO: localize
    newPlaylist.addActionListener(e -> {
      if (!Files.exists(mediator.options().playlistsPath))
        UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "Playlist path doesn't exists!"); //TODO: localize
      else
      {
        JFileChooser chooser = new JFileChooser();
  
        chooser.setCurrentDirectory(mediator.options().playlistsPath.toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
          @Override
          public boolean accept(File f) { return f.getName().endsWith(".lpl"); }

          @Override
          public String getDescription() { return "Playlist Files (*.lpl)"; }
          
        });
        
        int result = chooser.showSaveDialog(mediator.modalTarget());
        
        if (result == JFileChooser.APPROVE_OPTION)
        {
          String file = chooser.getSelectedFile().getAbsoluteFile().toString();

          if (!file.endsWith(".lpl"))
            file = FileUtils.trimExtension(file) + ".lpl";
          
          Path path = Paths.get(file);
          
          if (Files.exists(path))
            UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "A playlist with the same name already exists!"); //TODO: localize
          else
          {
            try 
            {
              Files.createFile(path);
              Playlist playlist = new Playlist(path);
              mediator.addPlaylist(playlist);
              mediator.selectPlaylist(playlist);
            }
            catch (IOException ex)
            {
              ex.printStackTrace();
            }
          }
        }
      }
      

      
    });
    add(newPlaylist);
    
    JButton deletePlaylist = new JButton(Icon.DELETE_PLAYLIST.icon(24));
    deletePlaylist.setToolTipText("Delete current playlist"); //TODO: localize
    deletePlaylist.addActionListener(e -> {
      Playlist playlist = mediator.playlist();
      
      if (playlist != null)
      {
        //TODO: maybe ignore the setting?
        boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
            mediator.modalTarget(),
            "Warning",
            "Deleting a playlist can't be undone, do you want to proceed?"
        );
        
        if (confirmed)
        {
          try
          {
            if (Files.exists(playlist.path()))
              Files.delete(playlist.path());
            
            mediator.removePlaylist(playlist);
            mediator.selectPlaylist(null);
          }
          catch (IOException ex)
          {
            ex.printStackTrace();
          }
        }
      }
    });
    add(deletePlaylist);

    JButton save = new JButton(Icon.SAVE.icon(24));
    save.setToolTipText("Save playlist"); //TODO: localize
    save.addActionListener(e -> Tasks.Standalone.save(mediator.playlist()));
    add(save);
    
    addSeparator();
    
    JButton addEntry = new JButton(Icon.ADD_ENTRY.icon(24));
    addEntry.setToolTipText(Strings.HELP_ADD_NEW_ENTRY.text());
    addEntry.addActionListener(e -> Tasks.addEntryToPlaylist(mediator));
    add(addEntry);   
    
    JButton deleteSelectionButton = new JButton(Icon.DELETE_ENTRY.icon(24));
    deleteSelectionButton.setToolTipText(Strings.HELP_REMOVE_SELECTION_TOOLTIP.text());
    deleteSelectionButton.addActionListener(e -> Tasks.removeSelectedEntriesFromPlaylist(mediator));
    add(deleteSelectionButton);   
    
    addSeparator();
    
    JButton sortButton = new JButton(Icon.SORT_AZ.icon(24));
    sortButton.setToolTipText(Strings.HELP_SORT_PLAYLIST_TOOLTIP.text());
    sortButton.addActionListener(e -> executePlaylistTask(mediator, PlaylistTask.SortPlaylistAlphabetically, mediator.playlist()));
    add(sortButton);
    
    JButton removeTags = new JButton(Icon.REMOVE_TAGS.icon(24));
    removeTags.setToolTipText("Remove tags from entry names"); //TODO: localize
    removeTags.addActionListener(e -> executeEntryTaskOnPlaylist(mediator, EntryTask.RemoveTagsFromName, mediator.playlist()));
    add(removeTags);
    
    JButton renameToFilename = new JButton(Icon.RENAME_TO_FILENAME.icon(24));
    renameToFilename.setToolTipText("Rename all entries to match their filename"); //TODO: localize
    renameToFilename.addActionListener(e -> executeEntryTaskOnPlaylist(mediator, EntryTask.RenameEntryToMatchFileName, mediator.playlist()));
    add(renameToFilename);

    JButton relativize = new JButton(Icon.RELATIVIZE.icon(24));
    relativize.setToolTipText(Strings.HELP_RELATIVIZE_TO_RETROARCH.text());
    relativize.addActionListener(e -> Tasks.relativizePathsToRetroarch(mediator));
    add(relativize);
    
    JButton makeAbsolute = new JButton(Icon.MAKE_ABSOLUTE.icon(24));
    makeAbsolute.setToolTipText(Strings.HELP_MAKE_ABSOLUTE_PATHS.text());
    makeAbsolute.addActionListener(e -> Tasks.makePathsAbsolute(mediator));
    add(makeAbsolute);
  
    setFloatable(false);
  }
  
  public void executeEntryTaskOnPlaylist(Mediator mediator, EntryTask task, Playlist playlist)
  {
    executePlaylistTask(mediator, PlaylistTask.of(task), playlist);
  }
  
  private void executePlaylistTask(Mediator mediator, PlaylistTask task, Playlist playlist)
  {
    try
    {
      Tasks.executePlaylistTask(mediator, task, playlist);
    }
    catch (TaskException e)
    {
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", e.dialogMessage);
    }
  }
}
