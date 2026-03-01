package com.demo.workflow.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sling Model for Comment Thread Component
 *
 * Manages threaded comments for workflow review, supporting replies,
 * mentions, attachments, and resolution tracking.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                 COMMENT THREAD MODEL                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   Comment Structure:                                            │
 * │   ┌─────────────────────────────────────────────────────────┐  │
 * │   │  Comment                                                │  │
 * │   │  ├── id: unique identifier                              │  │
 * │   │  ├── parentId: for replies (null for root)              │  │
 * │   │  ├── author: user info                                  │  │
 * │   │  ├── content: comment text (supports @mentions)         │  │
 * │   │  ├── attachments: file references                       │  │
 * │   │  ├── resolved: boolean                                  │  │
 * │   │  ├── created: timestamp                                 │  │
 * │   │  └── replies: nested comments                           │  │
 * │   └─────────────────────────────────────────────────────────┘  │
 * │                                                                 │
 * │   Storage: /var/workflow/comments/{workflowId}/{commentId}     │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class CommentThreadModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    @RequestAttribute(name = "workflowId")
    @Default(values = "")
    private String workflowId;

    @RequestAttribute(name = "contentPath")
    @Default(values = "")
    private String contentPath;

    private List<Comment> items;
    private int totalCount;
    private int unresolvedCount;
    private int resolvedCount;
    private int mentionsCount;
    private boolean isEmpty;
    private boolean hasMore;
    private String currentUser;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
    private static final String COMMENTS_BASE_PATH = "/var/workflow/comments";

    @PostConstruct
    protected void init() {
        currentUser = resourceResolver.getUserID();
        items = new ArrayList<>();

        loadComments();
        calculateCounts();
    }

    private void loadComments() {
        // In production, load from /var/workflow/comments/{workflowId}
        String commentsPath = COMMENTS_BASE_PATH + "/" + workflowId;
        Resource commentsResource = resourceResolver.getResource(commentsPath);

        if (commentsResource != null) {
            for (Resource commentResource : commentsResource.getChildren()) {
                Comment comment = buildComment(commentResource);
                if (comment != null && comment.getParentId() == null) {
                    // Load replies
                    loadReplies(comment, commentsResource);
                    items.add(comment);
                }
            }
        }

        // Sort by date (newest first by default)
        items.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));

        isEmpty = items.isEmpty();
        hasMore = items.size() > 20; // Pagination threshold
    }

    private Comment buildComment(Resource commentResource) {
        Comment comment = new Comment();
        comment.setId(commentResource.getName());
        comment.setPath(commentResource.getPath());

        org.apache.sling.api.resource.ValueMap props = commentResource.getValueMap();
        comment.setParentId(props.get("parentId", String.class));
        comment.setContent(props.get("content", ""));
        comment.setAuthorId(props.get("authorId", ""));
        comment.setAuthorName(props.get("authorName", "Unknown"));
        comment.setAuthorRole(props.get("authorRole", "User"));
        comment.setAuthorAvatar("/libs/granite/security/content/userProperties.html/" + comment.getAuthorId() + "/avatar");
        comment.setResolved(props.get("resolved", false));
        comment.setEdited(props.get("edited", false));

        Calendar created = props.get("created", Calendar.class);
        if (created != null) {
            comment.setCreated(created.getTime());
            comment.setFormattedDate(DATE_FORMAT.format(created.getTime()));
        }

        // Check edit/resolve permissions
        comment.setCanEdit(comment.getAuthorId().equals(currentUser));
        comment.setCanResolve(true); // In production, check against approver group

        // Load attachments
        Resource attachmentsNode = commentResource.getChild("attachments");
        if (attachmentsNode != null) {
            List<Attachment> attachments = new ArrayList<>();
            for (Resource attachmentResource : attachmentsNode.getChildren()) {
                Attachment attachment = new Attachment();
                attachment.setName(attachmentResource.getName());
                attachment.setUrl(attachmentResource.getValueMap().get("url", String.class));
                attachments.add(attachment);
            }
            comment.setAttachments(attachments);
            comment.setHasAttachments(!attachments.isEmpty());
        }

        return comment;
    }

    private void loadReplies(Comment parent, Resource commentsResource) {
        List<Comment> replies = new ArrayList<>();

        for (Resource commentResource : commentsResource.getChildren()) {
            String parentId = commentResource.getValueMap().get("parentId", String.class);
            if (parent.getId().equals(parentId)) {
                Comment reply = buildComment(commentResource);
                if (reply != null) {
                    loadReplies(reply, commentsResource); // Recursive for nested replies
                    replies.add(reply);
                }
            }
        }

        replies.sort((a, b) -> a.getCreated().compareTo(b.getCreated())); // Oldest first for replies
        parent.setReplies(replies);
        parent.setHasReplies(!replies.isEmpty());
    }

    private void calculateCounts() {
        totalCount = 0;
        unresolvedCount = 0;
        resolvedCount = 0;
        mentionsCount = 0;

        for (Comment comment : items) {
            countComment(comment);
        }
    }

    private void countComment(Comment comment) {
        totalCount++;
        if (comment.isResolved()) {
            resolvedCount++;
        } else {
            unresolvedCount++;
        }

        // Check for mentions of current user
        if (comment.getContent().contains("@" + currentUser)) {
            mentionsCount++;
        }

        // Count replies
        if (comment.getHasReplies()) {
            for (Comment reply : comment.getReplies()) {
                countComment(reply);
            }
        }
    }

    // Getters
    public String getWorkflowId() { return workflowId; }
    public List<Comment> getItems() { return items; }
    public int getTotalCount() { return totalCount; }
    public int getUnresolvedCount() { return unresolvedCount; }
    public int getResolvedCount() { return resolvedCount; }
    public int getMentionsCount() { return mentionsCount; }
    public boolean getIsEmpty() { return isEmpty; }
    public boolean getHasMore() { return hasMore; }

    /**
     * Inner class representing a comment
     */
    public static class Comment {
        private String id;
        private String path;
        private String parentId;
        private String content;
        private String authorId;
        private String authorName;
        private String authorRole;
        private String authorAvatar;
        private boolean resolved;
        private boolean edited;
        private Date created;
        private String formattedDate;
        private boolean canEdit;
        private boolean canResolve;
        private boolean hasAttachments;
        private boolean hasReplies;
        private List<Attachment> attachments;
        private List<Comment> replies;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public String getAuthorRole() { return authorRole; }
        public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
        public String getAuthorAvatar() { return authorAvatar; }
        public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
        public boolean isResolved() { return resolved; }
        public void setResolved(boolean resolved) { this.resolved = resolved; }
        public boolean isEdited() { return edited; }
        public void setEdited(boolean edited) { this.edited = edited; }
        public Date getCreated() { return created; }
        public void setCreated(Date created) { this.created = created; }
        public String getFormattedDate() { return formattedDate; }
        public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
        public boolean getCanEdit() { return canEdit; }
        public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }
        public boolean getCanResolve() { return canResolve; }
        public void setCanResolve(boolean canResolve) { this.canResolve = canResolve; }
        public boolean getHasAttachments() { return hasAttachments; }
        public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }
        public boolean getHasReplies() { return hasReplies; }
        public void setHasReplies(boolean hasReplies) { this.hasReplies = hasReplies; }
        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
        public List<Comment> getReplies() { return replies; }
        public void setReplies(List<Comment> replies) { this.replies = replies; }
    }

    /**
     * Inner class representing an attachment
     */
    public static class Attachment {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
