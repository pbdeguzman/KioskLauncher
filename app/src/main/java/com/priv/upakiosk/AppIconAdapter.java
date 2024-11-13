package com.priv.upakiosk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppIconAdapter extends RecyclerView.Adapter<AppIconAdapter.ViewHolder> {
    public interface Callback {
        void onClick(PackageInfo item);
    }

    private AppIconAdapter.Callback callback;

    public void setCallback(AppIconAdapter.Callback callback) {
        this.callback = callback;
    }

    private final List<PackageInfo> appIconList;

    public AppIconAdapter(List<PackageInfo> appIconList) {
        this.appIconList = appIconList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_holder, viewGroup, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppIconAdapter.ViewHolder viewHolder, int i) {
        PackageInfo selectedItem = appIconList.get(i);
        viewHolder.tvAppName.setText(selectedItem.getName());
        viewHolder.ivIcon.setImageDrawable(selectedItem.getIcon());

        viewHolder.layoutHolder.setOnClickListener(view -> callback.onClick(selectedItem));
    }

    @Override
    public int getItemCount() {
        return appIconList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View layoutHolder;
        private final ImageView ivIcon;
        private final TextView tvAppName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHolder = itemView.findViewById(R.id.layoutHolder);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
        }
    }
}
