package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.jakz.retrocompanion.data.Playlist;

public class PlaylistInfoPanel extends JPanel
{
  private final Mediator mediator;
  private Playlist playlist;
  
  private JLabel nameCaption;
  private JTextField nameField;
  
  private JLabel countLabel;
  
  PlaylistInfoPanel(Mediator mediator)
  {
    this.mediator = mediator;
    
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Playlist Informations"));
    setLayout(new GridBagLayout());
    
    GridBagHolder c = new GridBagHolder();
    c.w(0.5f, 0.5f);
    
    nameCaption = new JLabel("Name: ");
    nameField = new JTextField(30);
    countLabel = new JLabel("0 entries");
    
    add(nameCaption, c.g(0, 0).w(1).leftInsets(10).left().c());  
    add(nameField, c.g(1, 0).w(3).center().hfill().c());  
    add(countLabel, c.g(4, 0).left().hInsets(10).c());
  }
  
  public void setPlaylist(Playlist playlist)
  {
    this.playlist = playlist;
    
    if (playlist != null)
    {
      nameField.setText(playlist.name());
      nameField.setEnabled(true);
      countLabel.setText(String.format("%d entries", playlist.size())); //TODO: localize
    }
    else
    {
      nameField.setText("");
      nameField.setEnabled(false);
      countLabel.setText("");
    }
  }
  
  public void refresh()
  {
    setPlaylist(playlist);
  }
}
