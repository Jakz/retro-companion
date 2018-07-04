package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.playlist.Entry;
import com.github.jakz.retrocompanion.playlist.Playlist;
import com.github.jakz.retrocompanion.playlist.ThumbnailType;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.TableModel;
import com.pixbits.lib.ui.table.editors.PathArgumentEditor;

public class PlaylistTablePanel extends JPanel
{
  private final Options options;
  
  private JTable table;
  private Model model;
  private Playlist playlist;
  
  public PlaylistTablePanel(Options options)
  {
    this.options = options;
    
    table = new JTable();
    model = new Model(table);
    
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    try
    {
      ColumnSpec<Entry,?> nameColumn = new ColumnSpec<>("Name", String.class, e -> e.name(), (e,v) -> {
        /* skip if name didn't change */
        if (v.equals(e.name()))
          return;
        
        final Entry entryWithSameName = playlist.get(v);
        
        /* don't do anything if name is already used for another entry */
        if (entryWithSameName != null && entryWithSameName != e)
          JOptionPane.showMessageDialog(this, "The name is already used for another entry!", "Error", JOptionPane.ERROR_MESSAGE);
        else
          e.rename(v, options);
      });
      nameColumn.setEditable(true);
      model.addColumn(nameColumn);
            
      ColumnSpec<Entry, Path> pathColumn = new ColumnSpec<Entry, Path>("Path", Entry.class.getField("path"), true);
      pathColumn.setEditor(new PathArgumentEditor(JFileChooser.FILES_ONLY));
      model.addColumn(pathColumn);
      
      final String[] thumbnailCaptions = { "B", "S", "T" };
      final ThumbnailType[] thumbnailType = { ThumbnailType.BOXART, ThumbnailType.SNAP, ThumbnailType.TITLE };

      for (int i = 0; i < 1/*ThumbnailType.values().length*/; ++i)
      {
        final ThumbnailType tt = thumbnailType[i];
        model.addColumn(new ColumnSpec<>(thumbnailCaptions[i], boolean.class, e -> Files.exists(options.pathForThumbnail(playlist, tt, e))));
      }
      
      model.setDefaultRenderer(boolean.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          JLabel field = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          
          if ((boolean)value)
          {
            field.setForeground(Color.GREEN);
            field.setText("✔");
          }
          else
          {
            field.setForeground(Color.RED);
            field.setText("❌");
          }
          
          return field;
        }
      });
      
      model.fireTableStructureChanged();
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
