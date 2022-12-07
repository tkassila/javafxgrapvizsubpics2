package com.metait.javafxgrapvizsubpics.tasks;
/**
 * This class is used contain returning values from a run execting service task.
 */
public class TaskResult {
    private String msg;
    private String strExecuteValue;
    private boolean bExecuting;
    private int exitValue = -1;

    public TaskResult(){}

    public TaskResult(String p_strExecuteValue, String p_msg, boolean p_bExecuting)
    {
        strExecuteValue = p_strExecuteValue;
        msg = p_msg;
        bExecuting = p_bExecuting;
    }

    public String getExecuteValue() {
        return strExecuteValue;
    }

    public void setExecuteValue(String value)
    {
        strExecuteValue = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String p_msg) {
        msg = p_msg;
    }

    public boolean isExecuting() {
        return bExecuting;
    }

    public void setExecuting(boolean bValue) {
        bExecuting = bValue;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }
}
