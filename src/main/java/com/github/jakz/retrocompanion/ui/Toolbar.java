package com.github.jakz.retrocompanion.ui;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.Tasks;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.pixbits.lib.ui.UIUtils;

public class Toolbar extends JToolBar 
{
  private final Mediator mediator;
  
  public Toolbar(Mediator mediator)
  {
    this.mediator = mediator;

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
    sortButton.addActionListener(e -> Tasks.sortPlaylistAlphabetically(mediator));
    add(sortButton);
    
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
}
