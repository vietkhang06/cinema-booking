package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.data.remote.api.ShowtimeApiService;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.repository.ShowtimeRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowtimeRepositoryImpl implements ShowtimeRepository {

    private final FirebaseFirestore firestore;
    private final ShowtimeApiService showtimeApi;
    private final boolean useBackendReads;

    public ShowtimeRepositoryImpl() {
        this(false);
    }

    public ShowtimeRepositoryImpl(boolean useBackendReads) {
        this.firestore = FirebaseFirestore.getInstance();
        this.showtimeApi = RetrofitClient.getInstance().create(ShowtimeApiService.class);
        this.useBackendReads = useBackendReads;
    }

    @Override
    public void createShowtime(Showtime showtime, ResultCallback<Showtime> callback) {
        DocumentReference ref = firestore.collection(FirestoreCollections.SHOWTIMES).document();
        showtime.showtimeId = ref.getId();
        showtime.createdAt = System.currentTimeMillis();
        showtime.updatedAt = System.currentTimeMillis();
        showtime.deleted = false;

        ref.set(showtime)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(showtime);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getShowtimeById(String showtimeId, ResultCallback<Showtime> callback) {
        if (useBackendReads) {
            enqueueSingle(showtimeApi.getShowtimeById(showtimeId), callback, "Unable to load showtime.");
            return;
        }

        firestore.collection(FirestoreCollections.SHOWTIMES)
                .document(showtimeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        if (showtime != null) {
                            showtime.showtimeId = doc.getId();
                            if (callback != null) callback.onSuccess(showtime);
                        } else {
                            if (callback != null) callback.onError("Lỗi phân tích suất chiếu.");
                        }
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy suất chiếu.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllShowtimes(ResultCallback<List<Showtime>> callback) {
        if (useBackendReads) {
            enqueueList(showtimeApi.getAllShowtimes(), callback, "Unable to load showtimes.");
            return;
        }

        firestore.collection(FirestoreCollections.SHOWTIMES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> showtimes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        if (showtime != null) {
                            showtime.showtimeId = doc.getId();
                            if (!showtime.deleted) {
                                showtimes.add(showtime);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(showtimes);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getShowtimesByMovieId(String movieId, ResultCallback<List<Showtime>> callback) {
        if (useBackendReads) {
            // Gọi REST API trước, nếu trả empty thì fallback sang Firestore
            showtimeApi.getShowtimesByMovieId(movieId).enqueue(new Callback<ApiResponse<List<Showtime>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Showtime> data = response.body().getData();
                        if (data != null && !data.isEmpty()) {
                            if (callback != null) callback.onSuccess(data);
                            return;
                        }
                        // API trả empty list — fallback Firestore để tránh màn hình trống
                        android.util.Log.w("SHOWTIME_REPO", "API returned empty showtimes for movieId=" + movieId + ", falling back to Firestore");
                        getShowtimesByMovieIdFromFirestore(movieId, callback);
                    } else {
                        android.util.Log.w("SHOWTIME_REPO", "API error (code=" + response.code() + "), falling back to Firestore");
                        getShowtimesByMovieIdFromFirestore(movieId, callback);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                    android.util.Log.w("SHOWTIME_REPO", "API network error, falling back to Firestore: " + t.getMessage());
                    getShowtimesByMovieIdFromFirestore(movieId, callback);
                }
            });
            return;
        }

        getShowtimesByMovieIdFromFirestore(movieId, callback);
    }

    private void getShowtimesByMovieIdFromFirestore(String movieId, ResultCallback<List<Showtime>> callback) {
        firestore.collection(FirestoreCollections.SHOWTIMES)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        if (showtime != null) {
                            showtime.showtimeId = doc.getId();
                            if (!showtime.deleted) {
                                list.add(showtime);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getShowtimesByCinemaId(String cinemaId, ResultCallback<List<Showtime>> callback) {
        if (useBackendReads) {
            enqueueList(showtimeApi.getShowtimesByCinemaId(cinemaId), callback, "Unable to load cinema showtimes.");
            return;
        }

        firestore.collection(FirestoreCollections.SHOWTIMES)
                .whereEqualTo("cinemaId", cinemaId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        if (showtime != null) {
                            showtime.showtimeId = doc.getId();
                            if (!showtime.deleted) {
                                list.add(showtime);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getShowtimesByDateRange(long startAt, long endAt, ResultCallback<List<Showtime>> callback) {
        if (useBackendReads) {
            getAllShowtimes(new ResultCallback<List<Showtime>>() {
                @Override
                public void onSuccess(List<Showtime> showtimes) {
                    List<Showtime> filtered = new ArrayList<>();
                    if (showtimes != null) {
                        for (Showtime showtime : showtimes) {
                            if (showtime.startAt >= startAt && showtime.startAt <= endAt) {
                                filtered.add(showtime);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(filtered);
                }

                @Override
                public void onError(String message) {
                    if (callback != null) callback.onError(message);
                }
            });
            return;
        }

        firestore.collection(FirestoreCollections.SHOWTIMES)
                .whereGreaterThanOrEqualTo("startAt", startAt)
                .whereLessThanOrEqualTo("startAt", endAt)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        if (showtime != null) {
                            showtime.showtimeId = doc.getId();
                            if (!showtime.deleted) {
                                list.add(showtime);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateShowtime(Showtime showtime, ResultCallback<Showtime> callback) {
        showtime.updatedAt = System.currentTimeMillis();
        firestore.collection(FirestoreCollections.SHOWTIMES)
                .document(showtime.showtimeId)
                .set(showtime)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(showtime);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void changeShowtimeStatus(String showtimeId, String status, ResultCallback<Showtime> callback) {
        getShowtimeById(showtimeId, new ResultCallback<Showtime>() {
            @Override
            public void onSuccess(Showtime showtime) {
                showtime.status = status;
                updateShowtime(showtime, callback);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onError(message);
            }
        });
    }

    @Override
    public void softDeleteShowtime(String showtimeId, ResultCallback<Void> callback) {
        firestore.collection(FirestoreCollections.SHOWTIMES)
                .document(showtimeId)
                .update("deleted", true, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    private void enqueueList(Call<ApiResponse<List<Showtime>>> call, ResultCallback<List<Showtime>> callback, String fallbackMessage) {
        call.enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Showtime> data = response.body().getData();
                    if (callback != null) callback.onSuccess(data != null ? data : new ArrayList<>());
                    return;
                }

                String message = response.body() != null && response.body().getMessage() != null
                        ? response.body().getMessage()
                        : fallbackMessage;
                if (callback != null) callback.onError(message);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                if (callback != null) callback.onError(fallbackMessage);
            }
        });
    }

    private void enqueueSingle(Call<ApiResponse<Showtime>> call, ResultCallback<Showtime> callback, String fallbackMessage) {
        call.enqueue(new Callback<ApiResponse<Showtime>>() {
            @Override
            public void onResponse(Call<ApiResponse<Showtime>> call, Response<ApiResponse<Showtime>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (callback != null) callback.onSuccess(response.body().getData());
                    return;
                }

                String message = response.body() != null && response.body().getMessage() != null
                        ? response.body().getMessage()
                        : fallbackMessage;
                if (callback != null) callback.onError(message);
            }

            @Override
            public void onFailure(Call<ApiResponse<Showtime>> call, Throwable t) {
                if (callback != null) callback.onError(fallbackMessage);
            }
        });
    }
}
