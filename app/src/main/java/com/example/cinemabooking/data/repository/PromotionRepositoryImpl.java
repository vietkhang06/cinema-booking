package com.example.cinemabooking.data.repository;

import androidx.annotation.NonNull;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Promotion;
import com.example.cinemabooking.domain.repository.PromotionRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PromotionRepositoryImpl implements PromotionRepository {

    private final FirebaseFirestore firestore;

    public PromotionRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void createPromotion(Promotion promotion, ResultCallback<Promotion> callback) {
        DocumentReference docRef;
        if (promotion.promoId == null || promotion.promoId.isEmpty()) {
            docRef = firestore.collection(FirestoreCollections.PROMOTIONS).document();
            promotion.promoId = docRef.getId();
        } else {
            docRef = firestore.collection(FirestoreCollections.PROMOTIONS).document(promotion.promoId);
        }

        docRef.set(promotion)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(promotion);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getPromotionById(String promoId, ResultCallback<Promotion> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS).document(promoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Promotion promotion = documentSnapshot.toObject(Promotion.class);
                    if (promotion != null && !promotion.deleted) {
                        if (callback != null) callback.onSuccess(promotion);
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy chương trình khuyến mãi");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllPromotions(ResultCallback<List<Promotion>> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Promotion> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Promotion promo = doc.toObject(Promotion.class);
                        if (promo != null && !promo.deleted) {
                            list.add(promo);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getActivePromotions(long currentTime, ResultCallback<List<Promotion>> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS)
                .whereEqualTo("status", "active")
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Promotion> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Promotion promo = doc.toObject(Promotion.class);
                        if (promo != null && promo.validFrom <= currentTime && promo.validTo >= currentTime) {
                            list.add(promo);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getPromotionByCode(String code, ResultCallback<Promotion> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS)
                .whereEqualTo("code", code)
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Promotion promo = doc.toObject(Promotion.class);
                        if (callback != null) callback.onSuccess(promo);
                    } else {
                        if (callback != null) callback.onError("Mã khuyến mãi không tồn tại");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void updatePromotion(Promotion promotion, ResultCallback<Promotion> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS).document(promotion.promoId)
                .set(promotion)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(promotion);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void softDeletePromotion(String promoId, ResultCallback<Void> callback) {
        firestore.collection(FirestoreCollections.PROMOTIONS).document(promoId)
                .update("deleted", true)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void increaseUsageCount(String promoId, ResultCallback<Promotion> callback) {
        final DocumentReference docRef = firestore.collection(FirestoreCollections.PROMOTIONS).document(promoId);
        firestore.runTransaction(new Transaction.Function<Promotion>() {
            @Override
            public Promotion apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(docRef);
                Promotion promo = snapshot.toObject(Promotion.class);
                if (promo != null) {
                    promo.usedCount = promo.usedCount + 1;
                    transaction.update(docRef, "usedCount", promo.usedCount);
                }
                return promo;
            }
        }).addOnSuccessListener(promo -> {
            if (callback != null) callback.onSuccess(promo);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getMessage());
        });
    }
}
