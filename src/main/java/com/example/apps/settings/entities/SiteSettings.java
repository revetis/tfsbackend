package com.example.apps.settings.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "site_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettings extends BaseEntity {

    @Column(name = "site_name", nullable = false)
    private String siteName;

    @Column(name = "site_description", length = 500)
    private String siteDescription;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(name = "google_analytics_id")
    private String googleAnalyticsId;

    @Column(name = "facebook_pixel_id")
    private String facebookPixelId;

    // Announcement Bar Settings
    @Column(name = "announcement_enabled")
    private Boolean announcementEnabled;

    @Column(name = "announcement_text")
    private String announcementText;

    @Column(name = "announcement_bg_color")
    private String announcementBgColor;

    @Column(name = "announcement_text_color")
    private String announcementTextColor;
}
