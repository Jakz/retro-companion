package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.github.jakz.retrocompanion.data.Playlist;

public class MainPanel extends JPanel
{
  public final PlaylistTablePanel playlistPanel;
  public final EntryInfoPanel entryInfoPanel;
  public final Toolbar toolbar;
  public final JComboBox<Playlist> playlistChooser;
  
  public MainPanel(Mediator mediator)
  {
    toolbar = new Toolbar(mediator);
    playlistChooser = new JComboBox<>();
    entryInfoPanel = new EntryInfoPanel(mediator);
    playlistPanel = new PlaylistTablePanel(mediator);
    
    playlistChooser.addItemListener(e -> {
      mediator.onPlaylistSelected(playlistChooser.getItemAt(playlistChooser.getSelectedIndex()));
    });
    
    setLayout(new BorderLayout());
    
    JPanel mainContent = new JPanel();
    mainContent.setLayout(new BorderLayout());
    
    JPanel rightPanel = new JPanel(new BorderLayout());
    
    rightPanel.add(entryInfoPanel, BorderLayout.PAGE_START);
    
    mainContent.add(playlistChooser, BorderLayout.PAGE_START);
    mainContent.add(playlistPanel, BorderLayout.CENTER);
    
    add(toolbar, BorderLayout.PAGE_START);
    add(mainContent, BorderLayout.CENTER);
    add(rightPanel, BorderLayout.LINE_END);
  }
}
