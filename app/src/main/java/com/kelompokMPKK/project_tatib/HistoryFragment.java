package com.kelompokMPKK.project_tatib;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private ListView listView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems = new ArrayList<>();
    private DatabaseReference historyRef, pointsRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listView = view.findViewById(R.id.listViewHistory);
        adapter = new HistoryAdapter(getActivity(), historyItems);
        listView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            historyRef = FirebaseDatabase.getInstance().getReference("history/" + userId);
            pointsRef = FirebaseDatabase.getInstance().getReference("points");

            fetchHistoryData();
        }

        return view;
    }

    public void fetchHistoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();

            DatabaseReference userRef = baseRef.child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    String role = userSnapshot.child("status").getValue(String.class);
                    DatabaseReference historyRef;

                    if ("guru".equals(role)) {
                        historyRef = baseRef.child("addHistory/" + userId);
                        Log.d("firebase", "Fetching history for guru from addHistory");
                    } else {
                        historyRef = baseRef.child("history/" + userId);
                        Log.d("firebase", "Fetching history from regular history");
                    }

                    historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                Log.e("firebase", "No data found in the history reference");
                                return;
                            }

                            historyItems.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                fetchDataForHistoryItem(snapshot);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("firebase", "Error getting history data", databaseError.toException());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("firebase", "Error checking user role", databaseError.toException());
                }
            });
        }
    }

    private void fetchDataForHistoryItem(DataSnapshot snapshot) {
        int kode = snapshot.child("kode").getValue(Integer.class);
        int poin = snapshot.child("poin").getValue(Integer.class);
        long timestamp = snapshot.child("timestamp").getValue(Long.class);
        DatabaseReference pointsRef = FirebaseDatabase.getInstance().getReference("points");

        pointsRef.child(String.valueOf(kode)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot pointSnapshot) {
                String pelanggaran = pointSnapshot.child("pelanggaran").getValue(String.class);
                historyItems.add(new HistoryItem(kode, poin, timestamp, pelanggaran));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("firebase", "Error getting point data", databaseError.toException());
            }
        });
    }
}
