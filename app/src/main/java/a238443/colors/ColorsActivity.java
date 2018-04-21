package a238443.colors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class ColorsActivity extends AppCompatActivity{
    ColorsAdapter colorAdapter;
    GridView gridView;
    ArrayList<Integer> savedColors;
    TextView redView, greenView, blueView, alphaView, hexView, preview;
    Toolbar colorsToolbar;
    Button deleteButton;
    int selectedColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colors);
        getData();
        setupElements();

        colorAdapter = new ColorsAdapter(getApplicationContext());
        gridView.setAdapter(colorAdapter);
        colorAdapter.addDatabase(savedColors);

        colorsToolbar.setNavigationIcon(R.drawable.ic_action_back);
        setupListeners();

        if(selectedColor != (-1)) {
            int position = savedInstanceState.getInt("selected");
            long id = savedInstanceState.getLong("id");
            gridView.performItemClick(gridView.getChildAt(position), position, id);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putIntegerArrayList("saved", savedColors);
        savedInstanceState.putInt("selected",selectedColor);
        savedInstanceState.putInt("position",gridView.getSelectedItemPosition());
        savedInstanceState.putLong("id",gridView.getSelectedItemId());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        colorAdapter.addDatabase(savedInstanceState.getIntegerArrayList("saved"));
        selectedColor = savedInstanceState.getInt("selected");

        if(selectedColor != (-1))
            fillViews(selectedColor);

    }

    private void getData() {
        if(savedColors == null) {
            Intent fromMain = getIntent();
            savedColors = fromMain.getIntegerArrayListExtra("saved");
        }
    }

    private void setupElements() {
        redView = findViewById(R.id.red_value);
        greenView = findViewById(R.id.green_value);
        blueView = findViewById(R.id.blue_value);
        hexView = findViewById(R.id.hex_value);
        preview = findViewById(R.id.color_preview);
        alphaView = findViewById(R.id.alpha_value);
        gridView = findViewById(R.id.colors_grid);
        colorsToolbar = findViewById(R.id.colors_toolbar);
        deleteButton = findViewById(R.id.delete_button);
    }

    private void fillViews(int selectedColor) {
        String toDisplay = "R: " + Color.red(selectedColor);
        redView.setText(toDisplay);

        toDisplay = "G: " + Color.green(selectedColor);
        greenView.setText(toDisplay);

        toDisplay = "B: " + Color.blue(selectedColor);
        blueView.setText(toDisplay);

        toDisplay = "A: " + Color.alpha(selectedColor);
        alphaView.setText(toDisplay);

        toDisplay = "#" + Integer.toHexString(selectedColor).toUpperCase();
        hexView.setText(toDisplay);

        preview.setBackgroundColor(selectedColor);
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        colorsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToMain = new Intent();
                returnToMain.putExtra("result",savedColors);
                setResult(Activity.RESULT_OK, returnToMain);
                finish();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                selectedColor = savedColors.get(position);
                fillViews(selectedColor);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        colorAdapter.removeItem(position);
                        preview.setBackgroundColor(Color.WHITE);
                        alphaView.setText("");
                        blueView.setText("");
                        greenView.setText("");
                        redView.setText("");
                        hexView.setText("");
                        deleteButton.setVisibility(View.INVISIBLE);
                        selectedColor = -1;
                    }
                });
            }
        });


    }
}
