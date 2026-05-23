package com.example.cinemabooking.data.remote.datasource;

import android.util.Log;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieRemoteDataSource {
    private static final String TAG = "MovieRemoteDataSource";
    private static final String COLLECTION_MOVIES = "movies";

    private final FirebaseFirestore firestore;
    private final com.example.cinemabooking.data.remote.api.MovieApiService movieApi;

    public MovieRemoteDataSource() {
        this(FirebaseFirestore.getInstance(), com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabooking.data.remote.api.MovieApiService.class));
    }

    public MovieRemoteDataSource(FirebaseFirestore firestore, com.example.cinemabooking.data.remote.api.MovieApiService movieApi) {
        this.firestore = firestore;
        this.movieApi = movieApi;
    }

    public void createMovie(Movie movie, ResultCallback<Movie> callback) {
        String tempMovieId = readString(movie, "movieId", "id");

        if (tempMovieId == null || tempMovieId.trim().isEmpty()) {
            tempMovieId = firestore.collection(COLLECTION_MOVIES).document().getId();
        }

        final String movieId = tempMovieId; // ✅ FIX CHÍNH

        Map<String, Object> movieData = movieToMap(movie);
        movieData.put("movieId", movieId);
        movieData.put("deleted", false);
        movieData.put("updatedAt", System.currentTimeMillis());

        if (!movieData.containsKey("status") || movieData.get("status") == null) {
            movieData.put("status", "COMING_SOON");
        }

        if (!movieData.containsKey("isActive")) {
            movieData.put("isActive", true);
        }

        if (!movieData.containsKey("createdAt")) {
            movieData.put("createdAt", System.currentTimeMillis());
        }

        firestore.collection(COLLECTION_MOVIES)
                .document(movieId)
                .set(movieData)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(mapDocumentToMovie(movieId, movieData));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tạo phim"));
                    }
                });
    }

    public void getMovieById(String movieId, ResultCallback<Movie> callback) {
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) callback.onError("movieId không hợp lệ");
            return;
        }

        Log.d(TAG, "Requesting movie by ID: " + movieId);
        movieApi.getMovieById(movieId).enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                Log.d(TAG, "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Movie fetched successfully: " + response.body().getData().title);
                    if (callback != null) callback.onSuccess(response.body().getData());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Không tìm thấy phim (Code: " + response.code() + ")";
                    Log.e(TAG, "API Error: " + msg);
                    if (callback != null) callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) callback.onError("Không thể kết nối Server. Vui lòng kiểm tra mạng.");
            }
        });
    }

    public void getAllMovies(ResultCallback<List<Movie>> callback) {
        Log.d(TAG, "Requesting all movies via API");
        movieApi.getAllMovies(0, 20).enqueue(new Callback<ApiResponse<List<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Movie>>> call, Response<ApiResponse<List<Movie>>> response) {
                Log.d(TAG, "getAllMovies - Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Movie> data = response.body().getData();
                    Log.d(TAG, "getAllMovies - Success. Count: " + (data != null ? data.size() : 0));
                    if (callback != null) callback.onSuccess(data != null ? data : new ArrayList<>());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Lỗi tải phim (Code: " + response.code() + ")";
                    Log.e(TAG, "getAllMovies - API Error: " + msg);
                    if (callback != null) {
                        // FIX: Only trigger onError. ViewModel should handle empty state if needed.
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Movie>>> call, Throwable t) {
                Log.e(TAG, "getAllMovies - Network Failure: " + t.getMessage(), t);
                if (callback != null) {
                    callback.onError("Server hiện không khả dụng. Vui lòng thử lại sau.");
                }
            }
        });
    }

    public void getMoviesByStatus(String status, ResultCallback<List<Movie>> callback) {
        final String normalizedStatus = normalizeStatus(status);
        Log.d(TAG, "Requesting movies by status: " + normalizedStatus);

        movieApi.getMoviesByStatus(normalizedStatus, 0, 20).enqueue(new Callback<ApiResponse<List<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Movie>>> call, Response<ApiResponse<List<Movie>>> response) {
                Log.d(TAG, "getMoviesByStatus - Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Movie> data = response.body().getData();
                    Log.d(TAG, "getMoviesByStatus - Success. Found: " + (data != null ? data.size() : 0));
                    if (callback != null) callback.onSuccess(data != null ? data : new ArrayList<>());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Lỗi tải phim (Code: " + response.code() + ")";
                    Log.e(TAG, "getMoviesByStatus - API Error: " + msg);
                    if (callback != null) {
                        // FIX: Only trigger onError.
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Movie>>> call, Throwable t) {
                Log.e(TAG, "getMoviesByStatus - Network Failure: " + t.getMessage(), t);
                if (callback != null) {
                    callback.onError("Không thể tải phim theo trạng thái.");
                }
            }
        });
    }

    public void searchMovies(String keyword, ResultCallback<List<Movie>> callback) {
        final String safeKeyword = normalizeText(keyword);

        getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (safeKeyword.isEmpty()) {
                    if (callback != null) callback.onSuccess(movies);
                    return;
                }

                List<Movie> filteredMovies = new ArrayList<>();
                for (Movie movie : movies) {
                    String title = normalizeText(readString(movie, "title"));
                    String description = normalizeText(readString(movie, "description"));
                    String language = normalizeText(readString(movie, "language"));
                    String genres = normalizeText(joinGenres(readGenres(readValue(movie, "genres"))));

                    if (title.contains(safeKeyword)
                            || description.contains(safeKeyword)
                            || language.contains(safeKeyword)
                            || genres.contains(safeKeyword)) {
                        filteredMovies.add(movie);
                    }
                }

                if (callback != null) callback.onSuccess(filteredMovies);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    public void updateMovie(Movie movie, ResultCallback<Movie> callback) {
        if (movie == null) {
            if (callback != null) callback.onError("Movie null");
            return;
        }

        String movieId = readString(movie, "movieId", "id");
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) callback.onError("movieId không hợp lệ");
            return;
        }

        Map<String, Object> movieData = movieToMap(movie);
        movieData.put("movieId", movieId);
        movieData.put("updatedAt", System.currentTimeMillis());

        firestore.collection(COLLECTION_MOVIES)
                .document(movieId)
                .set(movieData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(mapDocumentToMovie(movieId, movieData));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể cập nhật phim"));
                    }
                });
    }

    public void softDeleteMovie(String movieId, ResultCallback<Void> callback) {
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) callback.onError("movieId không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", false);
        updates.put("deleted", true);
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(COLLECTION_MOVIES)
                .document(movieId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể xoá mềm phim"));
                    }
                });
    }

    private boolean isVisible(DocumentSnapshot documentSnapshot) {
        Boolean deleted = documentSnapshot.getBoolean("deleted");
        if (deleted != null && deleted) {
            return false;
        }

        Boolean isActive = documentSnapshot.getBoolean("isActive");
        if (isActive != null) {
            return isActive;
        }

        return true;
    }

    private Movie mapSnapshotToMovie(DocumentSnapshot documentSnapshot) {
        return mapDocumentToMovie(documentSnapshot.getId(), documentSnapshot.getData());
    }

    private Movie mapDocumentToMovie(String movieId, Map<String, Object> data) {
        Movie movie = createMovieInstance();
        if (movie == null) {
            return null;
        }

        setValue(movie, "movieId", movieId);
        setValue(movie, "id", movieId);

        if (data == null) {
            return movie;
        }

        setValue(movie, "title", data.get("title"));
        setValue(movie, "description", data.get("description"));
        setValue(movie, "language", data.get("language"));
        setValue(movie, "ageRating", firstNonNull(data.get("ageRating"), data.get("age")));
        setValue(movie, "posterUrl", firstNonNull(data.get("posterUrl"), data.get("imageUrl")));
        setValue(movie, "imageUrl", firstNonNull(data.get("imageUrl"), data.get("posterUrl")));
        setValue(movie, "trailerUrl", data.get("trailerUrl"));
        setValue(movie, "ratingAvg", firstNonNull(data.get("ratingAvg"), data.get("rating")));
        setValue(movie, "ratingCount", data.get("ratingCount"));
        setValue(movie, "status", normalizeStatus(String.valueOf(firstNonNull(data.get("status"), ""))));
        setValue(movie, "genres", readGenres(firstNonNull(data.get("genres"), data.get("genre"))));
        setValue(movie, "genre", firstNonNull(data.get("genre"), joinGenres(readGenres(data.get("genres")))));
        setValue(movie, "durationMinutes", firstNonNull(data.get("durationMinutes"), data.get("duration")));
        setValue(movie, "duration", firstNonNull(data.get("duration"), data.get("durationMinutes")));
        setValue(movie, "createdAt", data.get("createdAt"));
        setValue(movie, "updatedAt", data.get("updatedAt"));
        setValue(movie, "isActive", data.get("isActive"));
        setValue(movie, "deleted", data.get("deleted"));

        return movie;
    }

    private Movie createMovieInstance() {
        try {
            return Movie.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> movieToMap(Movie movie) {
        Map<String, Object> data = new LinkedHashMap<>();

        putIfNotNull(data, "movieId", readValue(movie, "movieId", "id"));
        putIfNotNull(data, "title", readValue(movie, "title"));
        putIfNotNull(data, "description", readValue(movie, "description"));
        putIfNotNull(data, "language", readValue(movie, "language"));

        Object ageRating = firstNonNull(readValue(movie, "ageRating"), readValue(movie, "age"));
        putIfNotNull(data, "ageRating", ageRating);
        putIfNotNull(data, "age", ageRating);

        Object posterUrl = firstNonNull(readValue(movie, "posterUrl"), readValue(movie, "imageUrl"));
        putIfNotNull(data, "posterUrl", posterUrl);
        putIfNotNull(data, "imageUrl", posterUrl);

        putIfNotNull(data, "trailerUrl", readValue(movie, "trailerUrl"));

        Object ratingAvg = firstNonNull(readValue(movie, "ratingAvg"), readValue(movie, "rating"));
        putIfNotNull(data, "ratingAvg", ratingAvg);
        putIfNotNull(data, "rating", ratingAvg);

        putIfNotNull(data, "ratingCount", readValue(movie, "ratingCount"));

        String status = normalizeStatus(readString(movie, "status"));
        if (status != null) {
            data.put("status", status);
        }

        Object durationMinutes = firstNonNull(readValue(movie, "durationMinutes"), readValue(movie, "duration"));
        putIfNotNull(data, "durationMinutes", durationMinutes);
        putIfNotNull(data, "duration", durationMinutes);

        List<String> genres = readGenres(firstNonNull(readValue(movie, "genres"), readValue(movie, "genre")));
        if (!genres.isEmpty()) {
            data.put("genres", genres);
            data.put("genre", genres.get(0));
        }

        putIfNotNull(data, "isActive", readValue(movie, "isActive"));
        putIfNotNull(data, "deleted", readValue(movie, "deleted"));
        putIfPositiveLong(data, "createdAt", readValue(movie, "createdAt"));

        return data;
    }

    private void putIfNotNull(Map<String, Object> data, String key, Object value) {
        if (value != null) {
            data.put(key, value);
        }
    }

    private void putIfPositiveLong(Map<String, Object> data, String key, Object value) {
        if (value instanceof Number && ((Number) value).longValue() > 0L) {
            data.put(key, ((Number) value).longValue());
            return;
        }

        if (value != null) {
            try {
                long parsed = Long.parseLong(String.valueOf(value));
                if (parsed > 0L) {
                    data.put(key, parsed);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private List<String> readGenres(Object value) {
        List<String> genres = new ArrayList<>();
        if (value == null) {
            return genres;
        }

        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item == null) continue;
                String text = String.valueOf(item).trim();
                if (!text.isEmpty()) genres.add(text);
            }
            return genres;
        }

        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return genres;
        }

        String[] parts = raw.split("[,;|]");
        for (String part : parts) {
            String text = part.trim();
            if (!text.isEmpty()) genres.add(text);
        }

        return genres;
    }

    private String joinGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String genre : genres) {
            if (genre == null) continue;
            String text = genre.trim();
            if (text.isEmpty()) continue;
            if (builder.length() > 0) builder.append(", ");
            builder.append(text);
        }
        return builder.toString();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }

        String key = normalizeText(status);
        if (key.isEmpty()) {
            return null;
        }

        if ("dang_chieu".equals(key) || "now_showing".equals(key) || "showing".equals(key)) {
            return "NOW_SHOWING";
        }

        if ("sap_chieu".equals(key) || "coming_soon".equals(key) || "upcoming".equals(key)) {
            return "COMING_SOON";
        }

        if ("ngung_chieu".equals(key) || "ended".equals(key) || "inactive".equals(key) || "off".equals(key)) {
            return "ENDED";
        }

        return key.toUpperCase(Locale.getDefault());
    }

    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        normalized = normalized.toLowerCase(Locale.getDefault()).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return normalized;
    }

    private String messageOrDefault(Exception e, String defaultMessage) {
        if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }
        return defaultMessage;
    }

    private String readString(Object target, String... possibleNames) {
        Object value = readValue(target, possibleNames);
        return value == null ? null : String.valueOf(value);
    }

    private Object readValue(Object target, String... possibleNames) {
        if (target == null || possibleNames == null) {
            return null;
        }

        Class<?> clazz = target.getClass();

        for (String name : possibleNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            String capitalized = capitalize(name);

            try {
                Method getter = clazz.getMethod("get" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Method getter = clazz.getMethod("is" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private void setValue(Object target, String propertyName, Object value) {
        if (target == null || propertyName == null || value == null) {
            return;
        }

        Class<?> clazz = target.getClass();
        String capitalized = capitalize(propertyName);
        String setterName = "set" + capitalized;

        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(setterName) || method.getParameterCount() != 1) {
                continue;
            }

            try {
                Class<?> parameterType = method.getParameterTypes()[0];
                method.invoke(target, convertValue(value, parameterType));
                return;
            } catch (Exception ignored) {
            }
        }

        try {
            Field field = clazz.getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(target, convertValue(value, field.getType()));
        } catch (Exception ignored) {
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == String.class) {
            return String.valueOf(value);
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        }

        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(String.valueOf(value));
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(String.valueOf(value));
        }

        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(String.valueOf(value));
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(String.valueOf(value));
        }

        return value;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }
}