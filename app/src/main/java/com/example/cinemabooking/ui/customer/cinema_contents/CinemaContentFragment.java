package com.example.cinemabooking.ui.customer.cinema_contents;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.CinemaContentRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;
import com.example.cinemabooking.ui.customer.cinema_contents.adapter.CinemaFeedAdapter;
import com.example.cinemabooking.ui.customer.cinema_contents.mapper.CinemaFeedMapper;
import com.example.cinemabooking.ui.customer.cinema_contents.model.CinemaFeedItem;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class CinemaContentFragment extends Fragment implements CinemaFeedAdapter.OnItemClickListener {

    private enum Tab {
        COMMENT, NEWS, PERSON
    }

    private TextView tvTitle, tvSubtitle, tvEmpty;
    private ImageButton btnSearch;
    private MaterialButton btnComment, btnNews, btnPerson;
    private RecyclerView rvFeed;
    private LinearLayout layoutLoginRequired;
    private MaterialButton btnLoginRequired;

    private CinemaFeedAdapter adapter;
    private final List<CinemaContent> allContents = new ArrayList<>();
    private final CinemaContentRepositoryImpl repository = new CinemaContentRepositoryImpl();
    private Tab currentTab = Tab.COMMENT;

    public CinemaContentFragment() {
        super(R.layout.fragment_cinema_content);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        bindActions();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginState();
    }

    private void checkLoginState() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (isLoggedIn) {
            if (layoutLoginRequired != null) layoutLoginRequired.setVisibility(View.GONE);
            rvFeed.setVisibility(View.VISIBLE);
            if (allContents.isEmpty()) {
                loadData();
            }
        } else {
            if (layoutLoginRequired != null) layoutLoginRequired.setVisibility(View.VISIBLE);
            rvFeed.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnComment = view.findViewById(R.id.btnComment);
        btnNews = view.findViewById(R.id.btnNews);
        btnPerson = view.findViewById(R.id.btnPerson);
        rvFeed = view.findViewById(R.id.rvFeed);
        layoutLoginRequired = view.findViewById(R.id.layoutLoginRequired);
        btnLoginRequired = view.findViewById(R.id.btnLoginRequired);
        
        if (btnLoginRequired != null) {
            btnLoginRequired.setOnClickListener(v -> {
                if (getActivity() != null) {
                    AppNavigator.goToLoginForBooking(getActivity());
                }
            });
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null) return;
        adapter = new CinemaFeedAdapter(this);
        rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFeed.setAdapter(adapter);
    }

    private void bindActions() {
        btnSearch.setOnClickListener(v -> {
                if (getContext() != null) {
                    startActivity(new Intent(getContext(), CinemaSearchActivity.class));
                }
            }
        );

        btnComment.setOnClickListener(v -> {
            currentTab = Tab.COMMENT;
            renderCurrentTab();
        });

        btnNews.setOnClickListener(v -> {
            currentTab = Tab.NEWS;
            renderCurrentTab();
        });

        btnPerson.setOnClickListener(v -> {
            currentTab = Tab.PERSON;
            renderCurrentTab();
        });
    }

    private void loadData() {
        repository.getAll(new ResultCallback<List<CinemaContent>>() {
            @Override
            public void onSuccess(List<CinemaContent> data) {
                if (!isAdded() || getContext() == null) return;
                
                allContents.clear();
                if (data != null) {
                    allContents.addAll(data);
                }
                renderCurrentTab();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getContext() == null) return;
                
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(message == null || message.trim().isEmpty()
                        ? "Không thể tải nội dung điện ảnh"
                        : message);
            }
        });
    }

    private void renderCurrentTab() {
        styleTabs();

        List<CinemaContent> filtered;
        if (currentTab == Tab.COMMENT) {
            tvTitle.setText("Điện ảnh");
            tvSubtitle.setText("Bình luận nổi bật");
            filtered = filterByType(CinemaContentType.COMMENT);
        } else if (currentTab == Tab.NEWS) {
            tvTitle.setText("Điện ảnh");
            tvSubtitle.setText("Tin tức mới nhất");
            filtered = filterByType(CinemaContentType.NEWS);
        } else {
            tvTitle.setText("Điện ảnh");
            tvSubtitle.setText("Gương mặt điện ảnh");
            filtered = filterByType(CinemaContentType.PERSON);
        }

        List<CinemaFeedItem> feedItems = CinemaFeedMapper.toFeedItems(filtered);
        adapter.submitList(feedItems);

        if (feedItems.isEmpty()) {
            tvEmpty.setText("Chưa có nội dung phù hợp");
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.GONE);
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private List<CinemaContent> filterByType(CinemaContentType type) {
        List<CinemaContent> result = new ArrayList<>();
        for (CinemaContent item : allContents) {
            if (item != null && item.type == type) {
                result.add(item);
            }
        }
        return result;
    }

    private void styleTabs() {
        styleTab(btnComment, currentTab == Tab.COMMENT);
        styleTab(btnNews, currentTab == Tab.NEWS);
        styleTab(btnPerson, currentTab == Tab.PERSON);
    }

    private void styleTab(MaterialButton button, boolean selected) {
        if (getContext() == null) return;
        
        if (selected) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B141E")));
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }
    }

    @Override
    public void onItemClick(CinemaFeedItem item) {
        if (getContext() == null) return;
        
        Intent intent = new Intent(getContext(), CinemaContentDetailActivity.class);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_ID, item.id);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_TITLE, item.title);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_TAG, item.tag);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_EXCERPT, item.excerpt);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_CONTENT, item.content);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_META, item.meta);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_AUTHOR, item.author);
        intent.putExtra(CinemaContentDetailActivity.EXTRA_IMAGE_URL, item.imageUrl);
        startActivity(intent);
    }
}
