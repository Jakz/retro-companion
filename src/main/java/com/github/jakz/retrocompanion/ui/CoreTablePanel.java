package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Playlist;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.TableModel;

public class CoreTablePanel extends JPanel
{
  private final Mediator mediator;
  
  private JTable table;
  private Model model;
  private DataSource<Core> cores;
  
  public CoreTablePanel(Mediator mediator)
  {
    this.mediator = mediator;
    
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
      model.addColumn(new ColumnSpec<>("Path", String.class, c -> c.shortLibraryName()));
    } 
    catch (NoSuchFieldException | SecurityException e) 
    {
      e.printStackTrace();
    }
  }
  
  public void setCores(CoreSet cores)
  {
    this.cores = cores;
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
