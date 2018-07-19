package com.github.jakz.retrocompanion.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.tasks.EntryTask;
import com.github.jakz.retrocompanion.tasks.PlaylistTask;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.ui.UIUtils;

public class Toolbar extends JToolBar 
{
  private final Mediator mediator;
  private CoreSelectMenu coreSelectMenu;

  private final int ICON_SIZE = 32;
  
  private JLabel summaryLabel;
  
  public Toolbar(Mediator mediator)
  {
    this.mediator = mediator;
    
    summaryLabel = new JLabel();
    
    JButton newPlaylist = new JButton(Icon.NEW_PLAYLIST.icon(ICON_SIZE));
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
    
    JButton deletePlaylist = new JButton(Icon.DELETE_PLAYLIST.icon(ICON_SIZE));
    deletePlaylist.setToolTipText("Delete current playlist"); //TODO: localize
    deletePlaylist.addActionListener(e -> Tasks.executeTaskUI(mediator, PlaylistTask.DeletePlaylist));
    add(deletePlaylist);

    JButton save = new JButton(Icon.SAVE.icon(ICON_SIZE));
    save.setToolTipText("Save playlist"); //TODO: localize
    save.addActionListener(e -> Tasks.executeTaskUI(mediator, PlaylistTask.SavePlaylist));
    add(save);
    
    addSeparator();
    
    JButton addEntry = new JButton(Icon.ADD_ENTRY.icon(ICON_SIZE));
    addEntry.setToolTipText(Strings.HELP_ADD_NEW_ENTRY.text());
    addEntry.addActionListener(e -> Tasks.executeTaskUI(mediator, PlaylistTask.AddNewEntryToPlaylist));
    add(addEntry);   
    
    JButton deleteSelectionButton = new JButton(Icon.DELETE_ENTRY.icon(ICON_SIZE));
    deleteSelectionButton.setToolTipText(Strings.HELP_REMOVE_SELECTION_TOOLTIP.text());
    deleteSelectionButton.addActionListener(e -> Tasks.removeSelectedEntriesFromPlaylist(mediator));
    add(deleteSelectionButton);   
    
    addSeparator();
    
    JButton sortButton = new JButton(Icon.SORT_AZ.icon(ICON_SIZE));
    sortButton.setToolTipText(Strings.HELP_SORT_PLAYLIST_TOOLTIP.text());
    sortButton.addActionListener(e -> Tasks.executeTaskUI(mediator, PlaylistTask.SortPlaylistAlphabetically));
    add(sortButton);
    
    JButton removeTags = new JButton(Icon.REMOVE_TAGS.icon(ICON_SIZE));
    removeTags.setToolTipText("Remove tags from entry names"); //TODO: localize
    removeTags.addActionListener(e -> Tasks.executeEntryTaskOnPlaylistUI(mediator, EntryTask.RemoveTagsFromName));
    add(removeTags);
    
    JButton renameToFilename = new JButton(Icon.RENAME_TO_FILENAME.icon(ICON_SIZE));
    renameToFilename.setToolTipText("Rename all entries to match their filename"); //TODO: localize
    renameToFilename.addActionListener(e -> Tasks.executeEntryTaskOnPlaylistUI(mediator, EntryTask.RenameEntryToMatchFileName));
    add(renameToFilename);

    JButton relativize = new JButton(Icon.RELATIVIZE.icon(ICON_SIZE));
    relativize.setToolTipText(Strings.HELP_RELATIVIZE_TO_RETROARCH.text());
    relativize.addActionListener(e -> Tasks.relativizePathsToRetroarch(mediator));
    add(relativize);
    
    JButton makeAbsolute = new JButton(Icon.MAKE_ABSOLUTE.icon(ICON_SIZE));
    makeAbsolute.setToolTipText(Strings.HELP_MAKE_ABSOLUTE_PATHS.text());
    makeAbsolute.addActionListener(e -> Tasks.makePathsAbsolute(mediator));
    add(makeAbsolute);
    
    JButton setCore = new JButton(Icon.SET_CORE.icon(ICON_SIZE));
    setCore.setToolTipText("Set same core for all entries");
    
    setCore.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (coreSelectMenu == null)
          coreSelectMenu = new CoreSelectMenu(mediator.options().cores);
        
        coreSelectMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    
    add(setCore);
    
    addSeparator();
    
    add(summaryLabel);
    
    add(Box.createHorizontalGlue());
    
    JButton launchRetroarch = new JButton(Icon.RETROARCH.icon(ICON_SIZE));
    launchRetroarch.setToolTipText("Launch RetroArch"); //TODO: localize
    launchRetroarch.addActionListener(e -> Tasks.executeTaskUI(mediator, Tasks.LaunchRetroarch));
    add(launchRetroarch);
    
    JButton options = new JButton(Icon.OPTIONS.icon(ICON_SIZE));
    options.setToolTipText("Options"); //TODO: localize
    options.addActionListener(e -> mediator.showOptions());
    add(options);
  
    setFloatable(false);
  }
  
  public void updateSummaryLabel(Mediator mediator)
  {
    int playlistCount = mediator.playlists().size();
    int entriesCount = 0;
    long totalSizeInBytes = 0;
    
    for (Playlist playlist : mediator.playlists())
    {
      entriesCount += playlist.size();
      totalSizeInBytes += playlist.sizeInBytes(mediator);
    }
    
    summaryLabel.setText(String.format("%d entries in %d playlists (%s)", entriesCount, playlistCount, StringUtils.humanReadableByteCount(totalSizeInBytes, true, true)));
  }
  
  private class CoreSelectMenu extends JPopupMenu
  {
    public CoreSelectMenu(CoreSet set)
    {
      Map<String, List<Core>> coreBySystem = set.stream()
          .collect(Collectors.groupingBy(Core::systemName, () -> new TreeMap<>(), Collectors.toList()));
      
      JMenuItem detect = new JMenuItem("Auto-Detect"); //TODO: localize
      detect.addActionListener(e -> Tasks.executeEntryTaskOnPlaylistUI(mediator, EntryTask.AssignCore(Optional.empty())));
      add(detect);
      
      coreBySystem.forEach((k, cores) -> {
        JMenu menu = new JMenu(k);
        add(menu);

        for (Core core : cores)
        {
          JMenuItem coreItem = new JMenuItem(core.shortLibraryName());
          coreItem.addActionListener(e -> Tasks.executeEntryTaskOnPlaylistUI(mediator, EntryTask.AssignCore(Optional.ofNullable(new Core.Ref(core)))));
          menu.add(coreItem);
        }
      });
    }
  }
}
