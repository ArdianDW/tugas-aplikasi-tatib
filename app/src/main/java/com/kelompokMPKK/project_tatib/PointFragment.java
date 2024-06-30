package com.kelompokMPKK.project_tatib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PointFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PointFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView listView;
    private EditText searchBar;
    private PointAdapter adapter;
    private DatabaseReference databaseReference;

    public PointFragment() {
        // Required empty public constructor
    }

    public static PointFragment newInstance(String param1, String param2) {
        PointFragment fragment = new PointFragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_point, container, false);

        searchBar = (EditText) view.findViewById(R.id.search_bar);
        listView = (ListView) view.findViewById(R.id.list_view);

        // Inisialisasi Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("points");

        // Setup adapter
        PointAdapter adapter = new PointAdapter(getContext(), databaseReference);
        listView.setAdapter(adapter);

        // Setup search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PointItem item = (PointItem) adapter.getItem(position);
                String kodePoin = String.valueOf(item.getKode());
                PointDetailFragment detailFragment = PointDetailFragment.newInstance(kodePoin);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, detailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    public class PointAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater inflater;
        private List<PointItem> pointItems = new ArrayList<>();
        private List<PointItem> filteredPointItems = new ArrayList<>();

        public PointAdapter(Context context, DatabaseReference databaseReference) {
            this.inflater = LayoutInflater.from(context); // Cara yang benar untuk mendapatkan LayoutInflater
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pointItems.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        PointItem item = snapshot.getValue(PointItem.class);
                        pointItems.add(item);
                    }
                    filteredPointItems.addAll(pointItems); // Tambahkan ini
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        @Override
        public int getCount() {
            return filteredPointItems.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredPointItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = view.findViewById(android.R.id.text1);
            PointItem item = filteredPointItems.get(position);
            textView.setText(item.getKode() + ": " + item.getPelanggaran() + " (" + item.getPoin() + " poin)");
            return view;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<PointItem> filteredResults;
                    if (constraint == null || constraint.length() == 0) {
                        filteredResults = new ArrayList<>(pointItems); // Pastikan untuk membuat salinan
                    } else {
                        filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                    }

                    FilterResults results = new FilterResults();
                    results.values = filteredResults;

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredPointItems = (ArrayList<PointItem>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        private List<PointItem> getFilteredResults(String constraint) {
            List<PointItem> results = new ArrayList<>();
            for (PointItem item : pointItems) {
                // Konversi kode dari int ke String
                String kodeAsString = String.valueOf(item.getKode());
                String pelanggaran = item.getPelanggaran();

                // Cek apakah kode atau pelanggaran mengandung constraint
                if (kodeAsString.toLowerCase().contains(constraint) || pelanggaran.toLowerCase().contains(constraint)) {
                    results.add(item);
                }
            }
            return results;
        }
    }
}

