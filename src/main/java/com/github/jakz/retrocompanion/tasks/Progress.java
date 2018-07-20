package com.github.jakz.retrocompanion.tasks;

@FunctionalInterface
public interface Progress
{
  void report(float progress, String text);
  
  public static Progress DUMMY = (p, t) -> { };
}
