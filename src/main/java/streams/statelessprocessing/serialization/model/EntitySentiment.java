package streams.statelessprocessing.serialization.model;

import com.google.gson.annotations.SerializedName;

public class EntitySentiment {
    @SerializedName("CreatedAt")
    private Long createdAt;

    @SerializedName("Id")
    private Long id;

    @SerializedName("Entity")
    private String entity;

    @SerializedName("Text")
    private String text;

    @SerializedName("SentimentScore")
    private Double sentimentScore;

    @SerializedName("SentimentMagnitude")
    private Double sentimentMagnitude;

    @SerializedName("Salience")
    private Double salience;

    public String getEntity() {
        return this.entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Double getSentimentScore() {
        return this.sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public Double getSentimentMagnitude() {
        return this.sentimentMagnitude;
    }

    public void setSentimentMagnitude(Double sentimentMagnitude) {
        this.sentimentMagnitude = sentimentMagnitude;
    }

    public Double getSalience() {
        return this.salience;
    }

    public void setSalience(Double salience) {
        this.salience = salience;
    }



    public Long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}