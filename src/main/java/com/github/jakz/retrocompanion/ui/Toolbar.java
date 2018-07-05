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
        
    JButton sortButton = new JButton(Icon.SORT_AZ.icon(24));
    sortButton.setToolTipText(Strings.HELP_SORT_PLAYLIST_TOOLTIP.text());
    sortButton.addActionListener(e -> Tasks.sortPlaylistAlphabetically(mediator));
    add(sortButton);
    
    addSeparator();
    
    JButton deleteSelectionButton = new JButton(Icon.DELETE_SELECTION.icon(24));
    deleteSelectionButton.setToolTipText(Strings.HELP_REMOVE_SELECTION_TOOLTIP.text());
    deleteSelectionButton.addActionListener(e -> Tasks.removeSelectedEntriesFromPlaylist(mediator));
    add(deleteSelectionButton);   
  
    setFloatable(false);
  }
}
