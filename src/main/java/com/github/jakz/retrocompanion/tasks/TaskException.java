package com.github.jakz.retrocompanion.tasks;

public class TaskException extends Exception
{
  public final boolean shouldShowErrorDialog;
  public final String dialogMessage;
  
  public TaskException(String message)
  {
    super(message);
    shouldShowErrorDialog = false;
    dialogMessage = null;
  }
  
  public TaskException(String message, String dialogMessage)
  {
    super(message);
    shouldShowErrorDialog = true;
    this.dialogMessage = dialogMessage;
  }
}
