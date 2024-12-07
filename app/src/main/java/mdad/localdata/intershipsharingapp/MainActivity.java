package mdad.localdata.intershipsharingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //change this ip address to your machine ip address, or you can use your atspace web address.
    public static String ipBaseAddress = "http://192.168.0.19/project";
    Button btnViewItem;
    Button btnNewProduct;
    Button btnLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Buttons
        btnViewItem= (Button) findViewById(R.id.btnViewItem);
        btnNewProduct = (Button) findViewById(R.id.btnCreateProduct);
        btnLogin = (Button) findViewById(R.id.btnLogin);
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
        // view products click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create New Product Activity
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }
}