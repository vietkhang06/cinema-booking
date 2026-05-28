package com.example.cinemabooking.ui.customer.cinema_contents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;

public class CinemaContentDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "extra_cinema_id";
    public static final String EXTRA_TITLE = "extra_cinema_title";
    public static final String EXTRA_TAG = "extra_cinema_tag";
    public static final String EXTRA_EXCERPT = "extra_cinema_excerpt";
    public static final String EXTRA_CONTENT = "extra_cinema_content";
    public static final String EXTRA_META = "extra_cinema_meta";
    public static final String EXTRA_AUTHOR = "extra_cinema_author";
    public static final String EXTRA_IMAGE_URL = "extra_cinema_image_url";

    private ImageButton btnBack;
    private ImageView ivCover;
    private TextView tvTag, tvTitle, tvMeta, tvAuthor, tvContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_content_detail);

        btnBack = findViewById(R.id.btnBack);
        ivCover = findViewById(R.id.ivCover);
        tvTag = findViewById(R.id.tvTag);
        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvContent = findViewById(R.id.tvContent);

        btnBack.setOnClickListener(v -> finish());

        tvTag.setText(getStringExtra(EXTRA_TAG));
        tvTitle.setText(getStringExtra(EXTRA_TITLE));
        tvMeta.setText(getStringExtra(EXTRA_META));
        tvAuthor.setText(getStringExtra(EXTRA_AUTHOR));
        tvContent.setText(getStringExtra(EXTRA_CONTENT));

        Glide.with(this)
                .load(getStringExtra(EXTRA_IMAGE_URL))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivCover);
    }

    private String getStringExtra(String key) {
        String value = getIntent().getStringExtra(key);
        return value == null ? "" : value;
    }
}
