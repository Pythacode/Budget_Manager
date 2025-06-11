package com.nathanaelle.budget_manager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main_fragment extends Fragment {
    private LayoutInflater layoutInflater;

    public static Main_fragment newInstance() {
        return new Main_fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateDataAnotherFragment(String name) {
        if (getView() != null) {
            updateData(getView(), name);

        }
    }

    @SuppressLint("SetTextI18n")
    public void updateData(View view, String ActuName) {

        DecimalFormat df = new DecimalFormat("0.00");

        DBHandler dbHandler = new DBHandler(requireContext());
        SQLiteDatabase db = dbHandler.getWritableDatabase();

        TextView restBudgetValue = view.findViewById(R.id.rest_budget_value);
        Spinner choixSpinner = view.findViewById(R.id.choix_spinner);

        Cursor cursor = db.rawQuery("SELECT name, defaultBudget, value FROM budgets;", null);
        List<String> optionsList = new ArrayList<>();

        int defaultPosition = 0;
        int index = 0;

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                optionsList.add(name);
                if (ActuName.equals(name)) {
                    defaultPosition = index;
                    restBudgetValue.setText(df.format(cursor.getFloat(cursor.getColumnIndexOrThrow("value"))) + " €");

                    if (cursor.getFloat(cursor.getColumnIndexOrThrow("value")) < 0) {
                        restBudgetValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                    }
                }
                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        optionsList.add(getString(R.string.add_budget));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, optionsList.toArray(new String[0])
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choixSpinner.setAdapter(adapter);
        choixSpinner.setSelection(defaultPosition);

        choixSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View viewSelected, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals(getString(R.string.add_budget))) {
                    AddAnything popup = AddAnything.newInstance(getString(R.string.add_budget), selectedItem);
                    popup.show(getParentFragmentManager(), "budget_popup");
                } else {
                    LinearLayout parentLayout = requireView().findViewById(R.id.ScrolView_operation);
                    parentLayout.removeAllViews();

                    Cursor cursor = db.rawQuery("SELECT libele, value FROM " + selectedItem.replace(' ', '_') + ";", null);
                    if (cursor.moveToFirst()) {
                        do {
                            String libele = cursor.getString(cursor.getColumnIndexOrThrow("libele"));
                            float value = cursor.getFloat(cursor.getColumnIndexOrThrow("value"));

                            View defaultElement = layoutInflater.inflate(R.layout.default_operation, parentLayout, false);

                            TextView libeleText = defaultElement.findViewById(R.id.libele);
                            libeleText.setText(libele);

                            TextView valueText = defaultElement.findViewById(R.id.value);
                            valueText.setText(df.format(value) + " €");

                            if (value >= 0) {
                                valueText.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
                            }

                            parentLayout.addView(defaultElement);
                        } while (cursor.moveToNext());
                    }

                    ScrollView scrollView = requireView().findViewById(R.id.scroolView);;
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    cursor.close();

                    cursor = db.rawQuery("SELECT value FROM budgets WHERE name = '" + selectedItem + "';", null);

                    if (cursor.moveToFirst()) {
                        do {
                            float value = cursor.getFloat(cursor.getColumnIndexOrThrow("value"));
                            restBudgetValue.setText(df.format(value) + " €");

                            if (value < 0) {
                                restBudgetValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        updateData(view, "");

        Button add_gain = view.findViewById(R.id.button_gain);
        add_gain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner choixSpinner = view.findViewById(R.id.choix_spinner);
                String table = choixSpinner.getSelectedItem().toString();
                AddAnything popup = AddAnything.newInstance(getString(R.string.add_gain), table);
                popup.show(getParentFragmentManager(), "budget_popup");
            }
        });

        Button button_expense = view.findViewById(R.id.button_expense);
        button_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner choixSpinner = view.findViewById(R.id.choix_spinner);
                String table = choixSpinner.getSelectedItem().toString();
                AddAnything popup = AddAnything.newInstance(getString(R.string.add_expense), table);
                popup.show(getParentFragmentManager(), "budget_popup");
            }
        });

        Button button_reinitialise = view.findViewById(R.id.button_reinitialisation);
        button_reinitialise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Réinitialiser l'application")
                        .setMessage("Voulez-vous vraiment reinitialiser l'app ?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            requireContext().deleteDatabase("BudgetManager.db");
                            requireActivity().finishAffinity();
                            System.exit(0);
                        })
                        .setNegativeButton("Non", null)
                        .show();
            }
        });

        return view;

    }
}
