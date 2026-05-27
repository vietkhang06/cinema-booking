package com.example.cinemabooking.ui.customer.cinema_contents;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaContentRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;
import com.example.cinemabooking.ui.customer.cinema_contents.adapter.CinemaFeedAdapter;
import com.example.cinemabooking.ui.customer.cinema_contents.mapper.CinemaFeedMapper;
import com.example.cinemabooking.ui.customer.cinema_contents.model.CinemaFeedItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CinemaSearchActivity extends BaseActivity implements CinemaFeedAdapter.OnItemClickListener {

    private enum Tab {
        ALL, COMMENT, NEWS, PERSON
    }

    private final CinemaContentRepositoryImpl repository = new CinemaContentRepositoryImpl();
    private final List<CinemaContent> allContents = new ArrayList<>();
    private CinemaFeedAdapter adapter;

    private TextInputEditText edtSearch;
    private ImageButton btnBack;
    private TextView tvResultCount, tvEmpty;
    private MaterialButton btnComment, btnNews, btnPerson;
    private RecyclerView rvResults;

    private Tab currentTab = Tab.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_content_search);

        initViews();
        setupRecyclerView();
        bindActions();
        loadData();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnComment = findViewById(R.id.btnComment);
        btnNews = findViewById(R.id.btnNews);
        btnPerson = findViewById(R.id.btnPerson);
        rvResults = findViewById(R.id.rvResults);
    }

    private void setupRecyclerView() {
        adapter = new CinemaFeedAdapter(this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> finish());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                render();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnComment.setOnClickListener(v -> {
            currentTab = Tab.COMMENT;
            render();
        });

        btnNews.setOnClickListener(v -> {
            currentTab = Tab.NEWS;
            render();
        });

        btnPerson.setOnClickListener(v -> {
            currentTab = Tab.PERSON;
            render();
        });
    }

    private void loadData() {
        repository.getAll(new ResultCallback<List<CinemaContent>>() {
            @Override
            public void onSuccess(List<CinemaContent> data) {
                allContents.clear();
                if (data != null) {
                    allContents.addAll(data);
                }
                render();
            }

            @Override
            public void onError(String message) {
                tvEmpty.setText(message);
                tvEmpty.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    private void render() {
        styleTabs();

        String query = getQuery().toLowerCase(Locale.ROOT);
        List<CinemaContent> filtered = new ArrayList<>();

        for (CinemaContent item : allContents) {
            if (item == null) continue;

            boolean matchesQuery = query.isEmpty()
                    || safe(item.title).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.excerpt).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.meta).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.content).toLowerCase(Locale.ROOT).contains(query)
                    || safe(item.tag).toLowerCase(Locale.ROOT).contains(query);

            boolean matchesTab = currentTab == Tab.ALL
                    || (currentTab == Tab.COMMENT && item.type == CinemaContentType.COMMENT)
                    || (currentTab == Tab.NEWS && item.type == CinemaContentType.NEWS)
                    || (currentTab == Tab.PERSON && item.type == CinemaContentType.PERSON);

            if (matchesQuery && matchesTab) {
                filtered.add(item);
            }
        }

        List<CinemaFeedItem> feedItems = CinemaFeedMapper.toFeedItems(filtered);
        adapter.submitList(feedItems);

        tvResultCount.setText(feedItems.size() + " kết quả");
        tvEmpty.setVisibility(feedItems.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void styleTabs() {
        styleTab(btnComment, currentTab == Tab.COMMENT);
        styleTab(btnNews, currentTab == Tab.NEWS);
        styleTab(btnPerson, currentTab == Tab.PERSON);
    }

    private void styleTab(MaterialButton button, boolean selected) {
        if (selected) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1B141E")));
            button.setTextColor(android.graphics.Color.WHITE);
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            button.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private String getQuery() {
        return edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onItemClick(CinemaFeedItem item) {
        Intent intent = new Intent(this, CinemaContentDetailActivity.class);
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
