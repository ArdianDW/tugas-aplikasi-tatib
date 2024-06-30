package com.kelompokMPKK.project_tatib;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link addPoint#newInstance} factory method to
 * create an instance of this fragment.
 */
public class addPoint extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String selectedPoint;  // Deklarasi variabel di tingkat kelas
    private ArrayAdapter<String> pointsAdapter;  // Adapter untuk ListView
    private Spinner spinnerUser;  // Spinner untuk memilih user
    private HashMap<String, String> nameToUidMap = new HashMap<>();

    public addPoint() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment addPoint.
     */
    // TODO: Rename and change types and number of parameters
    public static addPoint newInstance(String param1, String param2) {
        addPoint fragment = new addPoint();
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
        View view = inflater.inflate(R.layout.fragment_add_point, container, false);
        Spinner spinnerClass = view.findViewById(R.id.spinnerClass);
        spinnerUser = view.findViewById(R.id.spinnerUser); // Initialize spinnerUser here
        SearchView searchView = view.findViewById(R.id.searchView);
        ListView listViewPoints = view.findViewById(R.id.listViewPoints);
        Button buttonAddPoint = view.findViewById(R.id.buttonAddPoint);

        if (spinnerUser == null) {
            Log.e("Initialization Error", "Spinner is not found in the layout");
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference classesRef = databaseRef.child("classes");

        List<String> classes = new ArrayList<>();
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        classesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                classes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String className = snapshot.getKey();
                    classes.add(className);
                }
                classAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("firebase", "Error getting class data", databaseError.toException());
            }
        });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = classes.get(position);
                loadUsers(selectedClass, spinnerUser);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        pointsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewPoints.setAdapter(pointsAdapter);

        DatabaseReference pointsRef = FirebaseDatabase.getInstance().getReference("points");
        pointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pointsAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PointItem point = snapshot.getValue(PointItem.class);
                    String displayText = point.getPelanggaran() + " - " + point.getPoin();
                    pointsAdapter.add(displayText);
                }
                pointsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("firebase", "Error getting data", databaseError.toException());
            }
        });

        setupSearchAndSelection(searchView, listViewPoints, buttonAddPoint);

        listViewPoints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPoint = pointsAdapter.getItem(position);  // Menyimpan poin yang dipilih
                Toast.makeText(getContext(), "Poin dipilih: " + selectedPoint, Toast.LENGTH_SHORT).show();
            }
        });

        buttonAddPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerUser != null && selectedPoint != null) {
                    String selectedUserName = spinnerUser.getSelectedItem().toString();
                    String userId = nameToUidMap.get(selectedUserName);
                    if (userId != null) {
                        addPointToUser(userId, selectedPoint, selectedUserName);
                    } else {
                        Toast.makeText(getContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Pilih user dan poin terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadUsers(String selectedClass, Spinner spinnerUser) {
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query usersQuery = baseRef.orderByChild("kelas").equalTo(selectedClass);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUser.setAdapter(userAdapter);

        usersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userAdapter.clear();
                nameToUidMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (!"guru".equals(status)) {  // Menyaring user yang bukan guru
                        String uid = snapshot.getKey();
                        String name = snapshot.child("nama").getValue(String.class);
                        userAdapter.add(name);
                        nameToUidMap.put(name, uid);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("firebase", "Error getting user data", databaseError.toException());
            }
        });
    }

    private void setupSearchAndSelection(SearchView searchView, ListView listViewPoints, Button buttonAddPoint) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pointsAdapter.getFilter().filter(newText);
                return true;
            }
        });

        buttonAddPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerUser != null && selectedPoint != null) {
                    String selectedUserName = spinnerUser.getSelectedItem().toString();
                    String userId = nameToUidMap.get(selectedUserName);
                    if (userId != null) {
                        addPointToUser(userId, selectedPoint, selectedUserName);
                    } else {
                        Toast.makeText(getContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Pilih user dan poin terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addPointToUser(String userId, String selectedPoint, String userName) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("poin");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("addHistory");

        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentPoints = mutableData.getValue(Integer.class);
                if (currentPoints == null) {
                    mutableData.setValue(Integer.parseInt(selectedPoint.split(" - ")[1])); // asumsikan format "Pelanggaran - 10"
                } else {
                    mutableData.setValue(currentPoints + Integer.parseInt(selectedPoint.split(" - ")[1]));
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("addPoint", "Poin berhasil ditambahkan, memanggil addHistory");
                    addHistory(userId, selectedPoint, userName);
                } else {
                    Log.e("addPoint", "Gagal menambahkan poin: " + databaseError.getMessage());
                }
            }
        });
    }

    private void addHistory(String userId, String pointDetails, String userName) {
        try {
            String[] details = pointDetails.split(" - ");
            if (details.length < 2) {
                Log.e("addHistory", "Format pointDetails tidak valid: " + pointDetails);
                return;
            }

            // Menggunakan kode default jika tidak ada kode yang disediakan
            int kode = details.length == 3 ? Integer.parseInt(details[0]) : 62; // 999 sebagai kode default
            String pelanggaran = details.length == 3 ? details[1] : details[0];
            int poin = Integer.parseInt(details[details.length - 1]); // Poin selalu di akhir

            Map<String, Object> historyData = new HashMap<>();
            historyData.put("kode", kode);
            historyData.put("pelanggaran", pelanggaran);
            historyData.put("poin", poin);
            historyData.put("timestamp", ServerValue.TIMESTAMP);
            historyData.put("namaMurid", userName);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String guruId = currentUser.getUid();
                DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("addHistory").child(guruId).push();
                historyRef.setValue(historyData, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.e("addHistory", "Gagal mengirim data ke addHistory: " + databaseError.getMessage());
                        } else {
                            Log.d("addHistory", "Data berhasil dikirim ke addHistory");
                        }
                    }
                });
            } else {
                Log.e("addHistory", "Gagal mendapatkan pengguna yang sedang login");
            }
        } catch (NumberFormatException e) {
            Log.e("addHistory", "Gagal mengonversi kode atau poin ke integer", e);
        } catch (Exception e) {
            Log.e("addHistory", "Error processing history item", e);
        }
    }
}
