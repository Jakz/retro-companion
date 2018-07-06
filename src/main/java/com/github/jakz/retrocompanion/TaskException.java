package com.github.jakz.retrocompanion;

public class TaskException extends Exception
{
  public final boolean shouldShowErrorDialog;
  public final String dialogTitle;
  public final String dialogMessage;
  
  public TaskException(String message)
  {
    super(message);
    shouldShowErrorDialog = false;
    dialogTitle = null;
    dialogMessage = null;
  }
  
  public TaskException(String message, String dialogTitle, String dialogMessage)
  {
    super(message);
    shouldShowErrorDialog = true;
    this.dialogTitle = dialogTitle;
    this.dialogMessage = dialogMessage;
  }
}
