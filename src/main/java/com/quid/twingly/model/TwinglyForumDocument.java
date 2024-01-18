package com.quid.twingly.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TwinglyForumDocument implements Serializable {

	@JsonProperty("post_id")
	private String postId;

	@JsonProperty("post_url")
	private String postUrl;

	@JsonProperty("post_url_with_anchor")
	private String postUrlWithAnchor;

	@JsonProperty("post_title")
	private String postTitle;

	@JsonProperty("post_text")
	private String postText;

	@JsonProperty("post_language")
	private String postLanguage;

	@JsonProperty("post_published_at")
	private String postPublishedAt;

	@JsonProperty("post_inserted_at")
	private String postInsertedAt;

	@JsonProperty("post_updated_at")
	private String postUpdatedAt;

	@JsonProperty("post_crawled_at")
	private String postCrawledAt;

	@JsonProperty("author_id")
	private String authorId;

	@JsonProperty("author_name")
	private String authorName;

	@JsonProperty("author_profile_url")
	private String authorProfileUrl;

	@JsonProperty("author_avatar_url")
	private String authorAvatarUrl;

	@JsonProperty("author_signature")
	private String authorSignature;

	@JsonProperty("author_location")
	private String authorLocation;

	@JsonProperty("thread_id")
	private String threadId;

	@JsonProperty("thread_title")
	private String threadTitle;

	@JsonProperty("forum_id")
	private String forumId;

	@JsonProperty("forum_key")
	private String forumKey;

	@JsonProperty("forum_url")
	private String forumUrl;

	@JsonProperty("forum_name")
	private String forumName;

	@JsonProperty("forum_country")
	private String forumCountry;

	@JsonProperty("site_id")
	private String siteId;

	@JsonProperty("site_key")
	private String siteKey;

	@JsonProperty("site_url")
	private String siteUrl;

	@JsonProperty("site_title")
	private String siteTitle;

	@JsonProperty("post_is_thread_start")
	private boolean postIsThreadStart;

	@JsonProperty("number_of_posts_in_thread")
	private int numberOfPostsInThread;

	@JsonProperty("NBreceiveTime")
	private long nbReceiveTime;

	@Override
 	public String toString(){
		return 
			"DocumentsDTO{" + 
			"post_id = '" + postId + '\'' + 
			",post_url = '" + postUrl + '\'' + 
			",post_url_with_anchor = '" + postUrlWithAnchor + '\'' + 
			",post_title = '" + postTitle + '\'' + 
			",post_text = '" + postText + '\'' + 
			",post_language = '" + postLanguage + '\'' + 
			",post_published_at = '" + postPublishedAt + '\'' + 
			",post_inserted_at = '" + postInsertedAt + '\'' + 
			",post_updated_at = '" + postUpdatedAt + '\'' + 
			",post_crawled_at = '" + postCrawledAt + '\'' + 
			",author_id = '" + authorId + '\'' + 
			",author_name = '" + authorName + '\'' + 
			",author_profile_url = '" + authorProfileUrl + '\'' + 
			",author_avatar_url = '" + authorAvatarUrl + '\'' + 
			",author_signature = '" + authorSignature + '\'' + 
			",author_location = '" + authorLocation + '\'' + 
			",thread_id = '" + threadId + '\'' + 
			",thread_title = '" + threadTitle + '\'' + 
			",forum_id = '" + forumId + '\'' + 
			",forum_key = '" + forumKey + '\'' + 
			",forum_url = '" + forumUrl + '\'' + 
			",forum_name = '" + forumName + '\'' + 
			",forum_country = '" + forumCountry + '\'' + 
			",site_id = '" + siteId + '\'' + 
			",site_key = '" + siteKey + '\'' + 
			",site_url = '" + siteUrl + '\'' + 
			",site_title = '" + siteTitle + '\'' + 
			",post_is_thread_start = '" + postIsThreadStart + '\'' + 
			",number_of_posts_in_thread = '" + numberOfPostsInThread + '\'' + 
			"}";
		}
}