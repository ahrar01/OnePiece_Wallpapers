package com.triple.astudio.onepieceopwallpapers.activity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.triple.astudio.onepieceopwallpapers.HintServiceImpl;
import com.triple.astudio.onepieceopwallpapers.R;
import com.triple.astudio.onepieceopwallpapers.models.Hint;
import com.triple.astudio.onepieceopwallpapers.models.Wallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class DisplayImage extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private FloatingActionButton fab_more, fab_download, fab_set_wall, fab_share; // fab buttons on layout
    private Animation OpenAnimation, CloseAnimation, clockwiseAnimation, AnticlockwiseAnimation;
    private Boolean isOpen = false;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private LinearLayout LinearFabLayout;
    private String url, id;
    private CheckBox checkBoxFav;
    int position;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_display_image);
        fab_more = findViewById(R.id.fab_more);
        fab_download = findViewById(R.id.fab_download);
        fab_set_wall = findViewById(R.id.fab_set_wall);
        fab_share = findViewById(R.id.fab_share);
        LinearFabLayout = findViewById(R.id.LinearFablayout);
        Intent intent = getIntent();
        url = intent.getStringExtra("wallpaper_url");
        id = intent.getStringExtra("id");
        checkBoxFav = findViewById(R.id.checkBox_fav);
        HintServiceImpl hintService = new HintServiceImpl();
        hintService.addHint(new Hint(fab_more, "Here You Can Set Wallpaper,Download and Share", " "));
        hintService.addHint(new Hint(checkBoxFav, "Click Here to Save in Favourites", " "));


        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            hintService.showHint(this);
            editor.putBoolean("firstrun", false).apply();

        }


        //change check box state
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");

            final Wallpaper wallpaper = new Wallpaper(id, id, id, url);


            dbFavs.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot wallpaperSnapShot : dataSnapshot.getChildren()) {
                            String id = wallpaperSnapShot.getKey();
                            String title = wallpaperSnapShot.child("title").getValue(String.class);
                            String desc = wallpaperSnapShot.child("desc").getValue(String.class);
                            String url = wallpaperSnapShot.child("url").getValue(String.class);
                            Wallpaper w = new Wallpaper(id, title, desc, url);
                            w.id = dataSnapshot.getKey();
                            if (w.url.equals(wallpaper.url)) {
                                checkBoxFav.setChecked(true);
                            }

                        }


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        checkBoxFav.setOnCheckedChangeListener(this);
        //initialising the animation variable
        OpenAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        CloseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        clockwiseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        AnticlockwiseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);

        //  position = Integer.parseInt(intent.getStringExtra("position"));

        PhotoView photoView = findViewById(R.id.photo_view);
        Glide.with(this)
                .load(url)
                .thumbnail(Glide.with(this).load(R.drawable.loading))
                .into(photoView);

        fab_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) { // checking if the button is already clicked or not
                    fab_download.startAnimation(CloseAnimation);
                    fab_set_wall.startAnimation(CloseAnimation);
                    fab_more.startAnimation(AnticlockwiseAnimation);
                    fab_share.startAnimation(CloseAnimation);
                    fab_share.setClickable(false);
                    fab_set_wall.setClickable(false);
                    fab_download.setClickable(false);
                    LinearFabLayout.setVisibility(View.INVISIBLE);
                    isOpen = false;


                } else {
                    fab_download.startAnimation(OpenAnimation);
                    fab_set_wall.startAnimation(OpenAnimation);
                    fab_more.startAnimation(clockwiseAnimation);
                    fab_share.startAnimation(OpenAnimation);
                    fab_share.setClickable(true);
                    fab_set_wall.setClickable(true);
                    fab_download.setClickable(true);
                    LinearFabLayout.setVisibility(View.VISIBLE);
                    isOpen = true;
                }


            }
        });


        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWallpaper();
            }
        });
        fab_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadWallpaper();
                Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_SHORT).show();
            }
        });

        fab_set_wall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper();
            }
        });


    }


    private void shareWallpaper() {

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                          @Override
                          public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                              Intent intent = new Intent(Intent.ACTION_SEND);
                              intent.setType("image/*");
                              intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource));
                              intent.putExtra(Intent.EXTRA_TEXT, "Hey check this Amazing One Piece HD Wallpaper application " + "http://bit.ly/2MOrfhq");
                              startActivity(Intent.createChooser(intent, "One Piece Wallpaper"));
                          }
                      }
                );
    }

    private Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "OP_Wallpaper_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void downloadWallpaper() {

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                          @Override
                          public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                              Intent intent = new Intent(Intent.ACTION_VIEW);

                              Uri uri = saveWallpaperAndGetUri(resource, id);
                              if (uri != null) {
                                  // intent.setDataAndType(uri, "image/*");
                                  //startActivity(Intent.createChooser(intent, "One Piece Wallpaper"));

                                  sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

                              }
                          }
                      }
                );
    }


    private Uri saveWallpaperAndGetUri(Bitmap bitmap, String id) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat
                    .shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            return null;
        }

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/OnePiece_Wallpapers");
        folder.mkdirs();

        File file = new File(folder, id + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            return Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setWallpaper() {
        WallpaperManager myWallManager = WallpaperManager.getInstance(getApplicationContext());

      
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                        intent.setDataAndType(getLocalBitmapUri(resource), "image/*");
                        intent.putExtra("jpg", "image/*");
                        startActivity(Intent.createChooser(
                                intent, "Set as:"));
/*

                        try {
                            WallpaperManager.getInstance(getApplicationContext()).setBitmap(resource);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                });


    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first...", Toast.LENGTH_LONG).show();
            buttonView.setChecked(false);
            return;
        }

        DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("favourites");

        Wallpaper w = new Wallpaper(id, id, id, url);

        if (isChecked) {
            dbFavs.child(w.id).setValue(w);
        } else {
            dbFavs.child(w.id).setValue(null);
        }
    }
}

