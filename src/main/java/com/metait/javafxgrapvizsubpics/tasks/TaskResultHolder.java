package com.metait.javafxgrapvizsubpics.tasks;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class instances are hold a task result of a service, which has TaskResult as return value for a task service.
 * The hold task result is modified after a service instance by the code of main control. At las this control
 * is listen same result holder value proerty.
 */
public class TaskResultHolder {
    private TaskResult taskResult;
    private ObjectProperty<TaskResult> valuePropertyHolder = new SimpleObjectProperty<TaskResult>();

    public TaskResultHolder(AbstractBaseService service)
    {
        service.valueProperty().addListener((ols, oldData, newData) -> {
            if (newData != null) {
                taskResult = (TaskResult) newData;
                valuePropertyHolder.setValue(newData);
            }
        });
    }

    public final ObjectProperty<TaskResult> valueProperty()
    {
        return valuePropertyHolder;
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
        valuePropertyHolder.setValue(taskResult);
    }
}
