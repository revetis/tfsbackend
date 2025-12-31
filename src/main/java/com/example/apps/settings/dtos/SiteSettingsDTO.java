package com.example.apps.settings.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SiteSettingsDTO {
    private Long id;
    private String siteName;
    private String siteDescription;
    private String logoUrl;
    private String faviconUrl;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    private String youtubeUrl;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String googleAnalyticsId;
    private String facebookPixelId;
}
