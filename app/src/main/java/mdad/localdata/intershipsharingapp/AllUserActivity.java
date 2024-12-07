package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllUserActivity extends AppCompatActivity {

    ListView lv;
    ArrayList<HashMap<String, String>> userList;
    private static String url_all_user = StaffMainActivity.ipBaseAddress + "/get_all_user.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view first
        setContentView(R.layout.activity_all_user);
        EdgeToEdge.enable(this);

        // Get resource id of ListView after content view is set
        lv = findViewById(R.id.list);

        // ArrayList to store user info in HashMap for ListView
        userList = new ArrayList<HashMap<String, String>>();

        // Re-usable method to use Volley to retrieve users from database
        postData(url_all_user, null);
    }

    public void postData(String url, Map<String, String> params) {
        // Create a RequestQueue for Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a StringRequest for Volley (POST method)
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                // Response from the server
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the response from the server
                        Log.d("Response", response);  // Add this line to log the response

                        // Check if error code received from server
                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Handle the response data received from server
                        // Assuming the server is returning a JSON array or a formatted string, you can parse it accordingly
                        String[] users = response.split(":");

                        // Check if the response contains data
                        if (users.length == 0) {
                            Toast.makeText(getApplicationContext(), "No users found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // For each user, retrieve the user details
                        for (String user : users) {
                            String[] details = user.split(";");
                            if (details.length >=7) { // Ensure there are 8 fields (safety check)
                                String UserId = details[0];
                                String name = details[1];
                                String email = details[2];
                                String username = details[3];
                                String password = details[4];
                                String course = details[5];
                                String year_of_study = details.length > 6 ? details[6] : ""; // Handle missing year_of_study
                                String graduated_year = details.length > 7 ? details[7] : ""; // Handle missing graduated_year
                                String RoleId = details.length > 8 ? details[8] : "";

                                String header = "Role: " + RoleId;
                                // Create a new HashMap to store user info
                                HashMap<String, String> map = new HashMap<>();
                                map.put("UserId", UserId);
                                map.put("name", name);
                                map.put("email", email);
                                map.put("username", username);
                                map.put("password", password);
                                map.put("course", course);
                                map.put("year_of_study", year_of_study);
                                map.put("graduated_year", graduated_year);
                                map.put("RoleId", RoleId);

                                // Add map to the ArrayList
                                userList.add(map);
                            }
                        }

                        // Populate the ListView with user information from HashMap
                        ListAdapter adapter = new SimpleAdapter(
                                AllUserActivity.this, userList,
                                R.layout.user_list_item, new String[]{"UserId", "name", "email", "username", "password", "course", "year_of_study", "graduated_year","RoleId"},
                                new int[]{R.id.UserId, R.id.name, R.id.email, R.id.username, R.id.password, R.id.course, R.id.year_of_study,R.id.graduated_year, R.id.RoleId});

                        // Update ListView
                        lv.setAdapter(adapter);
                    }
                },

                // Error in Volley
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());  // Log the error
                        Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Add StringRequest to RequestQueue in Volley
        queue.add(stringRequest);
    }

}
