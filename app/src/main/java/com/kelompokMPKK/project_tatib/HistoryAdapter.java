package com.kelompokMPKK.project_tatib;

import android.content.Context;
import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.Date;
import java.util.List;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {
    public HistoryAdapter(Context context, List<HistoryItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
        }

        HistoryItem item = getItem(position);
        TextView tvKode = convertView.findViewById(R.id.tvKode);
        TextView tvPoin = convertView.findViewById(R.id.tvPoin);
        TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);
        TextView tvPelanggaran = convertView.findViewById(R.id.tvPelanggaran);

        tvKode.setText(String.valueOf(item.getKode()));
        tvPoin.setText(String.valueOf(item.getPoin()));
        tvTimestamp.setText(DateFormat.getDateTimeInstance().format(new Date(item.getTimestamp())));
        tvPelanggaran.setText(item.getPelanggaran());

        return convertView;
    }
}
