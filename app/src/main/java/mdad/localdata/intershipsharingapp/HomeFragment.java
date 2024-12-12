package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private LinearLayout lv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        lv = view.findViewById(R.id.list);
        fetchInternships();
        return view;
    }

    private void fetchInternships() {
        String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_internship,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] internships = response.split(":");
                    for (String internship : internships) {
                        if (!internship.isEmpty()) {
                            String[] details = internship.split(";");
                            if (details.length >= 10) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("InternshipID", details[0]);
                                map.put("title", details[1]);
                                map.put("description", details[2]);
                                map.put("company", details[3]);
                                map.put("start_date", details[4]);
                                map.put("end_date", details[5]);
                                map.put("date_shared", details[6]);
                                map.put("user_name", details.length > 7 ? details[7] : "");
                                map.put("username", details.length > 8 ? details[8] : "");
                                map.put("location_name", details.length > 9 ? details[9] : "");
                                map.put("role", details[10]);
                                Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                addInternshipToLayout(map);
                            }
                        }
                    }
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addInternshipToLayout(final HashMap<String, String> item) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(20, 20, 20, 20);
        textView.setTextSize(16);
        textView.setBackgroundColor(0xFFFFFFFF);
        textView.setTextColor(0xFF000000);

        String displayText = "Internship ID: " + item.get("InternshipID") +
                "\nTitle: " + item.get("title") +
                "\nCompany: " + item.get("company") +
                "\nStart Date: " + item.get("start_date") +
                "\nEnd Date: " + item.get("end_date") +
                "\nDate Shared: " + item.get("date_shared") +
                "\nUser: " + item.get("user_name") +
                "\nUsername: " + item.get("username") +
                "\nLocation Name: " + item.get("location_name")+
                "\nRole: " + item.get("role");

        textView.setText(displayText);
        lv.addView(textView);
    }
}
