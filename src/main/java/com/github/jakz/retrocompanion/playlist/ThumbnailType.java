package com.github.jakz.retrocompanion.playlist;

public enum ThumbnailType
{
  BOXARTS("Named_Boxarts"),
  SNAPS("Named_Snaps"),
  TITLES("Named_Titles")
  
  ;
  
  private ThumbnailType(String folderName)
  {
    this.folderName = folderName;
  }
  
  public String folderName;
}
