package com.demo.workflow.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sling Model for Review History Component
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ReviewHistoryModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    @RequestAttribute(name = "workflowId")
    @Default(values = "")
    private String workflowId;

    private String workflowTitle;
    private String startDate;
    private String initiator;
    private String totalDuration;
    private String currentStatus;
    private String statusColor;
    private int progressPercent;
    private int completedSteps;
    private int totalSteps;
    private List<Step> steps;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    @PostConstruct
    protected void init() {
        steps = new ArrayList<>();
        loadWorkflowHistory();
    }

    private void loadWorkflowHistory() {
        // In production, query WorkflowSession for workflow instance
        // For demo, provide sample data structure
        workflowTitle = "Content Approval Workflow";
        startDate = DATE_FORMAT.format(new Date());
        initiator = resourceResolver.getUserID();
        currentStatus = "In Review";
        statusColor = "blue";
        totalSteps = 5;
        completedSteps = 2;
        progressPercent = (completedSteps * 100) / totalSteps;
        totalDuration = "2 days 4 hours";
    }

    // Getters
    public String getWorkflowId() { return workflowId; }
    public String getWorkflowTitle() { return workflowTitle; }
    public String getStartDate() { return startDate; }
    public String getInitiator() { return initiator; }
    public String getTotalDuration() { return totalDuration; }
    public String getCurrentStatus() { return currentStatus; }
    public String getStatusColor() { return statusColor; }
    public int getProgressPercent() { return progressPercent; }
    public int getCompletedSteps() { return completedSteps; }
    public int getTotalSteps() { return totalSteps; }
    public List<Step> getSteps() { return steps; }

    public static class Step {
        private String id;
        private String title;
        private String type;
        private String status; // completed, current, pending, rejected
        private String startDate;
        private String endDate;
        private String participant;
        private String assignee;
        private String decision;
        private String decisionIcon;
        private String decisionClass;
        private String comment;
        private String duration;
        private String waitingTime;
        private String dueDate;
        private boolean isOverdue;

        // Getters and setters omitted for brevity
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getParticipant() { return participant; }
        public void setParticipant(String participant) { this.participant = participant; }
        public String getAssignee() { return assignee; }
        public void setAssignee(String assignee) { this.assignee = assignee; }
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getDecisionIcon() { return decisionIcon; }
        public void setDecisionIcon(String decisionIcon) { this.decisionIcon = decisionIcon; }
        public String getDecisionClass() { return decisionClass; }
        public void setDecisionClass(String decisionClass) { this.decisionClass = decisionClass; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getWaitingTime() { return waitingTime; }
        public void setWaitingTime(String waitingTime) { this.waitingTime = waitingTime; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(boolean isOverdue) { this.isOverdue = isOverdue; }
    }
}
