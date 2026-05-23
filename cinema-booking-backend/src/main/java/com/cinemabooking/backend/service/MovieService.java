package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.MovieDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "movies";

    /**
     * Fetch all movies with lenient filtering (handles missing deleted/isActive fields).
     */
    public List<MovieDTO> getAllMovies(int page, int size) throws ExecutionException, InterruptedException {
        logger.info("Fetching movies from Firestore - page: {}, size: {}", page, size);
        
        try {
            // FIX: Removed strict orderBy("updatedAt") because it excludes documents missing that field.
            // We fetch the latest documents and will sort them in Java.
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .limit(100) // Fetch a reasonable batch for lenient filtering
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            logger.info("Retrieved {} documents from Firestore COLLECTION: {}", documents.size(), COLLECTION);

            List<MovieDTO> allMovies = documents.stream()
                    .map(this::mapToDTO)
                    .filter(m -> !Boolean.TRUE.equals(m.getDeleted())) // Keep if deleted is null or false
                    .filter(m -> !Boolean.FALSE.equals(m.getIsActive())) // Keep if isActive is null or true
                    .sorted((m1, m2) -> {
                        // Sort by updatedAt descending in memory, handling nulls
                        long t1 = m1.getUpdatedAt() != null ? m1.getUpdatedAt() : 0L;
                        long t2 = m2.getUpdatedAt() != null ? m2.getUpdatedAt() : 0L;
                        return Long.compare(t2, t1);
                    })
                    .collect(Collectors.toList());

            logger.info("Movies available after lenient filtering and sorting: {}", allMovies.size());

            // Simple pagination
            int start = page * size;
            if (start >= allMovies.size()) {
                logger.debug("Pagination start index {} out of bounds for size {}", start, allMovies.size());
                return new ArrayList<>();
            }
            int end = Math.min(start + size, allMovies.size());
            
            List<MovieDTO> pagedMovies = allMovies.subList(start, end);
            logger.info("Returning {} movies for page {}", pagedMovies.size(), page);
            return pagedMovies;

        } catch (Exception e) {
            logger.error("Error in getAllMovies (Firestore query failed): {}", e.getMessage(), e);
            throw e;
        }
    }


    public MovieDTO getMovieById(String id) throws ExecutionException, InterruptedException {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION).document(id).get().get();
            if (document.exists() && isVisible(document)) {
                return mapToDTO(document);
            }
        } catch (Exception e) {
            logger.error("Error fetching movie by ID {}: {}", id, e.getMessage());
        }
        return null;
    }

    public List<MovieDTO> getMoviesByStatus(String status, int page, int size) throws ExecutionException, InterruptedException {
        logger.info("Fetching movies by status: {}", status);
        List<MovieDTO> allMovies = getAllMovies(0, 1000); // Fetch all for filtering
        
        List<MovieDTO> filtered = allMovies.stream()
                .filter(m -> status.equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());

        logger.info("Movies with status {}: {}", status, filtered.size());

        int start = page * size;
        if (start >= filtered.size()) return new ArrayList<>();
        int end = Math.min(start + size, filtered.size());
        
        return filtered.subList(start, end);
    }

    public List<MovieDTO> searchMovies(String keyword) throws ExecutionException, InterruptedException {
        logger.info("Searching movies with keyword: {}", keyword);
        List<MovieDTO> allMovies = getAllMovies(0, 1000);
        
        String lowerKeyword = keyword.toLowerCase();
        return allMovies.stream()
                .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    private boolean isVisible(DocumentSnapshot doc) {
        Boolean deleted = doc.getBoolean("deleted");
        Boolean isActive = doc.getBoolean("isActive");
        // Lenient: visible if deleted is not true AND isActive is not false
        return !Boolean.TRUE.equals(deleted) && !Boolean.FALSE.equals(isActive);
    }

    private MovieDTO mapToDTO(DocumentSnapshot doc) {
        return MovieDTO.builder()
                .movieId(doc.getId())
                .title(doc.getString("title"))
                .description(doc.getString("description"))
                .language(doc.getString("language"))
                .ageRating(doc.contains("ageRating") ? doc.getString("ageRating") : doc.getString("age"))
                .posterUrl(doc.contains("posterUrl") ? doc.getString("posterUrl") : doc.getString("imageUrl"))
                .trailerUrl(doc.getString("trailerUrl"))
                .ratingAvg(doc.getDouble("ratingAvg") != null ? doc.getDouble("ratingAvg") : doc.getDouble("rating"))
                .ratingCount(doc.getLong("ratingCount") != null ? doc.getLong("ratingCount").intValue() : 0)
                .status(doc.getString("status"))
                .genres((List<String>) doc.get("genres"))
                .durationMinutes(doc.getLong("durationMinutes") != null ? doc.getLong("durationMinutes").intValue() : 
                                (doc.getLong("duration") != null ? doc.getLong("duration").intValue() : 0))
                .createdAt(doc.getLong("createdAt"))
                .updatedAt(doc.getLong("updatedAt"))
                .isActive(doc.getBoolean("isActive") != null ? doc.getBoolean("isActive") : true)
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .build();
    }
}
