package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.ui.elements.JPlaceHolderTextField;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.FilterableDataSource;
import com.pixbits.lib.ui.table.SimpleListSelectionListener;
import com.pixbits.lib.ui.table.TableModel;
import com.pixbits.lib.ui.table.editors.PathArgumentEditor;
import com.pixbits.lib.ui.table.renderers.AlternateColorTableCellRenderer;
import com.pixbits.lib.ui.table.renderers.DefaultTableAndListRenderer;

public class PlaylistTablePanel extends JPanel
{
  private final Mediator mediator;
  private final Options options;
  
  private PlaylistTable table;
  private Model model;
  private Playlist playlist;
  
  private JPlaceHolderTextField searchField;
  
  private final EntryPopupMenu entryPopupMenu;
  
  @SuppressWarnings("unchecked")
  public PlaylistTablePanel(Mediator mediator)
  {
    this.mediator = mediator;
    this.options = mediator.options();
    
    table = new PlaylistTable();
    model = new Model(table);
    searchField = new JPlaceHolderTextField(20, "search");
    entryPopupMenu = new EntryPopupMenu(mediator);
    
    
    JScrollPane tablePane = new JScrollPane(table);
    tablePane.setPreferredSize(new Dimension(800, 400));
    
    setLayout(new BorderLayout());
    add(tablePane, BorderLayout.CENTER);
    add(searchField, BorderLayout.SOUTH);
    
    searchField.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override public void insertUpdate(DocumentEvent e) { table.repaint(); }
      @Override public void removeUpdate(DocumentEvent e) { table.repaint(); }
      @Override public void changedUpdate(DocumentEvent e) { table.repaint(); }    
    });
    
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
        {
          e.rename(v, options);
          mediator.onEntrySelected(e);
        }
      });
      nameColumn.setWidth(250);
      nameColumn.setEditable(true);
      model.addColumn(nameColumn);
      
      final String[] thumbnailCaptions = { "B", "S", "T" };
      final ThumbnailType[] thumbnailType = { ThumbnailType.BOXART, ThumbnailType.SNAP, ThumbnailType.TITLE };

      for (int i = 0; i < 1/*ThumbnailType.values().length*/; ++i)
      {
        final ThumbnailType tt = thumbnailType[i];
        
        ColumnSpec<Entry, ?> thumbnailColumn = new ColumnSpec<>(thumbnailCaptions[i], boolean.class, e -> Files.exists(options.pathForThumbnail(playlist, tt, e)));
        thumbnailColumn.setWidth(30);
        model.addColumn(thumbnailColumn);
      }
      
      model.setDefaultRenderer(boolean.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          JLabel field = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          
          field.setHorizontalAlignment(JLabel.CENTER);
          
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
      
      ColumnSpec<Entry, String> formatColumn = new ColumnSpec<Entry, String>("F", String.class, (e -> {
        String ext = FileUtils.pathExtension(e.path()).toLowerCase();
        return ext;
      }));
      formatColumn.setWidth(60);
      model.addColumn(formatColumn);
      
      ColumnSpec<Entry, String> sizeColumn = new ColumnSpec<>("S", String.class, e -> StringUtils.humanReadableByteCount(e.sizeInBytes()));
      sizeColumn.setWidth(80);
      model.addColumn(sizeColumn);
      
      //e.core().map(Core.Ref::shortLibraryName).orElse("DETECT")
      Function<Entry, Optional<Core.Ref>> getter = e -> e.core();
      BiConsumer<Entry, Optional<Core.Ref>> setter = (e,v) -> e.setCore(v.map(Core.Ref::dupe));
      ColumnSpec<Entry, Optional<Core.Ref>> coreColumn = new ColumnSpec<Entry, Optional<Core.Ref>>("Core", (Class<Optional<Core.Ref>>)(Class<?>)Optional.class, getter, setter);
      model.addColumn(coreColumn);

      DefaultTableAndListRenderer<Optional<Core.Ref>> renderer = new DefaultTableAndListRenderer<>()
      {
        @Override
        public void decorate(JLabel label, JComponent source, Optional<Core.Ref> value, int index, boolean isSelected, boolean hasFocus)
        {
          label.setFont(PlaylistTablePanel.this.getFont().deriveFont(PlaylistTablePanel.this.getFont().getSize()*0.8f));
          label.setText(value.map(c -> c.shortLibraryName()).orElse("DETECT"));
        }
      };
      
      List<Optional<Core.Ref>> cores = options.cores.buildRefList();
      JComboBox<Optional<Core.Ref>> comboBox = new JComboBox<>(cores.toArray(new Optional[cores.size()]));
      comboBox.setRenderer(renderer);
      
      coreColumn.setEditable(true);
      coreColumn.setRenderer(renderer);
      coreColumn.setEditor(new DefaultCellEditor(comboBox));
      coreColumn.setWidth(150);
      
      ColumnSpec<Entry, Path> pathColumn = new ColumnSpec<Entry, Path>("Path", Path.class, e -> e.path());
      pathColumn.setEditor(new PathArgumentEditor(JFileChooser.FILES_ONLY));
      model.addColumn(pathColumn);
      
      model.fireTableStructureChanged();
      
      table.getSelectionModel().addListSelectionListener(SimpleListSelectionListener.ofJustSingle(i -> {
        mediator.onEntrySelected(i != -1 ? model.data().get(i) : null);      
      }));
      
      table.addMouseListener(new MouseAdapter() {
        private void handlePopup(MouseEvent e)
        {
          if (e.isPopupTrigger())
          {
            int row = table.rowAtPoint(e.getPoint());
            
            if (row != -1)
            {
              table.setRowSelectionInterval(row, row);
              Entry entry = model.data().get(table.convertRowIndexToModel(row));
              entryPopupMenu.rebuild(entry);
              entryPopupMenu.show(e.getComponent(), e.getX(), e.getY());

            }
          }
        }
        
        @Override public void mousePressed(MouseEvent e) { handlePopup(e); }        
        @Override public void mouseReleased(MouseEvent e) { handlePopup(e); }
      });
      
      table.setDragEnabled(true);
      table.setDropMode(DropMode.INSERT_ROWS);
      table.setTransferHandler(new PlaylistTableTransferHandler(mediator, table, model));
      table.setFillsViewportHeight(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private class PlaylistTable extends JTable
  {
    @Override public Component prepareRenderer(TableCellRenderer renderer, int r, int c)
    {
      JComponent component = (JComponent)super.prepareRenderer(renderer, r, c);

      if (table.getColumnClass(c) == Boolean.class || table.getColumnClass(c) == Boolean.TYPE)
        return component;
      
      String search = searchField.getText();
      Entry entry = model.data().get(convertRowIndexToModel(r));
      
      if (!search.isEmpty())
      {
        
        boolean matches = entry.name().toLowerCase().contains(search.toLowerCase());
       
        component.setForeground(matches ? Color.BLACK : Color.LIGHT_GRAY);
        if (matches)
        {
          return component;
        }
      }
      else
        component.setForeground(Color.BLACK);
      
      if (!Files.exists(entry.path()))
        component.setForeground(Color.RED);
      
      /*
      boolean isSelected = false;
      for (int i : table.getSelectedRows())
      {
        if (i == r)
        {
          isSelected = true;
          break;
        }
      }

      AlternateColorTableCellRenderer.setBackgroundColor(component, isSelected, r);*/
      
      return component;
    }
  }
  
  public List<Entry> getSelectedEntries()
  {
    int[] rows = table.getSelectedRows();
        
    return Arrays.stream(rows)
        .map(table::convertRowIndexToModel)
        .mapToObj(playlist::get)
        .collect(Collectors.toList());
  }
  
  public boolean selectEntry(Entry entry)
  {
    if (playlist != null)
    {
      int index = playlist.indexOf(entry);
      
      if (index != -1)
      {
        int aindex = table.convertRowIndexToView(index);
        table.getSelectionModel().setSelectionInterval(aindex, aindex);
        return true;
      }
    }
    
    return false;
  }
  
  public void setPlaylist(Playlist playlist)
  {
    this.playlist = playlist;
    table.clearSelection();
    model.setData(playlist != null ? playlist : DataSource.empty());
    refresh();
  }
  
  public Playlist playlist()
  {
    return playlist;
  }
  
  public void refresh()
  {
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
