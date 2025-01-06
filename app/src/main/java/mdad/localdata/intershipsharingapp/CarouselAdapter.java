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

    public CarouselAdapter(List<CarouselItem> items) {
        this.items = items;
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

        // If the item contains a Bitmap, set the ImageView with the Bitmap
        if (item.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(item.getImageBitmap());
        } else {
            // If the item contains a resource ID, set the ImageView with the resource
            holder.imageView.setImageResource(item.getImageResId());
        }

        holder.textView.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
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
        private String title;

        // Constructor for resource ID
        public CarouselItem(int imageResId, String title) {
            this.imageResId = imageResId;
            this.imageBitmap = null; // Not used when using resource ID
            this.title = title;
        }

        // Constructor for Bitmap image
        public CarouselItem(Bitmap imageBitmap, String title) {
            this.imageResId = 0; // Not used when using Bitmap
            this.imageBitmap = imageBitmap;
            this.title = title;
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
    }
}