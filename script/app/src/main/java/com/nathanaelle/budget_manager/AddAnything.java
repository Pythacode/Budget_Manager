package com.nathanaelle.budget_manager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddAnything extends DialogFragment {

    public static AddAnything newInstance(String budgetName, String table) {
        AddAnything fragment = new AddAnything();
        Bundle args = new Bundle();
        args.putString("type", budgetName);
        args.putString("table", table);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Pour prendre toute la largeur, ou MATCH_PARENT / WRAP_CONTENT selon besoin
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_anything, container, false);

        String type = getArguments() != null ? getArguments().getString("type") : "Error";
        String table = getArguments() != null ? getArguments().getString("table") : "Error";

        String title = "";
        String libele = "";
        String value = "";

        Button submitButton = view.findViewById(R.id.submit);
        DBHandler dbHandler = new DBHandler(requireContext());
        SQLiteDatabase db = dbHandler.getWritableDatabase();


        assert type != null;
        if (type.equals(getString(R.string.add_gain))) {
            title = getString(R.string.add_gain);
            libele = getString(R.string.libele);
            value = getString(R.string.value_gain);

            submitButton.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    TextInputLayout libeleInput = getView().findViewById(R.id.libeleInput);
                    String libeleText = libeleInput.getEditText().getText().toString();

                    TextInputLayout valueInput = getView().findViewById(R.id.valueInput);
                    String valueText = valueInput.getEditText().getText().toString();

                    if (!libeleText.equals("") && !valueText.equals("")) {

                        float valueInt = Float.parseFloat(valueText);


                        // Formater la date du jour
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDate = sdf.format(new Date());

                        // Préparer les données à insérer
                        ContentValues values = new ContentValues();
                        values.put("libele", libeleText);
                        values.put("date", currentDate);
                        values.put("value", valueInt);

                        // Insertion dans la base de données
                        SQLiteDatabase db = dbHandler.getWritableDatabase();
                        assert table != null;
                        long id = db.insert(table.replace(' ', '_'), null, values);

                        // Vérification
                        if (id != -1) {
                            dismiss();

                            int OLDvalue = 0;
                            Cursor cursor = db.rawQuery("SELECT value FROM budgets WHERE name = ?", new String[]{table});

                            if (cursor.moveToFirst()) {
                                OLDvalue = cursor.getInt(0);  // récupère la première colonne de la première ligne
                            }
                            cursor.close();

                            String sql = "UPDATE budgets SET value = ? WHERE name = ?";
                            db.execSQL(sql, new Object[]{OLDvalue + valueInt, table});
                            Main_fragment fragment = (Main_fragment) getParentFragmentManager().findFragmentById(R.id.fragment_container_view);
                            if (fragment != null) {
                                Log.d("RL", "Start rechargemnet");
                                fragment.updateDataAnotherFragment(table);
                            } else {
                                Log.e("RL", "Echec du rechargement.");
                            }

                            Log.d("DB", "Insertion réussie, ID = " + id);
                        } else {
                            Toast.makeText(requireContext(), "Une érreur s'est produite", Toast.LENGTH_SHORT).show();
                            Log.e("DB", "Échec de l'insertion !");
                        }
                    }

                }

            });


        } else if (type.equals(getString(R.string.add_expense))) {
            title = getString(R.string.add_expense);
            libele = getString(R.string.libele);
            value = getString(R.string.value_expense);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextInputLayout libeleInput = getView().findViewById(R.id.libeleInput);
                    String libeleText = libeleInput.getEditText().getText().toString();

                    TextInputLayout valueInput = getView().findViewById(R.id.valueInput);
                    String valueText = valueInput.getEditText().getText().toString();

                    if (!libeleText.equals("") && !valueText.equals("")) {

                        float valueInt = Float.parseFloat(valueText);


                        // Formater la date du jour
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDate = sdf.format(new Date());

                        // Préparer les données à insérer
                        ContentValues values = new ContentValues();
                        values.put("libele", libeleText);
                        values.put("date", currentDate);
                        values.put("value", -valueInt);

                        // Insertion dans la base de données
                        SQLiteDatabase db = dbHandler.getWritableDatabase();
                        assert table != null;
                        long id = db.insert(table.replace(' ', '_'), null, values);

                        // Vérification
                        if (id != -1) {
                            dismiss();

                            float OLDvalue = 0;
                            Cursor cursor = db.rawQuery("SELECT value FROM budgets WHERE name = ?", new String[]{table});

                            if (cursor.moveToFirst()) {
                                OLDvalue = cursor.getFloat(0);  // récupère la première colonne de la première ligne
                            }
                            cursor.close();

                            String sql = "UPDATE budgets SET value = ? WHERE name = ?";
                            db.execSQL(sql, new Object[]{OLDvalue - valueInt, table});
                            Main_fragment fragment = (Main_fragment) getParentFragmentManager().findFragmentById(R.id.fragment_container_view);
                            if (fragment != null) {
                                Log.d("RL", "Start rechargemnet");
                                fragment.updateDataAnotherFragment(table);
                            } else {
                                Log.e("RL", "Echec du rechargement.");
                            }

                            Log.d("DB", "Insertion réussie, ID = " + id);
                        } else {
                            Toast.makeText(requireContext(), "Une érreur s'est produite", Toast.LENGTH_SHORT).show();
                            Log.e("DB", "Échec de l'insertion !");
                        }
                    }

                }
            });

        } else if (type.equals(getString(R.string.add_budget))) {
            title = getString(R.string.add_budget);
            libele = getString(R.string.libele_budget);
            value = getString(R.string.value_budget);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextInputLayout tableInput = getView().findViewById(R.id.libeleInput);
                    String table = tableInput.getEditText().getText().toString().replace(' ', '_');

                    TextInputLayout valueInput = getView().findViewById(R.id.valueInput);
                    String valueText = valueInput.getEditText().getText().toString();

                    if (!table.equals("") && !valueText.equals("")) {

                        float valueInt = Float.parseFloat(valueText);

                        SQLiteDatabase db = dbHandler.getWritableDatabase();

                        String createTable = "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "libele TEXT NOT NULL, " +
                                "date TEXT NOT NULL, " +
                                "value REAL NOT NULL);";

                        db.execSQL(createTable);

                        Toast.makeText(getContext(), "Table créée avec succès", Toast.LENGTH_SHORT).show();



                        // Formater la date du jour
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDate = sdf.format(new Date());

                        // Préparer les données à insérer
                        ContentValues values = new ContentValues();
                        values.put("libele", "Montant initial");
                        values.put("date", currentDate);
                        values.put("value", valueInt);

                        long id = db.insert(table, null, values);

                        // Préparer les données à insérer
                        ContentValues valuesMaintable = new ContentValues();
                        valuesMaintable.put("name", table.replace('_', ' '));
                        valuesMaintable.put("value", valueInt);

                        id = db.insert("budgets", null, valuesMaintable);

                        // Vérification
                        if (id != -1) {
                            dismiss();

                            Main_fragment fragment = (Main_fragment) getParentFragmentManager().findFragmentById(R.id.fragment_container_view);
                            if (fragment != null) {
                                Log.d("RL", "Start rechargemnet");
                                fragment.updateDataAnotherFragment(table.replace('_', ' '));
                            } else {
                                Log.e("RL", "Echec du rechargement.");
                            }

                            Log.d("DB", "Insertion réussie, ID = " + id);
                        } else {
                            Toast.makeText(requireContext(), "Une érreur s'est produite", Toast.LENGTH_SHORT).show();
                            Log.e("DB", "Échec de l'insertion !");
                        }
                    }

                }
            });
        }

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(title);
        TextView libelView = view.findViewById(R.id.popup_libele);
        libelView.setText(libele);
        TextView valueView = view.findViewById(R.id.popup_value);
        valueView.setText(value);

        return view;
    }
}
