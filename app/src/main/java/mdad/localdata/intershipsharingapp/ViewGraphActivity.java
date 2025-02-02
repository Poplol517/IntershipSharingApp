package mdad.localdata.intershipsharingapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.HashMap;

public class ViewGraphActivity extends AppCompatActivity {
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private LineChart lineChart;
    private String communityId;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_graph);

        // Retrieve data from Intent extras
        communityId = getIntent().getStringExtra("communityId");
        ownerId = getIntent().getStringExtra("ownerId");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize chart
        lineChart = findViewById(R.id.line_chart);
        fetchData();
    }

    private void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_get_userchat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        processResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ViewGraphActivity", "Error fetching data: " + error.getMessage());
                    }
                });
        queue.add(stringRequest);
    }

    private void processResponse(String response) {
        try {
            String[] rows = response.split(":");
            HashMap<String, Integer> dateCountMap = new HashMap<>();

            for (String row : rows) {
                String[] fields = row.split(";");
                if (fields.length >= 14) {
                    String chatId = fields[2];
                    String dateJoined = fields[13];
                    String userId = fields[1];

                    if (chatId.equals(communityId) && !userId.equals(ownerId)) {
                        dateCountMap.put(dateJoined, dateCountMap.getOrDefault(dateJoined, 0) + 1);
                    }
                }
            }

            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> dateLabels = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -14);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            int index = 0;
            for (int i = 0; i < 15; i++) {
                String currentDate = sdf.format(calendar.getTime());
                dateLabels.add(currentDate);
                int count = dateCountMap.getOrDefault(currentDate, 0);
                entries.add(new Entry(index, count));
                calendar.add(Calendar.DATE, 1);
                index++;
            }

            LineDataSet lineDataSet = new LineDataSet(entries, "People Joined (per day)");
            lineDataSet.setColor(Color.BLUE);
            lineDataSet.setCircleColor(Color.RED);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setCircleRadius(4f);
            lineDataSet.setValueTextSize(10f);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    return (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "";
                }
            });

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setGranularity(1f);
            lineChart.getAxisRight().setEnabled(false);

            lineChart.setData(new LineData(lineDataSet));
            lineChart.invalidate();
        } catch (Exception e) {
            Log.e("ViewGraphActivity", "Error processing response: " + e.getMessage());
        }
    }
}
