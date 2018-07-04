package com.github.jakz.retrocompanion.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.jakz.retrocompanion.Options;
import com.pixbits.lib.ui.elements.BrowseButton;

public class PathsPanel extends JPanel
{
  private static class GridBagHolder
  {
    private GridBagConstraints c;
    
    GridBagHolder() { c = new GridBagConstraints(); }
    
    public GridBagHolder w(float x, float y) { c.weightx = x; c.weighty = y; return this; }
    public GridBagHolder g(int x, int y) { c.gridx = x; c.gridy = y; return this; }
    public GridBagHolder w(int w) { c.gridwidth = w; return this; }
    public GridBagHolder a(int a) { c.anchor = a; return this; }
    
    public GridBagHolder rightInsets(int v) { c.insets = new Insets(0, 0, 0, v); return this; }
    public GridBagHolder noInsets() { c.insets = new Insets(0,0,0,0); return this; }
    
    public GridBagHolder lineEnd() { return a(GridBagConstraints.LINE_END); }
    public GridBagHolder center() { return a(GridBagConstraints.CENTER); }

    
    public GridBagConstraints c() { return c; }
  }
  
  private final Options options;
  
  private final String[] captions = { "RetroArch Path", "Playlists Path", "Thumbnails Path" };
  private final BrowseButton[] browseFields;
    
  public PathsPanel(Options options)
  {
    this.options = options;
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
      options.derivePathsFromRetroarch();
    });
    
  }
  
  private void onPathChanged()
  {
    options.retroarchPath = browseFields[0].getPath();
    options.playlistsPath = browseFields[1].getPath();
    options.thumbnailsPath = browseFields[2].getPath();
  }
}
