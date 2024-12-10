package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class UserMainActivity extends AppCompatActivity {
    private LinearLayout lv;  // Reference to LinearLayout to dynamically add TextViews
    private static String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        lv = findViewById(R.id.list);  // Reference to the LinearLayout
        postData(url_all_internship, null);
        Map<Integer, Class<?>> navigationMap = new HashMap<>();
        navigationMap.put(R.id.menu_home, UserMainActivity.class);
        navigationMap.put(R.id.menu_login, LoginActivity.class);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Class<?> selectedActivity = navigationMap.get(item.getItemId());

            if (selectedActivity != null) {
                Intent intent = new Intent(UserMainActivity.this, selectedActivity);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void postData(String url, Map<String, String> params) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Split the response by the ':' character to get each internship
                        String[] internships = response.split(":");

                        for (String internship : internships) {
                            if (!internship.isEmpty()) {
                                String[] details = internship.split(";");

                                if (details.length >= 7) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("InternshipID", details[0]);
                                    map.put("name", details[1]);
                                    map.put("description", details[2]);
                                    map.put("company", details[3]);
                                    map.put("start_date", details[4]);
                                    map.put("end_date", details[5]);
                                    map.put("date_shared", details[6]);
                                    map.put("user_name", details.length > 7 ? details[7] : "");
                                    map.put("username", details.length > 8 ? details[8] : "");
                                    map.put("location_name", details.length > 9 ? details[9] : "");

                                    addInternshipToLayout(map);  // Add the data to a new TextView
                                }
                            }
                        }
                    }

                    private void addInternshipToLayout(final HashMap<String, String> item) {
                        TextView textView = new TextView(UserMainActivity.this);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        textView.setPadding(20, 20, 20, 20);
                        textView.setTextSize(16);
                        textView.setBackgroundColor(0xFFFFFFFF);  // White background for text view
                        textView.setTextColor(0xFF000000);  // Black text color

                        String displayText = "Internship ID: " + item.get("InternshipID") +
                                "\nName: " + item.get("name") +
                                "\nCompany: " + item.get("company") +
                                "\nStart Date: " + item.get("start_date") +
                                "\nEnd Date: " + item.get("end_date") +
                                "\nDate Shared: " + item.get("date_shared") +
                                "\nUser: " + item.get("user_name") +
                                "\nUsername: " + item.get("username") +
                                "\nLocation Name: " + item.get("location_name");

                        textView.setText(displayText);

                        // Set click listener on TextView
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(UserMainActivity.this, "Clicked on: " + item.get("company"), Toast.LENGTH_SHORT).show();

                                // You can perform an action here like opening a detailed view or navigating to another activity
                                // Intent intent = new Intent(UserMainActivity.this, InternshipDetailsActivity.class);
                                // intent.putExtra("InternshipID", item.get("InternshipID"));
                                // startActivity(intent);
                            }
                        });

                        lv.addView(textView);  // Dynamically add TextView to the layout
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(getApplicationContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(stringRequest);
    }
}


