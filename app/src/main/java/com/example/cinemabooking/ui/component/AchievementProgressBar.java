package com.example.cinemabooking.ui.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.Arrays;
import java.util.List;

/**
 * Custom View that draws a progress bar with milestone dots,
 * matching the achievement bar design:
 *
 *   O-----------O-----------O
 *  0đ      2,000,000đ   4,000,000đ
 *
 * Milestones are at 0%, 50%, and 100%.
 * Progress fill is drawn up to the current progress value.
 */
public class AchievementProgressBar extends View {

    // ── Milestone data class ──────────────────────────────────────────────────

    public static class Milestone {
        /** Where this milestone sits on the bar, 0.0 – 1.0 */
        public final float  fraction;
        /** Label rendered below the dot (e.g. "2,000,000đ") */
        public final String label;
        /** Drawable resource id of the icon rendered above the dot */
        public final int    iconResId;
        /** Optional color to tint the icon */
        public final Integer tintColor;

        public Milestone(float fraction, String label, int iconResId) {
            this(fraction, label, iconResId, null);
        }

        public Milestone(float fraction, String label, int iconResId, Integer tintColor) {
            this.fraction  = Math.max(0f, Math.min(1f, fraction));
            this.label     = label;
            this.iconResId = iconResId;
            this.tintColor = tintColor;
        }
    }

    // ── Dimension constants (dp / sp) ─────────────────────────────────────────
    private static final float ICON_SIZE_DP      = 40f;
    private static final float ICON_STAR_GAP_DP  =  0f;
    private static final float STAR_SIZE_DP      = 12f;
    private static final float STAR_TRACK_GAP_DP =  4f;
    private static final float TRACK_HEIGHT_DP   =  6f;
    private static final float DOT_RADIUS_DP     =  8f;
    private static final float LABEL_GAP_DP      =  6f;
    private static final float TEXT_SIZE_SP      = 12f;
    private static final float DOT_STROKE_DP     =  2f;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COLOR_TRACK      = 0xFFD0D8E8;
    private static final int COLOR_FILL       = 0xFF3A8EF5;
    private static final int COLOR_DOT_BG     = Color.WHITE;
    private static final int COLOR_DOT_BG1     = 0xFF1d34b5;
    private static final int COLOR_DOT_RING   = 0xFF3A8EF5;
    private static final int COLOR_STAR       = 0xFFFFA500;
    private static final int COLOR_LABEL      = 0xFF555555;

    // ── Internal state ────────────────────────────────────────────────────────
    private float           progress  = 0f;
    private List<Milestone> milestones;
    private Bitmap[]        iconBitmaps;

    // ── Paint objects ─────────────────────────────────────────────────────────
    private final Paint trackPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotBgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotBgPaint1   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path starPath     = new Path();

    private float density;
    private float scaledDensity;

    // ── Constructors ──────────────────────────────────────────────────────────
    public AchievementProgressBar(Context context) {
        super(context);
        init(context);
    }

    public AchievementProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AchievementProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density       = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;

        trackPaint.setColor(COLOR_TRACK);
        trackPaint.setStyle(Paint.Style.FILL);

        fillPaint.setColor(COLOR_FILL);
        fillPaint.setStyle(Paint.Style.FILL);

        dotBgPaint.setColor(COLOR_DOT_BG);
        dotBgPaint.setStyle(Paint.Style.FILL);

        dotBgPaint1.setColor(COLOR_DOT_BG1);
        dotBgPaint1.setStyle(Paint.Style.FILL);

        dotRingPaint.setColor(COLOR_DOT_RING);
        dotRingPaint.setStyle(Paint.Style.STROKE);
        dotRingPaint.setStrokeWidth(dp(DOT_STROKE_DP));

