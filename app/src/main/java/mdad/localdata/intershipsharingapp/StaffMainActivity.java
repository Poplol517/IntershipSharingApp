package mdad.localdata.intershipsharingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StaffMainActivity extends AppCompatActivity {
    //change this ip address to your machine ip address, or you can use your atspace web address.
    public static String ipBaseAddress = "http://172.30.60.206/project";
    Button btnViewItem;
    Button btnNewProduct;
    Button btnLogout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);
// Buttons
        btnViewItem= (Button) findViewById(R.id.btnViewItem);
        btnNewProduct = (Button) findViewById(R.id.btnCreateProduct);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Launching All products Activity
             Intent i = new Intent(getApplicationContext(), AllUserActivity.class);
             startActivity(i);
            }
        });
        // view products click event
        btnNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create New Product Activity
                Intent i = new Intent(getApplicationContext(), NewUserActivity.class);
                startActivity(i);
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create New Product Activity
                logoutUser();
            }
        });
    }
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void logoutUser() {
        // Clear the login session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clears all saved session data
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        navigateToLogin();
    }
}