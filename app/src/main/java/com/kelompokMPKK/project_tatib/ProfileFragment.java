package com.kelompokMPKK.project_tatib;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ProfileFragment extends Fragment {

    private Button logoutButton;
    private FirebaseAuth mAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button changePwBtn = view.findViewById(R.id.ChangePwBtn);

        changePwBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Changepw.class);
            startActivity(intent);
        });

        logoutButton = view.findViewById(R.id.logoutBtn);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = null;
        if (currentUser != null) {
            uid = currentUser.getUid();
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = databaseRef.child("users").child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userEmail = currentUser.getEmail();
                    String nama = dataSnapshot.child("nama").getValue(String.class);
                    String kelas = dataSnapshot.child("kelas").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);
                    String mapel = dataSnapshot.child("mapel").getValue(String.class);
                    String alamat = dataSnapshot.child("alamat").getValue(String.class);
                    String ttl = dataSnapshot.child("ttl").getValue(String.class);

                    TextView namaUserText = view.findViewById(R.id.namaUser);
                    TextView kelasText = view.findViewById(R.id.kelas);
                    TextView emailTextView = view.findViewById(R.id.infoEmail);
                    TextView nisText = view.findViewById(R.id.infoNIS);
                    TextView alamatText = view.findViewById(R.id.infoAlamat);
                    TextView ttlText = view.findViewById(R.id.infoTTL);
                    TextView nisLabel = view.findViewById(R.id.tagNIS);

                    Integer poinPelanggaran = dataSnapshot.child("poin").getValue(Integer.class);
                    if (poinPelanggaran == null) poinPelanggaran = 0;

                    if ("guru".equals(status)) {
                        nisText.setText(mapel);
                        nisLabel.setText("Mata Pelajaran");
                        kelasText.setText("Wali kelas " + kelas);
                    } else {
                        nisLabel.setText("Nis ( Nomor Induk Siswa )");
                        nisText.setText(dataSnapshot.child("nis").getValue(String.class));
                        kelasText.setText(kelas);
                    }

                    namaUserText.setText(nama);
                    emailTextView.setText(userEmail);
                    alamatText.setText(alamat);
                    ttlText.setText(ttl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(getActivity(), "Anda telah logout", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getActivity(), login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        return view;
    }
}
