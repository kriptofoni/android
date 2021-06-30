package arsi.dev.kriptofoni.Models;

import java.time.LocalDateTime;

public class NewsModel {

    String title, thumbnail, source, link;
    LocalDateTime date;

    public NewsModel(String title, String thumbnail, String source, String link, LocalDateTime date) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.source = source;
        this.link = link;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
