package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ViewUserStatFragment extends Fragment {

    private static final String urlViewAllUser = StaffMainActivity.ipBaseAddress + "/get_all_user.php";

    private PieChart pieChart;
    private BarChart barChart;

    public ViewUserStatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_user_stat, container, false);

        // Initialize PieChart and BarChart
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        // Populate the charts
        setupPieChart();
        setupBarChart();

        return view;
    }

    private void setupPieChart() {

        // Make the network request to fetch user data
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlViewAllUser, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Parse the PHP response
                String[] records = response.split(":");
                int studentCount = 0;
                int alumniCount = 0;
                int staffCount = 0;

                // Count the number of students and alumni
                for (String record : records) {
                    String[] fields = record.split(";");
                    if (fields.length >= 10) {
                        String category = fields[9];
                        if ("Student".equalsIgnoreCase(category)) {
                            studentCount++;
                        } else if ("Alumni".equalsIgnoreCase(category)) {
                            alumniCount++;
                        } else if ("Staff".equalsIgnoreCase(category)) {
                            staffCount++;
                        }
                    }
                }

                // Create entries for the pie chart
                ArrayList<PieEntry> entries = new ArrayList<>();
                entries.add(new PieEntry(studentCount, "Student"));
                entries.add(new PieEntry(alumniCount, "Alumni"));
                entries.add(new PieEntry(staffCount, "Staff"));

                // Setup PieDataSet
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(new int[]{Color.BLUE, Color.RED, Color.GREEN});
                dataSet.setValueTextSize(14f);
                dataSet.setValueTextColor(Color.WHITE);
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.format("%.1f%%", value);
                    }
                });

                // Setup PieData
                PieData data = new PieData(dataSet);

                // Configure the pie chart
                pieChart.setData(data);
                pieChart.setUsePercentValues(true);
                pieChart.setEntryLabelTextSize(12f);
                pieChart.setCenterText("User Distribution");
                pieChart.setCenterTextSize(18f);
                pieChart.animateY(1000);
                pieChart.getDescription().setEnabled(false);
                pieChart.invalidate(); // Refresh the chart

                // Set listener for chart clicks
                pieChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                        if (e instanceof PieEntry) {
                            PieEntry pieEntry = (PieEntry) e;
                            String label = pieEntry.getLabel();

                            // Create an Intent to navigate to the next activity
                            Intent intent = new Intent(requireContext(), ViewPieDetailActivity.class);
                            intent.putExtra("category", label); // Pass category (e.g., "Students")
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onNothingSelected() {
                        // No action needed
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error if the request fails
            }
        });

        // Add the request to the Volley queue
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }


    private void setupBarChart() {
        // Make the network request to fetch user data
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlViewAllUser, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Parse the PHP response
                String[] records = response.split(":");
                TreeMap<String, Integer> dateCounts = new TreeMap<>(); // TreeMap for sorted dates

                // Count occurrences of each date_joined
                for (String record : records) {
                    String[] fields = record.split(";");
                    if (fields.length >= 12) { // Ensure the field index exists
                        String dateJoined = fields[11];
                        if (!dateJoined.isEmpty()) {
                            dateCounts.put(dateJoined, dateCounts.getOrDefault(dateJoined, 0) + 1);
                        }
                    }
                }

                // Prepare entries for the bar chart
                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                int index = 0;

                for (Map.Entry<String, Integer> entry : dateCounts.entrySet()) {
                    entries.add(new BarEntry(index, entry.getValue()));
                    labels.add(entry.getKey());
                    index++;
                }

                // Setup BarDataSet
                BarDataSet dataSet = new BarDataSet(entries, "Users Joined");
                dataSet.setColors(new int[]{Color.BLUE});
                dataSet.setValueTextSize(14f);

                // Setup BarData
                BarData data = new BarData(dataSet);
                data.setBarWidth(0.7f);

                // Configure the bar chart
                barChart.setData(data);
                barChart.setFitBars(true);
                barChart.animateY(1000);

                // Setup x-axis labels
                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChart.getXAxis().setLabelRotationAngle(-45); // Rotate labels for better readability
                barChart.getXAxis().setGranularity(1f);
                barChart.getXAxis().setGranularityEnabled(true);
                barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart.getXAxis().setTextSize(8f); // Smaller font size for x-axis labels
                barChart.setExtraBottomOffset(30f);
                barChart.getDescription().setEnabled(false);
                barChart.getLegend().setEnabled(false);
                barChart.invalidate(); // Refresh the chart
                barChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                        if (e instanceof BarEntry) {
                            // Get the x-axis index of the selected bar
                            int index = (int) h.getX(); // Highlight contains the x index

                            // Use the index to get the corresponding x-axis label
                            String selectedLabel = labels.get(index); // Labels array from your code


                            // Create an Intent to navigate to the next activity
                            Intent intent = new Intent(requireContext(), ViewBarDetailActivity.class);
                            intent.putExtra("category", selectedLabel); // Pass the selected x-axis label
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onNothingSelected() {
                        // No action needed
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error if the request fails
            }
        });

        // Add the request to the Volley queue
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }
}