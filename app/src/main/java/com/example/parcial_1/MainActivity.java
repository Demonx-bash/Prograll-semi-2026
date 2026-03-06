package com.example.parcial_1;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- CONFIGURACIÓN TABHOST ---
        TabHost host = findViewById(android.R.id.tabhost);
        host.setup();

        TabHost.TabSpec spec1 = host.newTabSpec("Agua");
        spec1.setContent(R.id.tabAgua);
        spec1.setIndicator("Agua Potable");
        host.addTab(spec1);

        TabHost.TabSpec spec2 = host.newTabSpec("Area");
        spec2.setContent(R.id.tabArea);
        spec2.setIndicator("Área");
        host.addTab(spec2);

        // Estilizar títulos de pestañas
        for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(14);
        }

        // --- LÓGICA AGUA POTABLE ---
        EditText etConsumo = findViewById(R.id.etConsumo);
        Button btnAgua = findViewById(R.id.btnCalcularAgua);
        TextView tvResAgua = findViewById(R.id.tvResAgua);

        btnAgua.setOnClickListener(v -> {
            String val = etConsumo.getText().toString();
            if(val.isEmpty()) return;

            double m = Double.parseDouble(val);
            double total;

            if (m <= 18) {
                total = 6.00;
            } else if (m <= 28) {
                total = 6.00 + (m - 18) * 0.45;
            } else {
                // Ejemplo: 38m -> 6 (fijo) + 4.50 (tramo 2) + (10 * 0.65) = 17.00
                total = 6.00 + 4.50 + (m - 28) * 0.65;
            }
            tvResAgua.setText("Total a Pagar: $" + String.format("%.2f", total));
        });

        // --- LÓGICA CONVERSOR UNIVERSAL ---
        EditText etAreaVal = findViewById(R.id.etValorArea);
        Spinner spDe = findViewById(R.id.spDeUnidad);
        Spinner spA = findViewById(R.id.spAUnidad);
        Button btnArea = findViewById(R.id.btnConvertirArea);
        TextView tvResArea = findViewById(R.id.tvResArea);

        String[] unidades = {"Pie²", "Vara²", "Yarda²", "Metro²", "Tarea", "Manzana", "Hectárea"};

        // Factores: Cuántos metros cuadrados tiene cada unidad
        double[] factores = {
                0.092903,   // Pie cuadrado
                0.698745,   // Vara cuadrada (ES)
                0.836127,   // Yarda cuadrada
                1.0,        // Metro cuadrado
                439.39,     // Tarea (ES)
                6987.45,    // Manzana (ES)
                10000.0     // Hectárea
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unidades);
        spDe.setAdapter(adapter);
        spA.setAdapter(adapter);

        btnArea.setOnClickListener(v -> {
            String val = etAreaVal.getText().toString();
            if(val.isEmpty()) return;

            double cantidad = Double.parseDouble(val);
            int deIdx = spDe.getSelectedItemPosition();
            int aIdx = spA.getSelectedItemPosition();

            // Paso 1: Convertir entrada a metros cuadrados
            double m2 = cantidad * factores[deIdx];
            // Paso 2: Convertir de metros cuadrados a destino
            double resultado = m2 / factores[aIdx];

            tvResArea.setText("Resultado: " + String.format("%.4f", resultado));
        });
    }
}