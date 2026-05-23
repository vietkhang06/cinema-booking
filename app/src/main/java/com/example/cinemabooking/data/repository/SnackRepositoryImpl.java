package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.dto.SnackDTO;
import com.example.cinemabooking.data.mapper.SnackMapper;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Snack;
import com.example.cinemabooking.domain.repository.SnackRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SnackRepositoryImpl implements SnackRepository {

    private final FirebaseFirestore db;

    public SnackRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void getAllSnacks(ResultCallback<List<Snack>> callback) {
        db.collection(FirestoreCollections.SNACKS)
                .whereEqualTo("deleted", false) // Chỉ lấy những món chưa bị xóa
                .whereEqualTo("isAvailable", true) // Chỉ lấy những món đang mở bán
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Snack> snacks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SnackDTO dto = document.toObject(SnackDTO.class);
                            // Ghi đè ID của document vào trường snackId để đảm bảo tính đồng nhất
                            dto.snackId = document.getId();
                            snacks.add(SnackMapper.toDomain(dto));
                        }
                        callback.onSuccess(snacks);
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Lỗi tải dữ liệu Snack");
                    }
                });
    }

    // Các hàm dưới đây để trống tạm thời (sẽ dùng cho màn hình Admin của Staff sau này)
    @Override
    public void createSnack(Snack snack, ResultCallback<Snack> callback) {}

    @Override
    public void getSnackById(String snackId, ResultCallback<Snack> callback) {}

    @Override
    public void getSnacksByCategoryId(String categoryId, ResultCallback<List<Snack>> callback) {}

    @Override
    public void updateSnack(Snack snack, ResultCallback<Snack> callback) {}

    @Override
    public void setAvailability(String snackId, boolean available, ResultCallback<Snack> callback) {}

    @Override
    public void softDeleteSnack(String snackId, ResultCallback<Void> callback) {}
}