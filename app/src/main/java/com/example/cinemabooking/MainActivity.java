package com.example.cinemabooking;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.usecase.movie.GetMoviesUseCase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOVIE_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        testFetchMovies();
    }

    private void testFetchMovies() {
        GetMoviesUseCase useCase = ((MyApp) getApplication())
                .getAppContainer()
                .getMoviesUseCase();

        useCase.execute(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> data) {
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "No movies found");
                    return;
                }

                for (Movie movie : data) {
                    Log.d(TAG, "Movie: " + movie.title);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error: " + message);
            }
        });
    }
}