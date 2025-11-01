package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class News_Model {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("news")
    @Expose
    private List<News> news = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }

    public class News {

        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        
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

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
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
    }
}
