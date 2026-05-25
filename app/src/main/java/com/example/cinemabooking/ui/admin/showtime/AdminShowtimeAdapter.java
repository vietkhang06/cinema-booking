package com.example.cinemabooking.ui.admin.showtime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.model.Showtime;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminShowtimeAdapter extends RecyclerView.Adapter<AdminShowtimeAdapter.ViewHolder> {

    public interface OnShowtimeActionListener {
        void onEdit(Showtime showtime);
        void onDelete(Showtime showtime);
    }

    private final List<Showtime> showtimes;
    private final Map<String, Movie> movieMap;
    private final Map<String, Cinema> cinemaMap;
    private final Map<String, Room> roomMap;
    private final OnShowtimeActionListener listener;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public AdminShowtimeAdapter(
            List<Showtime> showtimes,
            Map<String, Movie> movieMap,
            Map<String, Cinema> cinemaMap,
            Map<String, Room> roomMap,
            OnShowtimeActionListener listener) {
        this.showtimes = showtimes;
        this.movieMap = movieMap;
        this.cinemaMap = cinemaMap;
        this.roomMap = roomMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_showtime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Showtime showtime = showtimes.get(position);

        // Resolve movie name
        Movie movie = movieMap.get(showtime.movieId);
        holder.tvMovieTitle.setText(movie != null ? movie.title : "Phim không xác định");

        // Resolve cinema and room name
        Cinema cinema = cinemaMap.get(showtime.cinemaId);
        Room room = roomMap.get(showtime.roomId);
        String cinemaRoomText = (cinema != null ? cinema.name : "Rạp không xác định") + 
                " - " + (room != null ? room.name : "Phòng không xác định");
        holder.tvCinemaRoom.setText(cinemaRoomText);

        // Time format: HH:mm - dd/MM/yyyy
        String startTime = dateFormat.format(new Date(showtime.startAt));
        String endTime = timeFormat.format(new Date(showtime.endAt));
        holder.tvShowtimeTime.setText("Thời gian: " + startTime + " ~ " + endTime);

        // Format and Language
        String formatLang = (showtime.format != null ? showtime.format : "2D") + " | " + 
                (showtime.language != null ? showtime.language : "Phụ đề");
        holder.tvFormatLang.setText(formatLang);

        // Price formatting
        holder.tvPrice.setText(currencyFormat.format(showtime.basePrice));

        // Click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(showtime);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(showtime);
        });
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle;
        TextView tvCinemaRoom;
        TextView tvShowtimeTime;
        TextView tvFormatLang;
        TextView tvPrice;
        View btnEdit;
        View btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaRoom = itemView.findViewById(R.id.tvCinemaRoom);
            tvShowtimeTime = itemView.findViewById(R.id.tvShowtimeTime);
            tvFormatLang = itemView.findViewById(R.id.tvFormatLang);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