        starPaint.setColor(COLOR_STAR);
        starPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(COLOR_LABEL);
        labelPaint.setTextSize(sp(TEXT_SIZE_SP));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setStyle(Paint.Style.FILL);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Supply the milestone definitions. Each {@link Milestone} holds:
     * <ul>
     *   <li>{@code fraction} – position on the bar (0.0 – 1.0)</li>
     *   <li>{@code label}    – text shown below the dot</li>
     *   <li>{@code iconResId}– drawable shown above the dot</li>
     * </ul>
     * Milestones can be in any order; they do NOT need to be sorted.
     */
    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
        loadIconBitmaps();
        requestLayout();
        invalidate();
    }

    /**
     * Set current progress as a fraction between 0.0 and 1.0.
     * The fill and dot highlighting update automatically.
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /**
     * Convenience: set progress from an absolute value and a max value.
     * Example: {@code setProgress(1_400_000L, 4_000_000L)} → 35 %
     */
    public void setProgress(long current, long max) {
        setProgress(max == 0 ? 0f : (float) current / max);
    }

    public float getProgress() { return progress; }

    // ── Measurement ───────────────────────────────────────────────────────────

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float totalH =
                dp(ICON_SIZE_DP)          // icons
                        + dp(ICON_STAR_GAP_DP)   // gap
                        + dp(STAR_SIZE_DP)       // stars
                        + dp(STAR_TRACK_GAP_DP)  // gap
                        + dp(DOT_RADIUS_DP)      // top half of dot / track
                        + dp(DOT_RADIUS_DP)      // bottom half
                        + dp(LABEL_GAP_DP)       // gap
                        + sp(TEXT_SIZE_SP);      // label text

        int desiredH = (int) Math.ceil(totalH) + getPaddingTop() + getPaddingBottom();
        int h = resolveSize(desiredH, heightMeasureSpec);
        int w = resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec);
        setMeasuredDimension(w, h);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (milestones == null || milestones.isEmpty()) return;

        float availW = getWidth()  - getPaddingLeft() - getPaddingRight();
        float ox     = getPaddingLeft();
        float oy     = getPaddingTop();

        float iconSize  = dp(ICON_SIZE_DP);
        float starSize  = dp(STAR_SIZE_DP);
        float dotR      = dp(DOT_RADIUS_DP);
        float trackH    = dp(TRACK_HEIGHT_DP);

        // ── Vertical anchors ──────────────────────────────────────────────────
        float iconTop  = oy;
        float starCy   = iconTop + iconSize + dp(ICON_STAR_GAP_DP) + starSize / 2f;
        float trackCy  = starCy  + starSize / 2f + dp(STAR_TRACK_GAP_DP) + dotR;
        float labelY   = trackCy + dotR + dp(LABEL_GAP_DP) + sp(TEXT_SIZE_SP);

        // ── Horizontal track extents ──────────────────────────────────────────
        // Inset by dotR so the edge dots are fully visible
        float trackLeft  = ox + dotR;
        float trackRight = ox + availW - dotR;
        float trackWidth = trackRight - trackLeft;

        // 1. Track background
        RectF bgRect = new RectF(trackLeft, trackCy - trackH / 2f,
                trackRight, trackCy + trackH / 2f);
        canvas.drawRoundRect(bgRect, trackH / 2f, trackH / 2f, trackPaint);

        // 2. Progress fill
        float fillEnd = trackLeft + trackWidth * progress;
        RectF fillRect = new RectF(trackLeft, trackCy - trackH / 2f,
                fillEnd,   trackCy + trackH / 2f);
        canvas.drawRoundRect(fillRect, trackH / 2f, trackH / 2f, fillPaint);

        // 3. Per-milestone elements — all horizontally aligned to dotCx
        for (int i = 0; i < milestones.size(); i++) {
            Milestone m   = milestones.get(i);
            float dotCx   = trackLeft + trackWidth * m.fraction;

            // Icon — centered on dotCx, top-aligned to iconTop
            if (iconBitmaps != null && i < iconBitmaps.length && iconBitmaps[i] != null) {
                float bx  = dotCx - iconSize / 2f;
                RectF dst = new RectF(bx, iconTop, bx + iconSize, iconTop + iconSize);
                canvas.drawBitmap(iconBitmaps[i], null, dst, null);
            }

//            // Star — centered on dotCx, vertically at starCy
//            drawStar(canvas, dotCx, starCy, starSize / 2f, starPaint);

            // Milestone dot
            canvas.drawCircle(dotCx, trackCy, dotR, m.fraction > this.progress ? dotBgPaint : dotBgPaint1);
            canvas.drawCircle(dotCx, trackCy, dotR - dp(1f), dotRingPaint);

            // Label — clamped so it doesn't bleed outside the view
            if (m.label != null && !m.label.isEmpty()) {
                float textHalfW = labelPaint.measureText(m.label) / 2f;
                float clampedCx = Math.max(ox + textHalfW,
                        Math.min(ox + availW - textHalfW, dotCx));
                canvas.drawText(m.label, clampedCx, labelY, labelPaint);
            }
        }
    }

