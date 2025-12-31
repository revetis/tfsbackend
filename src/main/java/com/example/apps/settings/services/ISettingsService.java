package com.example.apps.settings.services;

import com.example.apps.settings.dtos.*;

import java.util.List;

public interface ISettingsService {
    // Site Settings
    SiteSettingsDTO getSiteSettings();

    SiteSettingsDTO updateSiteSettings(SiteSettingsDTO dto);

    // Sliders
    List<SliderDTO> getAllSliders();

    List<SliderDTO> getActiveSliders();

    SliderDTO getSliderById(Long id);

    SliderDTO createSlider(SliderDTO dto);

    SliderDTO updateSlider(Long id, SliderDTO dto);

    void deleteSlider(Long id);

    void updateSliderOrder(Long id, Integer newOrder);

    // FAQs
    List<FAQDTO> getAllFAQs();

    List<FAQDTO> getActiveFAQs();

    List<FAQDTO> getFAQsByCategory(String category);

    FAQDTO getFAQById(Long id);

    FAQDTO createFAQ(FAQDTO dto);

    FAQDTO updateFAQ(Long id, FAQDTO dto);

    void deleteFAQ(Long id);

    // Pages
    List<PageDTO> getAllPages();

    List<PageDTO> getActivePages();

    List<PageDTO> getFooterPages();

    PageDTO getPageById(Long id);

    PageDTO getPageBySlug(String slug);

    PageDTO createPage(PageDTO dto);

    PageDTO updatePage(Long id, PageDTO dto);

    void deletePage(Long id);
}
