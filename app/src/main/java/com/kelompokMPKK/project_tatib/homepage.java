package com.kelompokMPKK.project_tatib;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.net.Uri;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class homepage extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        bottomNavigationView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);
        fab = findViewById(R.id.fab);

        loadFragment(new HomeFragment(), false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    loadFragment(new HomeFragment(), false);

                } else if (itemId == R.id.history) {
                    loadFragment(new HistoryFragment(), false);

                } else if (itemId == R.id.point) {
                    loadFragment(new PointFragment(), false);

                } else {
                    loadFragment(new ProfileFragment(), false);
                }


                return true;
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + userId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String status = dataSnapshot.child("status").getValue(String.class);
                        if ("guru".equals(status)) {
                            // Mengubah ikon FAB menjadi logo "+"
                            fab.setImageResource(R.drawable.baseline_add_24);
                            // Mengubah fungsi FAB untuk membuka AddPointFragment
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    loadFragment(new addPoint(), true);
                                }
                            });
                        } else {
                            // Mengembalikan ikon FAB menjadi logo scan QR dan fungsi scanner
                            fab.setImageResource(R.drawable.baseline_qr_code_scanner_24);
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Scanner();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("firebase", "Error getting data", databaseError.toException());
                }
            });
        }

    }

    private void Scanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(startScan.class);
        launcher.launch(options);
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }


        fragmentTransaction.commit();
    }
    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            handleDynamicLink(result.getContents());
        }
    });

    private void handleDynamicLink(String link) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(Uri.parse(link))
            .addOnSuccessListener(this, pendingDynamicLinkData -> {
                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.getLink();
                }
    
                int pointsToAdd = Integer.parseInt(deepLink.getQueryParameter("pointsToAdd"));
                String pointsCode = deepLink.getQueryParameter("pointsCode");
    
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
    
                    
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + userId);
                    userRef.child("poin").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int currentPoints = task.getResult().getValue(Integer.class);
                            userRef.child("poin").setValue(currentPoints + pointsToAdd);
                        } else {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                    });
    
                    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history/" + userId);
                    Map<String, Object> historyData = new HashMap<>();
                    historyData.put("poin", pointsToAdd);
                    try {
                        int pointsCodeInt = Integer.parseInt(pointsCode);
                        historyData.put("kode", pointsCodeInt);
                    } catch (NumberFormatException e) {
                        Log.e("firebase", "Format kode tidak valid", e);
                        historyData.put("kode", 0);
                    }
                    historyData.put("timestamp", ServerValue.TIMESTAMP);
    
                    historyRef.push().setValue(historyData);
                } else {
                    Log.e("firebase", "User not logged in");
                }
            })
            .addOnFailureListener(this, e -> Log.w("DynamicLinks", "getDynamicLink:onFailure", e));
    }
}
