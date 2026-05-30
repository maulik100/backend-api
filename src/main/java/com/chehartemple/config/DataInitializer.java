package com.chehartemple.config;

import com.chehartemple.model.*;
import com.chehartemple.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TempleTimingRepository timingRepository;
    private final AppConfigRepository configRepository;
    private final NewsRepository newsRepository;
    private final GalleryRepository galleryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@chehartemple.com")) {
            userRepository.save(User.builder()
                    .name("Admin").email("admin@chehartemple.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN).emailVerified(true).build());
        }

        if (eventRepository.count() == 0) {
            eventRepository.save(Event.builder().title("Today Special Darshan")
                    .description("Special darshan for devotees")
                    .eventDate(LocalDate.now()).startTime("10:00 AM").endTime("12:00 PM").active(true).build());
            eventRepository.save(Event.builder().title("Navratri Mahotsav")
                    .description("Nine nights of devotion and celebration")
                    .eventDate(LocalDate.now().plusDays(30)).allDayEvent(true).active(true).build());
            eventRepository.save(Event.builder().title("Maha Aarti")
                    .description("Special evening aarti with devotional songs")
                    .eventDate(LocalDate.now().plusDays(7)).startTime("07:00 PM").endTime("08:30 PM").active(true).build());
        }

        if (timingRepository.count() == 0) {
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
            for (String day : days) {
                timingRepository.save(TempleTiming.builder().day(day)
                        .openTime("06:00 AM").closeTime("09:00 PM")
                        .morningAartiTime("07:00 AM").eveningAartiTime("07:00 PM")
                        .specialNote(day.equals("SUNDAY") ? "Special Bhajan: 05:00 PM" : null).build());
            }
        }

        if (configRepository.count() == 0) {
            // Social links & live stream
            configRepository.save(AppConfig.builder().configKey("LIVE_STREAM_URL").configValue("").build());
            configRepository.save(AppConfig.builder().configKey("FACEBOOK_URL").configValue("https://www.facebook.com/Gornokuvo").build());
            configRepository.save(AppConfig.builder().configKey("INSTAGRAM_URL").configValue("https://instagram.com/chehartemple").build());
            configRepository.save(AppConfig.builder().configKey("YOUTUBE_URL").configValue("https://youtube.com/@chehartemple").build());
            // Contact Info
            configRepository.save(AppConfig.builder().configKey("CONTACT_EMAIL").configValue("info@chehartemple.com").build());
            configRepository.save(AppConfig.builder().configKey("CONTACT_PHONE").configValue("+91-XXXXX-XXXXX").build());
            configRepository.save(AppConfig.builder().configKey("TEMPLE_ADDRESS").configValue("Chehar Temple, Gujarat, India").build());
        }

        if (newsRepository.count() == 0) {
            newsRepository.save(News.builder().title("Temple Renovation Complete")
                    .content("The main hall renovation has been completed. New marble flooring and improved lighting.").active(true).build());
            newsRepository.save(News.builder().title("New Parking Facility")
                    .content("A new parking area with 200+ capacity has been opened for devotees.").active(true).build());
            newsRepository.save(News.builder().title("Prasad Timings Changed")
                    .content("Prasad: 11 AM - 1 PM and 5 PM - 7 PM daily.").active(true).build());
        }

        if (galleryRepository.count() == 0) {
            galleryRepository.save(GalleryItem.builder().title("Temple Videos")
                    .url("https://www.facebook.com/Gornokuvo/videos/")
                    .mediaType(GalleryItem.MediaType.VIDEO).source(GalleryItem.MediaSource.FACEBOOK).active(true).build());
            galleryRepository.save(GalleryItem.builder().title("Temple Reels")
                    .url("https://www.facebook.com/Gornokuvo/reels/")
                    .mediaType(GalleryItem.MediaType.VIDEO).source(GalleryItem.MediaSource.FACEBOOK).active(true).build());
            galleryRepository.save(GalleryItem.builder().title("Temple Entrance")
                    .url("https://via.placeholder.com/800x600/FF6F00/FFFFFF?text=Temple+Entrance")
                    .mediaType(GalleryItem.MediaType.IMAGE).source(GalleryItem.MediaSource.INSTAGRAM).active(true).build());
            galleryRepository.save(GalleryItem.builder().title("Diwali Decoration")
                    .url("https://via.placeholder.com/800x600/E91E63/FFFFFF?text=Diwali+Decoration")
                    .mediaType(GalleryItem.MediaType.IMAGE).source(GalleryItem.MediaSource.OTHER).active(true).build());
        }
    }
}
