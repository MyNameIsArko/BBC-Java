package com.example.bbc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String apiKey = "AIzaSyDTiilHAiEkIxfuDYxJACuClN9XnJd2r8E";
    private String channelId = "UCndmr6gTAxUk8rxA8yF8Rwg";
    private String url = "https://www.googleapis.com/youtube/v3/search?key=" + apiKey + "&channelId=" + channelId + "&part=snippet,id&order=date&maxResults=30";

    JSONArray allVideos = null;
    JSONObject[] bbcVideos = new JSONObject[3];

    ImageButton btn1, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set buttons reference
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadVideos();
                selectBBC();
            }
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateThumbnails();
    }

    // Fetch all videos from channel
    private void downloadVideos() {
        String documentString = "";
        JSONObject document = new JSONObject();
        try {
            documentString = Jsoup.connect(url).ignoreContentType(true).get().body().text();
            JSONParser parser = new JSONParser();
            document = (JSONObject) parser.parse(documentString);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        allVideos = (JSONArray) document.get("items");
    }

    // Select latest 3 videos with BBC in title
    private void selectBBC() {
        int bbcCounter = 0;
        for (int i = 0; i < allVideos.size(); i++) {
            if(bbcCounter >= 3) break;
            JSONObject video = (JSONObject) allVideos.get(i);
            JSONObject snippet = (JSONObject) video.get("snippet");
            String title = (String) snippet.get("title");
            if(title.contains("BBC")) {
                bbcVideos[bbcCounter] = video;
                bbcCounter += 1;
            }
        }
    }

    // Update buttons image to thumbnail
    private void updateThumbnails() {
        for(int i = 0; i < bbcVideos.length; i++)
        {
            JSONObject video = bbcVideos[i];
            JSONObject snippet = (JSONObject) video.get("snippet");
            JSONObject thumbnails = (JSONObject) snippet.get("thumbnails");
            JSONObject highResThumbnail = (JSONObject) thumbnails.get("high");
            String thumbnailUrl = (String) highResThumbnail.get("url");
            System.out.println(thumbnailUrl);
            switch (i + 1) {
                case 1:
                    Picasso.get().load(thumbnailUrl).into(btn1);
                    break;
                case 2:
                    Picasso.get().load(thumbnailUrl).into(btn2);
                    break;
                case 3:
                    Picasso.get().load(thumbnailUrl).into(btn3);
                    break;
            }
        }
    }

    // Select video according to pressed button
    public void selectVideo(View view) {
        JSONObject id;
        String videoId = "";
        switch (view.getId()) {
            case R.id.button1:
                id = (JSONObject) bbcVideos[0].get("id");
                videoId = (String) id.get("videoId");
                break;
            case R.id.button2:
                id = (JSONObject) bbcVideos[1].get("id");
                videoId = (String) id.get("videoId");
                break;
            case R.id.button3:
                id = (JSONObject) bbcVideos[2].get("id");
                videoId = (String) id.get("videoId");
                break;
        }
        showVideo(videoId);
    }

    // Show selected video in Youtube
    private void showVideo(String id) {
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
        startActivity(youtubeIntent);
    }

    // Exit app
    public void exitApp(View view) {
        finishAndRemoveTask();
    }
}