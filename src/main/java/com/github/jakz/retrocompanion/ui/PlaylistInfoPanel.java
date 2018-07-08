package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PlaylistInfoPanel extends JPanel
{
  private final Mediator mediator;
  
  PlaylistInfoPanel(Mediator mediator)
  {
    this.mediator = mediator;
    
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Playlist Informations"));
    setLayout(new GridBagLayout());
    
    GridBagHolder g = new GridBagHolder();
    g.w(0.5f, 0.5f);
  }
}
