package com.kelompokMPKK.project_tatib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam2;
    private TextView date;
    private ListView listView;
    private EditText editNote;
    private ArrayList<String> notesList;
    private ArrayAdapter<String> notesAdapter;
    private static final int MAX_NOTES = 4;
    private String uid;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        date = view.findViewById(R.id.date);
        listView = view.findViewById(R.id.listview);
        editNote = view.findViewById(R.id.edit_note);
        Button clearNotesButton = view.findViewById(R.id.clear_notes);
        TextView nameHome = view.findViewById(R.id.nameHome); // Pastikan ID ini sesuai dengan layout XML Anda
        TextView poinPelanggaranText = view.findViewById(R.id.totalpoin);
        TextView kalimatJumlahPoin = view.findViewById(R.id.kalimatjumlahpoin); // Pastikan ID ini sesuai dengan layout XML Anda

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        }
        startClock();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = databaseRef.child("users").child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if ("guru".equals(status)) {
                        String kelas = dataSnapshot.child("kelas").getValue(String.class);
                        nameHome.setText("Wali Kelas " + kelas);

                        // Sembunyikan TextView poin pelanggaran dan kalimat jumlah poin
                        poinPelanggaranText.setVisibility(View.GONE);
                        kalimatJumlahPoin.setVisibility(View.GONE);

                        // Ambil dan tampilkan mata pelajaran dengan ukuran teks yang lebih kecil
                        String mapel = dataSnapshot.child("mapel").getValue(String.class);
                        poinPelanggaranText.setText("Mata Pelajaran: " + mapel);
                        poinPelanggaranText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        poinPelanggaranText.setVisibility(View.VISIBLE);
                    } else {
                        String nama = dataSnapshot.child("nama").getValue(String.class);
                        nameHome.setText(nama);

                        // Tampilkan poin pelanggaran dan kalimat jumlah poin untuk non-guru dengan ukuran teks normal
                        Integer poinPelanggaran = dataSnapshot.child("poin").getValue(Integer.class);
                        if (poinPelanggaran == null) poinPelanggaran = 0;
                        poinPelanggaranText.setText("Total Poin: " + poinPelanggaran);
                        poinPelanggaranText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        poinPelanggaranText.setVisibility(View.VISIBLE);
                        kalimatJumlahPoin.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        notesList = new ArrayList<>();
        notesAdapter = new ArrayAdapter<>(requireActivity(), R.layout.list_item_note, R.id.note_text, notesList);
        listView.setAdapter(notesAdapter);

        loadNotesFromFirebase();

        ImageButton addNoteButton = (ImageButton) view.findViewById(R.id.add_note);
        addNoteButton.setOnClickListener(v -> addNote());
        clearNotesButton.setOnClickListener(v -> clearNotes());

        return view;
    }

    private void loadNotesFromFirebase() {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String note = snapshot.getValue(String.class);
                    notesList.add(note);
                }
                notesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error loading notes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startClock() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        if (isAdded()) { // cek apakah Fragment masih ditambahkan ke Activity
                            requireActivity().runOnUiThread(() -> {
                                long dateLong = System.currentTimeMillis();
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy\nHH:mm:ss", new Locale("id", "ID"));
                                String dateString = sdf.format(dateLong);
                                date.setText(dateString);
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void addNote() {
        if (notesList.size() >= MAX_NOTES) {
            Toast.makeText(getActivity(), "Maximum number of notes reached", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = editNote.getText().toString().trim();
        if (!note.isEmpty()) {
            DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
            String noteId = notesRef.push().getKey();
            notesRef.child(noteId).setValue(note);
            notesList.add(note);
            notesAdapter.notifyDataSetChanged();
            editNote.setText("");
        } else {
            Toast.makeText(getActivity(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearNotes() {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        notesRef.removeValue();
        notesList.clear();
        notesAdapter.notifyDataSetChanged();
    }

}
