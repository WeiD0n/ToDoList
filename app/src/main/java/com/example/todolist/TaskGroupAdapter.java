package com.example.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder> {
    private List<TaskGroup> taskGroups;

    // Constructor to initialize the list of task groups
    public TaskGroupAdapter(List<TaskGroup> taskGroups) {
        this.taskGroups = taskGroups;
    }

    @Override
    public TaskGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item view layout for each task group
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_items, parent, false);
        return new TaskGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskGroupViewHolder holder, int position) {
        // Get the task group object at the current position
        TaskGroup taskGroup = taskGroups.get(position);

        // Set the task group name and task count in the respective views
        holder.groupNameTextView.setText(taskGroup.getGroupName());
        holder.taskCountTextView.setText("Tasks: " + taskGroup.getTaskCount());
    }

    @Override
    public int getItemCount() {
        return taskGroups.size(); // Return the number of items in the list
    }

    // ViewHolder class to represent each item in the RecyclerView
    public static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        TextView taskCountTextView;

        public TaskGroupViewHolder(View itemView) {
            super(itemView);
            // Initialize the TextViews
            groupNameTextView = itemView.findViewById(R.id.group_name);
            taskCountTextView = itemView.findViewById(R.id.task_count);
        }
    }
}
