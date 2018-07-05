package com.github.jakz.retrocompanion.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

class GridBagHolder
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