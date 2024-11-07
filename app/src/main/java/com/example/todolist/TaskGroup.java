package com.example.todolist;

public class TaskGroup {
    private String groupName;  // Group name (e.g., "School", "Work")
    private int taskCount;     // Number of tasks in that group

    // Constructor to initialize the TaskGroup object
    public TaskGroup(String groupName, int taskCount) {
        this.groupName = groupName;
        this.taskCount = taskCount;
    }

    // Getter for the group name
    public String getGroupName() {
        return groupName;
    }

    // Getter for the task count
    public int getTaskCount() {
        return taskCount;
    }
}

