package com.github.jakz.retrocompanion.playlist;

public enum ThumbnailType
{
  BOXART("Named_Boxarts"),
  SNAP("Named_Snaps"),
  TITLE("Named_Titles")
  
  ;
  
  private ThumbnailType(String folderName)
  {
    this.folderName = folderName;
  }
  
  public String folderName;
}
