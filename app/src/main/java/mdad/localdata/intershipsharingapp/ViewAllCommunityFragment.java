package mdad.localdata.intershipsharingapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class ViewAllCommunityFragment extends Fragment {

    private static final String url_get_communities = StaffMainActivity.ipBaseAddress + "/get_all_communities.php";
    private RecyclerView recyclerView;
    private CarouselAdapter adapter;
    private List<CarouselAdapter.CarouselItem> items;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_all_community, container, false);

        recyclerView = view.findViewById(R.id.carouselRecyclerView);  // Ensure recyclerView is initialized
        items = new ArrayList<>();
        adapter = new CarouselAdapter(items, item -> {
            // When an item is clicked, navigate to the detail fragment

            // Create a new instance of the CommunityDetailFragment
            ViewSelectedCommunityFragment detailFragment =ViewSelectedCommunityFragment.newInstance(item.getTitle(),item.getDescription(), item.getChatID(),item.getImageBitmap());

            // Begin the fragment transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailFragment); // Use your container ID
            transaction.addToBackStack(null); // Add to back stack for navigation
            transaction.commit(); // Commit the transaction
        });

        recyclerView.setAdapter(adapter);

        // Use LinearLayoutManager for vertical scrolling
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2); // 2 columns
        recyclerView.setLayoutManager(gridLayoutManager);


        // Remove PagerSnapHelper since you don't need snapping behavior anymore
        // PagerSnapHelper snapHelper = new PagerSnapHelper();
        // snapHelper.attachToRecyclerView(recyclerView);

        // Fetch the community data from the URL
        fetchCommunityData();

        return view;
    }

    private void fetchCommunityData() {
        String url_get_communities = StaffMainActivity.ipBaseAddress + "/get_all_communities.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_communities,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving community data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] communities = response.split(":");
                    items.clear();  // Clear the existing items before adding new ones

                    for (String community : communities) {
                        if (!community.isEmpty()) {
                            String[] details = community.split(";");
                            if (details.length >= 3) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("communityId", details[0]);
                                map.put("title", details[1]);
                                map.put("description", details[2]);
                                map.put("photo", details.length > 3 ? details[3] : "");
                                Log.d("CommunityDetails", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                addCommunityToCarousel(map);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving community data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addCommunityToCarousel(final HashMap<String, String> item) {
        // Create a CarouselItem for each community and add it to the list
        String imageData = item.get("photo");
        Log.d("CommunityDetails", "Photo Data: " + item.get("photo"));
        int imageResId = R.drawable.no_image; // Set default image

        if (imageData != null && !imageData.isEmpty()) {
            saveBase64ToFile(imageData, file -> {
                // Once the image is saved, decode the file to Bitmap
                Log.d("CommunityDetails", "File Path: " + file.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Log.d("CommunityDetails", "Bitmap: " + bitmap);
                if (bitmap != null) {
                    // Add new CarouselItem with the image and title
                    items.add(new CarouselAdapter.CarouselItem(bitmap, item.get("title"), item.get("description"),item.get("communityId")));
                    Log.d("CommunityDetails", "Item Added: " + item.get("communityId"));
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("ImageError", "Failed to decode bitmap from file.");
                }
            });
        } else {
            // Add a default item if no image is available
            items.add(new CarouselAdapter.CarouselItem( imageResId , item.get("title"), item.get("description"),item.get("communityId")));
            adapter.notifyDataSetChanged();
        }
    }


    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            // Decode the base64 data into a byte array
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            // Create the PNG file in the cache directory
            File cacheDir = requireContext().getCacheDir();
            File imageFile = new File(cacheDir, "community_image.png");  // PNG extension

            // Save the decoded bytes to the file
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(decodedBytes);
                fos.flush();  // Ensure all data is written
                listener.onFileSaved(imageFile);  // Notify when file is saved
            }

        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }
}
