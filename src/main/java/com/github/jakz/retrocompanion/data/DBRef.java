package com.github.jakz.retrocompanion.data;

public class DBRef
{
  public static class CRC extends DBRef
  {
    private final long crc;
    
    public CRC(long crc)
    {
      this.crc = crc;
    }
    
    public String toString()
    {
      return String.format("%08X|crc", crc);
    }
  }
}
