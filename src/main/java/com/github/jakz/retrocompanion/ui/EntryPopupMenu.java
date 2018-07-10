package com.github.jakz.retrocompanion.ui;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.tasks.EntryTask;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.io.archive.ArchiveFormat;

public class EntryPopupMenu extends JPopupMenu
{
  private final Mediator mediator;
  
  private final JMenu compressMenu;
  
  private final JMenuItem uncompress;
  private final JMenuItem openInExplorer;
  
  private Entry entry;
  
  public EntryPopupMenu(Mediator mediator)
  {
    this.mediator = mediator;
    
    compressMenu = new JMenu("Compress to...");
    
    JMenuItem toZip = new JMenuItem("ZIP");
    toZip.addActionListener(e -> Tasks.executeTaskOnEntryUI(mediator, EntryTask.CompressEntry(ArchiveFormat.ZIP), entry));
    JMenuItem to7Z = new JMenuItem("7Z");
    to7Z.addActionListener(e -> Tasks.executeTaskOnEntryUI(mediator, EntryTask.CompressEntry(ArchiveFormat._7ZIP), entry));

    compressMenu.add(toZip);
    compressMenu.add(to7Z);
    
    uncompress = new JMenuItem("Uncompress");
    
    openInExplorer = new JMenuItem("Open in file explorer");
    openInExplorer.addActionListener(e -> Tasks.executeTaskUI(mediator, Tasks.OpenFileInExplorer(() -> entry.absolutePath(mediator))));
  }
  
  void rebuild(Entry entry)
  {
    this.entry = entry;
    
    removeAll();
    
    add(openInExplorer);
    addSeparator();
    add(entry.isCompressed() ? uncompress : compressMenu);   
  }
}