//     ── Icon loading ──────────────────────────────────────────────────────────

//    private void loadIconBitmaps() {
//        if (milestones == null) { iconBitmaps = null; return; }
//
//        int px = (int) dp(ICON_SIZE_DP);
//        iconBitmaps = new Bitmap[milestones.size()];
//
//        for (int i = 0; i < milestones.size(); i++) {
//            int resId = milestones.get(i).iconResId;
//            if (resId == 0) continue;
//            try {
//                Bitmap raw = BitmapFactory.decodeResource(getResources(), resId);
//                if (raw == null) continue;
//                iconBitmaps[i] = Bitmap.createScaledBitmap(raw, px, px, true);
//                if (iconBitmaps[i] != raw) raw.recycle();
//            } catch (Exception ignored) { }
//        }
//    }

    // ── Icon loading ──────────────────────────────────────────────────────────

    /**
     * Loads icons using AppCompatResources so both vector (VectorDrawable /
     * AnimatedVectorDrawable) and raster (PNG/WebP) drawables are decoded
     * correctly. BitmapFactory silently returns null for vector XMLs, which
     * was the root cause of icons not appearing.
     */
    private void loadIconBitmaps() {
        if (milestones == null) { iconBitmaps = null; return; }

        int px = (int) dp(ICON_SIZE_DP);
        iconBitmaps = new Bitmap[milestones.size()];

        for (int i = 0; i < milestones.size(); i++) {
            Milestone m = milestones.get(i);
            int resId = m.iconResId;
            if (resId == 0) continue;
            try {
                // AppCompatResources handles VectorDrawable on all API levels
                Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
                if (drawable == null) continue;

                // Apply tint if provided
                if (m.tintColor != null) {
                    androidx.core.graphics.drawable.DrawableCompat.setTint(
                            androidx.core.graphics.drawable.DrawableCompat.wrap(drawable.mutate()),
                            m.tintColor
                    );
                }

                // Give the drawable explicit bounds before rasterising
                drawable.setBounds(0, 0, px, px);

                // Rasterise onto a fresh Bitmap
                Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.draw(canvas);

                iconBitmaps[i] = bitmap;
            } catch (Exception e) {
                // Log in debug builds; silently skip in release
                android.util.Log.w("AchievementBar", "Could not load icon for milestone " + i, e);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Draws a 5-pointed star centered at (cx, cy) with the given outer radius. */
    private void drawStar(Canvas canvas, float cx, float cy, float outerR, Paint paint) {
        float innerR = outerR * 0.45f;
        starPath.reset();
        for (int i = 0; i < 10; i++) {
            float angle = (float) (Math.PI / 5.0 * i - Math.PI / 2.0);
            float r     = (i % 2 == 0) ? outerR : innerR;
            float x     = cx + (float) Math.cos(angle) * r;
            float y     = cy + (float) Math.sin(angle) * r;
            if (i == 0) starPath.moveTo(x, y);
            else        starPath.lineTo(x, y);
        }
        starPath.close();
        canvas.drawPath(starPath, paint);
    }

    private float dp(float dp) { return dp * density; }
    private float sp(float sp) { return sp * scaledDensity; }
}
