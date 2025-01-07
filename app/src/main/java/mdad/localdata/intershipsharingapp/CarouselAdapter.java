package mdad.localdata.intershipsharingapp;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    private final List<CarouselItem> items;
    private final OnItemClickListener onItemClickListener;

    // Constructor with listener
    public CarouselAdapter(List<CarouselItem> items, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.carousel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarouselItem item = items.get(position);

        // Set image
        if (item.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(item.getImageBitmap());
        } else {
            holder.imageView.setImageResource(item.getImageResId());
        }

        // Set title
        holder.textView.setText(item.getTitle());

        // Set click listener
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        Log.d("CarouselAdapter", "onBindViewHolder called with item: " + item.getChatID());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(CarouselItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public static class CarouselItem {
        private int imageResId; // For resource ID (e.g., default image)
        private Bitmap imageBitmap; // For Bitmap images
        private String title, description;
        private String communityId;


        // Constructor for resource ID
        public CarouselItem(int imageResId, String title, String description,String communityId) {
            this.imageResId = imageResId;
            this.imageBitmap = null; // Not used when using resource ID
            this.title = title;
            this.description = description;
            this.communityId = communityId;
        }

        // Constructor for Bitmap image
        public CarouselItem(Bitmap imageBitmap, String title, String description,String communityId) {
            this.imageResId = 0; // Not used when using Bitmap
            this.imageBitmap = imageBitmap;
            this.title = title;
            this.description = description;
            this.communityId = communityId;

        }

        public String getChatID() {
            return communityId;  // Use getCommunityId instead of getChatID
        }

        public int getImageResId() {
            return imageResId;
        }

        public Bitmap getImageBitmap() {
            return imageBitmap;
        }

        public String getTitle() {
            return title;
        }
        public String getDescription() {
            return description;
        }
    }
}

