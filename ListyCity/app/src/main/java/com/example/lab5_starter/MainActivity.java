package com.example.lab5_starter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private Button addCityButton, deleteCityButton;
    private boolean deleteMode = false;
    private ListView cityListView;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            deleteMode = !deleteMode;
            if (deleteMode) {
                deleteCityButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f7364a")));
                Toast.makeText(this, "Select a city to delete", Toast.LENGTH_SHORT).show();
            } else {
                exitDeleteMode();
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (!deleteMode) {
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(), "City Details");
            } else {
                deleteCity(city);
            }
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");
        citiesRef.addSnapshotListener((value,error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }

        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
        docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    public void deleteCity(City city) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();
        exitDeleteMode();
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firstore", "Document added");
                    }
                });

    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }

    private void exitDeleteMode() {
        deleteMode = false;
        deleteCityButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff6750a4")));
    }
}