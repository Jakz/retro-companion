package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.github.jakz.retrocompanion.Option;
import com.github.jakz.retrocompanion.Options;
import com.pixbits.lib.ui.elements.BrowseButton;

public class OptionsPanel extends JPanel
{
  private final Mediator mediator;
  
  private final String[] captions = { "RetroArch Path", "Cores Path", "Info Path", "Playlists Path", "Thumbnails Path", "Save States Paths", "Saves Path", "Theme Path"};
  private final BrowseButton[] browseFields;
  
  private final Option<?>[] optionFields;
  
  public Border sectionBorder(String title)
  {
    return 
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), title), 
        BorderFactory.createEmptyBorder(5,5,5,5)
      );
  }
    
  public OptionsPanel(Mediator mediator)
  {
    this.mediator = mediator;
    final int count = captions.length;
    
    //TODO: localize
    optionFields = new Option<?>[] {
      new Option<Boolean>("Auto-Fix playlist names in file", "autoFixPlaylistNamesInEntries", mediator.options()),
      new Option<Boolean>("Show confirmation dialogs for undoable operations", "showConfirmationDialogForUndoableOperations", mediator.options()),
      new Option<Boolean>("Relativize paths to Retroarch when importing", "autoRelativizePathsWhenImporting", mediator.options()),
      new Option<Boolean>("Skipping importing already present files", "skipImportingDuplicates", mediator.options()),
      new Option<Boolean>("Overwrite thumbnails without confirmation", "overwriteThumbnailWithoutConfirmation", mediator.options()),
      new Option<Boolean>("Move thumbnails instead that copying them", "thumbnailMoveInsteadThanCopy", mediator.options()),
    };
    
    GridBagHolder c = new GridBagHolder();

    // paths panel
    JPanel pathsPanel = new JPanel();
    
    pathsPanel.setBorder(sectionBorder("Paths"));
    pathsPanel.setLayout(new GridBagLayout());

    c.w(0.5f, 0.5f);
    
    browseFields = new BrowseButton[count];
    
    for (int i = 0; i < count; ++i)
    {
      JLabel label = new JLabel(captions[i]);
      label.setHorizontalAlignment(JLabel.RIGHT);
      pathsPanel.add(label, c.g(0,i).rightInsets(20).lineEnd().c());
      
      browseFields[i] = new BrowseButton(30, BrowseButton.Type.DIRECTORIES);
      browseFields[i].setCallback(p -> onPathChanged());

      pathsPanel.add(browseFields[i], c.g(1,i).w(2).noInsets().center().c());  
    }
    
    browseFields[0].setCallback(p -> {
      onPathChanged();
      mediator.options().derivePathsFromRetroarch();
    });
     
    // options panel
    JPanel optionsPanel = new JPanel();
    optionsPanel.setBorder(sectionBorder("Options"));
    optionsPanel.setLayout(new GridBagLayout());
    
    c.w(1).noInsets().left();
    
    for (int i = 0; i < optionFields.length; ++i)
    {
      Option<?> option = optionFields[i];
      
      optionsPanel.add(option.getComponent(), c.g(0, i).c());
    }

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(pathsPanel);
    add(optionsPanel);
    
  }
  
  public void refresh()
  {
    Options options = mediator.options();
    
    browseFields[0].setText(options.retroarchPath.toString());
    browseFields[1].setText(options.coresPath.toString());
    browseFields[2].setText(options.infoPath.toString());
    browseFields[3].setText(options.playlistsPath.toString());
    browseFields[4].setText(options.thumbnailsPath.toString());
    browseFields[5].setText(options.statesPath.toString());
    browseFields[6].setText(options.savesPath.toString());
    browseFields[7].setText(options.themePath.toString());

    
    for (Option<?> option : optionFields) option.refresh();
  }
  
  private void onPathChanged()
  {
    Options options = mediator.options();
    
    options.retroarchPath = browseFields[0].getPath();
    options.coresPath = browseFields[1].getPath();
    options.infoPath = browseFields[2].getPath();
    options.playlistsPath = browseFields[3].getPath();
    options.thumbnailsPath = browseFields[4].getPath();
    options.statesPath = browseFields[5].getPath();
    options.savesPath = browseFields[6].getPath();
    options.themePath = browseFields[7].getPath();
  }
}
