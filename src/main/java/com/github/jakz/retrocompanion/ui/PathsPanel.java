package com.github.jakz.retrocompanion.ui;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.jakz.retrocompanion.Options;
import com.pixbits.lib.ui.elements.BrowseButton;

public class PathsPanel extends JPanel
{
  private final Mediator mediator;
  
  private final String[] captions = { "RetroArch Path", "Cores Path", "Info Path", "Playlists Path", "Thumbnails Path"};
  private final BrowseButton[] browseFields;
    
  public PathsPanel(Mediator mediator)
  {
    this.mediator = mediator;
    final int count = captions.length;
    
    this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Paths"), BorderFactory.createEmptyBorder(5,5,5,5)));
    this.setLayout(new GridBagLayout());
    GridBagHolder c = new GridBagHolder();
    
    c.w(0.5f, 0.5f);
    
    browseFields = new BrowseButton[count];
    
    for (int i = 0; i < count; ++i)
    {
      JLabel label = new JLabel(captions[i]);
      label.setHorizontalAlignment(JLabel.RIGHT);
      this.add(label, c.g(0,i).rightInsets(20).lineEnd().c());
      
      browseFields[i] = new BrowseButton(30, BrowseButton.Type.DIRECTORIES);
      browseFields[i].setCallback(p -> onPathChanged());

      this.add(browseFields[i], c.g(1,i).w(2).noInsets().center().c());  
    }
    
    browseFields[0].setCallback(p -> {
      onPathChanged();
      mediator.options().derivePathsFromRetroarch();
    });
    
  }
  
  public void refresh()
  {
    Options options = mediator.options();
    
    browseFields[0].setText(options.retroarchPath.toString());
    browseFields[1].setText(options.coresPath.toString());
    browseFields[2].setText(options.infoPath.toString());
    browseFields[3].setText(options.playlistsPath.toString());
    browseFields[4].setText(options.thumbnailsPath.toString());
  }
  
  private void onPathChanged()
  {
    Options options = mediator.options();
    
    options.retroarchPath = browseFields[0].getPath();
    options.coresPath = browseFields[1].getPath();
    options.infoPath = browseFields[2].getPath();
    options.playlistsPath = browseFields[3].getPath();
    options.thumbnailsPath = browseFields[4].getPath();
  }
}
