package example.com.crackle;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static example.com.crackle.Constants.API_KEY;
import static example.com.crackle.Constants.IMAGE_URL_SIZE;
import static example.com.crackle.Constants.LOG_TAG;

public class MovieDetailsActivity extends AppCompatActivity {

    @BindView(R.id.poster_image)
    ImageView posterImage;
    @BindView(R.id.backdrop_image)
    ImageView backdropImage;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.year)
    TextView year;
    @BindView(R.id.duration)
    TextView duration;
    @BindView(R.id.genre)
    TextView genre;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    private Movie movie;
    private int movieId;

    private MovieApiClient client;
    private Call<DetailResults> call;
    private HashMap<Integer, String> genreMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ButterKnife.bind(this);

        genreMap = Utils.fetchAllGenres(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        if (getIntent() != null) {
            if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
                movie = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
                movieId = movie.getMovieId();

                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    boolean isShow = true;
                    int scrollRange = -1;

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();
                        }
                        if (scrollRange + verticalOffset == 0) {
                            collapsingToolbarLayout.setTitle(movie.getTitle());
                            isShow = true;
                        } else if (isShow) {
                            collapsingToolbarLayout.setTitle(" ");
                            isShow = false;
                        }

                    }
                });

                title.setText(movie.getTitle());
                year.setText(movie.getReleaseDate().substring(0,4));
                Glide.with(this)
                        .load(IMAGE_URL_SIZE.concat(movie.getImageUrl()))
                        .into(posterImage);
                Glide.with(this)
                        .load(IMAGE_URL_SIZE.concat(movie.getBackdropImageUrl()))
                        .into(backdropImage);

                List<Integer> genreId = new ArrayList<>(movie.getGenres());
                int count = 0;
                for (int id : genreId) {
                    genre.append(genreMap.get(id));
                    count++;
                    if (count < genreId.size()) {
                        genre.append(", ");
                    }
                }

            }
        }

        if (movieId != 0) {
            client = MovieApiService.getClient().create(MovieApiClient.class);
            call = client.getMovieDetails(movieId, API_KEY);

            call.enqueue(new Callback<DetailResults>() {
                @Override
                public void onResponse(@NonNull Call<DetailResults> call, @NonNull Response<DetailResults> response) {
                    if (response.body() == null) {
                        return;
                    }
                    int runtime = response.body().getDuration();
                    duration.setText(Utils.formatDuration(MovieDetailsActivity.this, runtime));
                }

                @Override
                public void onFailure(@NonNull Call<DetailResults> call, @NonNull Throwable t) {
                    Toast.makeText(MovieDetailsActivity.this, "Error getting movie duration", Toast.LENGTH_SHORT).show();
                }
            });
        }

        viewPager.setAdapter(new MovieFragmentPagerAdapter(getSupportFragmentManager(), movie));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
