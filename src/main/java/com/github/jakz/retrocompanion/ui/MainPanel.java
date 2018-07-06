package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.github.jakz.retrocompanion.data.Playlist;

public class MainPanel extends JPanel
{
  public final PlaylistTablePanel playlistPanel;
  public final Toolbar toolbar;
  public final JComboBox<Playlist> playlistChooser;
  
  public MainPanel(Mediator mediator)
  {
    toolbar = new Toolbar(mediator);
    playlistChooser = new JComboBox<>();
    playlistPanel = new PlaylistTablePanel(mediator);
    
    playlistChooser.addItemListener(e -> {
      mediator.selectPlaylist(playlistChooser.getItemAt(playlistChooser.getSelectedIndex()));
    });
    
    setLayout(new BorderLayout());
    
    JPanel mainContent = new JPanel(new BorderLayout());
    
    mainContent.add(playlistChooser, BorderLayout.PAGE_START);
    mainContent.add(playlistPanel, BorderLayout.CENTER);
    
    add(toolbar, BorderLayout.PAGE_START);
    add(mainContent, BorderLayout.CENTER);
  }
}
