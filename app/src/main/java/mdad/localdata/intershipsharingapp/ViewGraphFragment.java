package mdad.localdata.intershipsharingapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ViewGraphFragment extends Fragment {
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private LineChart lineChart;

    public ViewGraphFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_graph, container, false);
        String communityId = getArguments().getString("communityId", "Default ID");


        // Find the LineChart view
        lineChart = view.findViewById(R.id.line_chart);

        // Fetch data from the server
        fetchData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_get_userchat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse and process the response
                        processResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ViewGraphFragment", "Error fetching data: " + error.getMessage());
                    }
                });

        queue.add(stringRequest);
    }

    private void processResponse(String response) {
        try {
            // Get the communityId passed through the Bundle
            String communityId = getArguments().getString("communityId", "Default ID");
            String owenerId = getArguments().getString("ownerId", "Default ID");

            // Parse the response
            String[] rows = response.split(":");
            HashMap<String, Integer> dateCountMap = new HashMap<>();

            for (String row : rows) {
                String[] fields = row.split(";");
                if (fields.length >= 14) {
                    String chatId = fields[2]; // Assuming ChatID is at index 2
                    String dateJoined = fields[13]; // Assuming date_joined is at index 13
                    String userId = fields[1]; // Assuming userId is at index 1

                    // Only process if the chatId matches the bundled communityId
                    if (chatId.equals(communityId)) {
                        // Only increment count if the ownerId does not match userId
                        if (!userId.equals(owenerId)) {
                            // Increment count for the date
                            dateCountMap.put(dateJoined, dateCountMap.getOrDefault(dateJoined, 0) + 1);
                        }
                    }
                }
            }

            // Prepare date labels dynamically (1 month range from current date)
            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> dateLabels = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -14); // Start 1 month before today
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            int index = 0;
            for (int i = 0; i < 15; i++) {
                String currentDate = sdf.format(calendar.getTime());
                dateLabels.add(currentDate);

                // Add entry for the current date
                int count = dateCountMap.getOrDefault(currentDate, 0);
                entries.add(new Entry(index, count));

                calendar.add(Calendar.DATE, 1); // Move to the next day
                index++;
            }

            // Create a LineDataSet
            LineDataSet lineDataSet = new LineDataSet(entries, "People Joined (per day)");
            lineDataSet.setColor(Color.BLUE);
            lineDataSet.setCircleColor(Color.RED);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setCircleRadius(4f);
            lineDataSet.setValueTextSize(10f);

            // Customize chart axes
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            lineChart.getXAxis().setLabelRotationAngle(-45);
            xAxis.setGranularity(1f); // Ensure one date per tick

            // Set custom X-Axis date formatter
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < dateLabels.size()) {
                        return dateLabels.get(index);
                    } else {
                        return "";
                    }
                }
            });

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setGranularity(1f);

            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(false); // Disable right Y-axis

            // Set data to the chart
            LineData lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);

            // Refresh the chart
            lineChart.invalidate();

        } catch (Exception e) {
            Log.e("ViewGraphFragment", "Error processing response: " + e.getMessage());
        }
    }
}
