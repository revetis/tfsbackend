package com.example.apps.settings.services.impl;

import com.example.apps.settings.dtos.*;
import com.example.apps.settings.entities.*;
import com.example.apps.settings.repositories.*;
import com.example.apps.settings.services.ISettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService implements ISettingsService {

    private final SiteSettingsRepository siteSettingsRepository;
    private final SliderRepository sliderRepository;
    private final FAQRepository faqRepository;
    private final PageRepository pageRepository;

    // ==================== Site Settings ====================

    @Override
    @Transactional
    public SiteSettingsDTO getSiteSettings() {
        SiteSettings settings = siteSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    SiteSettings newSettings = SiteSettings.builder()
                            .siteName("TFS E-Commerce")
                            .build();
                    return siteSettingsRepository.save(newSettings);
                });
        return convertToDTO(settings);
    }

    @Override
    @Transactional
    public SiteSettingsDTO updateSiteSettings(SiteSettingsDTO dto) {
        SiteSettings settings = siteSettingsRepository.findFirstByOrderByIdAsc()
                .orElse(new SiteSettings());

        BeanUtils.copyProperties(dto, settings, "id", "createdAt", "updatedAt");
        SiteSettings saved = siteSettingsRepository.save(settings);
        log.info("Site settings updated");
        return convertToDTO(saved);
    }

    // ==================== Sliders ====================

    @Override
    public List<SliderDTO> getAllSliders() {
        return sliderRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SliderDTO> getActiveSliders() {
        LocalDateTime now = LocalDateTime.now();
        return sliderRepository.findByActiveOrderByDisplayOrderAsc(true)
                .stream()
                .filter(slider -> {
                    if (slider.getStartDate() != null && slider.getStartDate().isAfter(now)) {
                        return false;
                    }
                    if (slider.getEndDate() != null && slider.getEndDate().isBefore(now)) {
                        return false;
                    }
                    return true;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SliderDTO getSliderById(Long id) {
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found with id: " + id));
        return convertToDTO(slider);
    }

    @Override
    @Transactional
    public SliderDTO createSlider(SliderDTO dto) {
        Slider slider = new Slider();
        BeanUtils.copyProperties(dto, slider, "id", "createdAt", "updatedAt");

        if (slider.getDisplayOrder() == null) {
            slider.setDisplayOrder(0);
        }
        if (slider.getActive() == null) {
            slider.setActive(true);
        }

        Slider saved = sliderRepository.save(slider);
        log.info("Slider created with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public SliderDTO updateSlider(Long id, SliderDTO dto) {
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found with id: " + id));

        BeanUtils.copyProperties(dto, slider, "id", "createdAt", "updatedAt");
        Slider saved = sliderRepository.save(slider);
        log.info("Slider updated with id: {}", id);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSlider(Long id) {
        sliderRepository.deleteById(id);
        log.info("Slider deleted with id: {}", id);
    }

    @Override
    @Transactional
    public void updateSliderOrder(Long id, Integer newOrder) {
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found with id: " + id));
        slider.setDisplayOrder(newOrder);
        sliderRepository.save(slider);
        log.info("Slider order updated for id: {}", id);
    }

    // ==================== FAQs ====================

    @Override
    public List<FAQDTO> getAllFAQs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FAQDTO> getActiveFAQs() {
        return faqRepository.findByActiveOrderByDisplayOrderAsc(true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FAQDTO> getFAQsByCategory(String category) {
        return faqRepository.findByCategoryAndActiveOrderByDisplayOrderAsc(category, true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FAQDTO getFAQById(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        return convertToDTO(faq);
    }

    @Override
    @Transactional
    public FAQDTO createFAQ(FAQDTO dto) {
        FAQ faq = new FAQ();
        BeanUtils.copyProperties(dto, faq, "id", "createdAt", "updatedAt");

        if (faq.getDisplayOrder() == null) {
            faq.setDisplayOrder(0);
        }
        if (faq.getActive() == null) {
            faq.setActive(true);
        }

        FAQ saved = faqRepository.save(faq);
        log.info("FAQ created with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public FAQDTO updateFAQ(Long id, FAQDTO dto) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));

        BeanUtils.copyProperties(dto, faq, "id", "createdAt", "updatedAt");
        FAQ saved = faqRepository.save(faq);
        log.info("FAQ updated with id: {}", id);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteFAQ(Long id) {
        faqRepository.deleteById(id);
        log.info("FAQ deleted with id: {}", id);
    }

    // ==================== Pages ====================

    @Override
    public List<PageDTO> getAllPages() {
        return pageRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PageDTO> getActivePages() {
        return pageRepository.findByActiveOrderByDisplayOrderAsc(true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PageDTO> getFooterPages() {
        return pageRepository.findByShowInFooterTrueAndActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageDTO getPageById(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found with id: " + id));
        return convertToDTO(page);
    }

    @Override
    public PageDTO getPageBySlug(String slug) {
        Page page = pageRepository.findBySlugAndActive(slug, true)
                .orElseThrow(() -> new RuntimeException("Page not found with slug: " + slug));
        return convertToDTO(page);
    }

    @Override
    @Transactional
    public PageDTO createPage(PageDTO dto) {
        if (pageRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Page with slug '" + dto.getSlug() + "' already exists");
        }

        Page page = new Page();
        BeanUtils.copyProperties(dto, page, "id", "createdAt", "updatedAt");

        if (page.getActive() == null) {
            page.setActive(true);
        }
        if (page.getShowInFooter() == null) {
            page.setShowInFooter(false);
        }
        if (page.getDisplayOrder() == null) {
            page.setDisplayOrder(0);
        }

        Page saved = pageRepository.save(page);
        log.info("Page created with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public PageDTO updatePage(Long id, PageDTO dto) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found with id: " + id));

        // Check slug uniqueness if changed
        if (!page.getSlug().equals(dto.getSlug()) && pageRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Page with slug '" + dto.getSlug() + "' already exists");
        }

        BeanUtils.copyProperties(dto, page, "id", "createdAt", "updatedAt");
        Page saved = pageRepository.save(page);
        log.info("Page updated with id: {}", id);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deletePage(Long id) {
        pageRepository.deleteById(id);
        log.info("Page deleted with id: {}", id);
    }

    // ==================== Converters ====================

    private SiteSettingsDTO convertToDTO(SiteSettings entity) {
        SiteSettingsDTO dto = new SiteSettingsDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SliderDTO convertToDTO(Slider entity) {
        SliderDTO dto = new SliderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private FAQDTO convertToDTO(FAQ entity) {
        FAQDTO dto = new FAQDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private PageDTO convertToDTO(Page entity) {
        PageDTO dto = new PageDTO();
        BeanUtils.copyProperties(entity, dto);
        // Explicit mapping to ensure boolean fields are carried over
        dto.setActive(entity.getActive());
        dto.setShowInFooter(entity.getShowInFooter());
        return dto;
    }
}
