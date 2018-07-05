package com.github.jakz.retrocompanion.ui;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import com.github.jakz.retrocompanion.Options;
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
    add(sortButton);
    
    sortButton.addActionListener(e -> {
      Playlist playlist = mediator.playlist();
      
      if (playlist != null)
      {
        //TODO: localize
        boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
            Toolbar.this.getParent(),
            "Warning",
            "Sorting the playlist can't be undone, are you sure you want to proceed?"
        );
        
        if (confirmed)
        {                 
          List<Entry> entries = playlist.stream()
              .sorted((e1, e2) -> e1.name().compareToIgnoreCase(e2.name()))
              .collect(Collectors.toList());
          
          playlist.clear();
          entries.forEach(playlist::add);
          
          mediator.selectPlaylist(playlist);
        }
      }
    });
    
    addSeparator();
    
    JButton deleteSelectionButton = new JButton(Icon.DELETE_SELECTION.icon(24));
    deleteSelectionButton.setToolTipText(Strings.HELP_REMOVE_SELECTION_TOOLTIP.text());
    add(deleteSelectionButton);
    
    deleteSelectionButton.addActionListener(e -> {
      Playlist playlist = mediator.playlist();
      List<Entry> entries = mediator.getSelectedEntries();
      
      if (playlist != null && !entries.isEmpty())
      {
        //TODO: localize
        boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
            Toolbar.this.getParent(),
            "Warning",
            "This can't be undone, are you sure you want to proceed?"
        );
        
        if (confirmed)
          mediator.removeEntriesFromPlaylist(entries);
      }
      
    });
    
    setFloatable(false);
  }
}
