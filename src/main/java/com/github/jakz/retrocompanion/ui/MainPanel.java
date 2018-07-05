package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.github.jakz.retrocompanion.Options;

public class MainPanel extends JPanel
{
  public final PlaylistTablePanel playlistPanel;
  public final Toolbar toolbar;
  
  public MainPanel(Mediator mediator)
  {
    playlistPanel = new PlaylistTablePanel(mediator);
    toolbar = new Toolbar(mediator);
    
    setLayout(new BorderLayout());
    add(playlistPanel, BorderLayout.CENTER);
    add(toolbar, BorderLayout.PAGE_START);
  }
}
