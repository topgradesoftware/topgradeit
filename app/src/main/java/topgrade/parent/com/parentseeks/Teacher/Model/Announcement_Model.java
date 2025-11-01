package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class Announcement_Model {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("announcements")
    @Expose
    private List<Announcement> announcements = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }

    public static class Announcement {

        @SerializedName("unique_id")
        @Expose
        private String uniqueId;

        @SerializedName("type")
        @Expose
        private String type; // "news" or "event"

        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("description")
        @Expose
        private String description;

        @SerializedName("publish_date")
        @Expose
        private String publishDate;

        @SerializedName("author")
        @Expose
        private String author;

        @SerializedName("category")
        @Expose
        private String category;

        @SerializedName("start_date")
        @Expose
        private String startDate;

        @SerializedName("end_date")
        @Expose
        private String endDate;

        // Constructor for News
        public Announcement(String uniqueId, String title, String description, String publishDate, String author, String category) {
            this.uniqueId = uniqueId;
            this.type = "news";
            this.title = title;
            this.description = description;
            this.publishDate = publishDate;
            this.author = author;
            this.category = category;
        }

        // Constructor for Event
        public Announcement(String uniqueId, String title, String startDate, String endDate) {
            this.uniqueId = uniqueId;
            this.type = "event";
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Default constructor
        public Announcement() {}

        // Getters and Setters
        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPublishDate() {
            return publishDate;
        }

        public void setPublishDate(String publishDate) {
            this.publishDate = publishDate;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public boolean isNews() {
            return "news".equals(type);
        }

        public boolean isEvent() {
            return "event".equals(type);
        }
    }
}
