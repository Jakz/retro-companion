package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.playlist.Core;
import com.github.jakz.retrocompanion.playlist.Playlist;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.TableModel;

public class CoreTablePanel extends JPanel
{
  private final Options options;
  
  private JTable table;
  private Model model;
  private DataSource<Core> cores;
  
  public CoreTablePanel(Options options)
  {
    this.options = options;
    
    table = new JTable();
    table.setAutoCreateRowSorter(true);
    model = new Model(table);
    
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    try 
    {
      model.addColumn(new ColumnSpec<>("Name", Core.class.getField("coreName"), false));
      model.addColumn(new ColumnSpec<>("Display Name", Core.class.getField("displayName"), false));
      model.addColumn(new ColumnSpec<>("System", Core.class.getField("systemName"), false));
      model.addColumn(new ColumnSpec<>("Path", Core.class.getField("path"), false));
    } 
    catch (NoSuchFieldException | SecurityException e) 
    {
      e.printStackTrace();
    }
  }
  
  public void setCores(List<Core> cores)
  {
    this.cores = DataSource.of(cores);
    model.setData(this.cores);
    model.fireTableStructureChanged();
  }
  
  private class Model extends TableModel<Core>
  {
    Model(JTable table)
    {
      super(table, DataSource.empty());
    }
  }
}
