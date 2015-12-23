package org.korjus.movietorrents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PosterPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster_popup);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        ImageView imageView = (ImageView) findViewById(R.id.ivPosterFull);
        Picasso.with(this)
                .load(url)
                .error(R.drawable.error)
                .into(imageView);
    }
}
