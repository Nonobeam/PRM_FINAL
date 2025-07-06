package per.nonobeam.phucnhse183026.myapplication.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ShopDB";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
        db.execSQL("CREATE TABLE Products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL, `desc` TEXT, quantity INTEGER, sold INTEGER)");
        db.execSQL("CREATE TABLE Cart(id INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, quantity INTEGER)");

        Random random = new Random();
        for (int i = 1; i <= 100; i++) {
            String name = "Product " + i;
            String desc = "Description for " + name;

            double price = 5 + (100 - 5) * random.nextDouble();
            price = Math.round(price * 100.0) / 100.0;

            int quantity = random.nextInt(91) + 10;
            int sold = random.nextInt(quantity + 1);

            String sql = "INSERT INTO Products (name, price, desc, quantity, sold) VALUES (" +
                    "'" + name + "', " +
                    price + ", " +
                    "'" + desc + "', " +
                    quantity + ", " +
                    sold + ");";

            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Products");
        db.execSQL("DROP TABLE IF EXISTS Cart");
        onCreate(db);
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("Users", null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Products", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double price = cursor.getDouble(2);
                String desc = cursor.getString(3);
                int quantity = cursor.getInt(4);
                int sold = cursor.getInt(5);

                list.add(new Product(id, name, desc, price, quantity, sold));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Products WHERE id = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            return new Product(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(3),
                    cursor.getDouble(2),
                    cursor.getInt(4),
                    cursor.getInt(5)
            );
        }
        cursor.close();
        return null;
    }

    public boolean addToCart(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor check = db.rawQuery("SELECT * FROM Cart WHERE productId = ?", new String[]{String.valueOf(productId)});
        if (check.getCount() > 0) {
            check.close();
            return false;
        }
        check.close();
        ContentValues values = new ContentValues();
        values.put("productId", productId);
        values.put("quantity", quantity);
        long result = db.insert("Cart", null, values);
        return result != -1;
    }

    public List<Product> getCartProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT p.id, p.name, p.`desc`, p.price, c.quantity " +
                        "FROM Cart c JOIN Products p ON c.productId = p.id", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String desc = cursor.getString(2);
                double price = cursor.getDouble(3);
                int qtyInCart = cursor.getInt(4);
                list.add(new Product(id, name, desc, price, qtyInCart, 0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}
