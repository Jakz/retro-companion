package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.github.jakz.retrocompanion.playlist.Entry;
import com.github.jakz.retrocompanion.playlist.Playlist;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.TableModel;
import com.pixbits.lib.ui.table.editors.PathArgumentEditor;

public class PlaylistTablePanel extends JPanel
{
  private JTable table;
  private Model model;
  private Playlist playlist;
  
  public PlaylistTablePanel()
  {
    table = new JTable();
    model = new Model(table);
    
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    try
    {
      model.addColumn(new ColumnSpec<>("Name", Entry.class.getField("name"), true));
      
      ColumnSpec<Entry, Path> pathColumn = new ColumnSpec<Entry, Path>("Path", Entry.class.getField("path"), true);
      pathColumn.setEditor(new PathArgumentEditor(JFileChooser.FILES_ONLY));
      model.addColumn(pathColumn);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void setPlaylist(Playlist playlist)
  {
    this.playlist = playlist;
    model.setData(playlist);
    model.fireTableDataChanged();
  }
  
  
  private class Model extends TableModel<Entry>
  {
    
    Model(JTable table)
    {
      super(table, DataSource.empty());
    }
  }
  
}
