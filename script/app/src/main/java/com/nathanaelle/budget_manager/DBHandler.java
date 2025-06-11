package com.nathanaelle.budget_manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    // Nom et version de la base
    private static final String DB_NAME = "BudgetManager.db";
    private static final int DB_VERSION = 2;

    // Constructeur
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Appelé à la première création de la base
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Créer les tables ici
        String createBudgetsTable = "CREATE TABLE IF NOT EXISTS budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "value FLOAT NOT NULL," +
                "defaultBudget INTEGER DEFAULT FALSE);";

        String createArgentTable = "CREATE TABLE IF NOT EXISTS `Argent_de_poche` (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "libele TEXT NOT NULL, " +
                "date DATE NOT NULL," +
                "value FLOAT NOT NULL);";

        db.execSQL(createBudgetsTable);
        db.execSQL(createArgentTable);

        String insertDefaultBudget = "INSERT INTO budgets (name, value, defaultBudget) VALUES " +
                "('Argent de poche', 0, 1);";

        db.execSQL(insertDefaultBudget);
    }

    // Appelé si la version change
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Exemple simple : on supprime et on recrée
        db.execSQL("DROP TABLE IF EXISTS categorie");
        onCreate(db);
    }
}
