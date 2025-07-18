package per.nonobeam.phucnhse183026.myapplication.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import per.nonobeam.phucnhse183026.myapplication.model.Order;
import per.nonobeam.phucnhse183026.myapplication.model.OrderItem;
import per.nonobeam.phucnhse183026.myapplication.model.Product;
import per.nonobeam.phucnhse183026.myapplication.model.ChatMessage;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ShopDB";
    private static final int DB_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
        db.execSQL("CREATE TABLE Products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL, `desc` TEXT, quantity INTEGER, sold INTEGER)");
        db.execSQL("CREATE TABLE Cart(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, quantity INTEGER)");
        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id TEXT UNIQUE," +
                "user_id TEXT," +
                "amount REAL," +
                "total_fee REAL," +
                "order_date INTEGER," +
                "status TEXT" +
                ")");
        db.execSQL("CREATE TABLE order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id TEXT," +
                "product_id TEXT," +
                "product_name TEXT," +
                "quantity INTEGER," +
                "price REAL," +
                "total_price REAL," +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                ")");
        db.execSQL("CREATE TABLE Messages(id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, message TEXT, timestamp INTEGER)");
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
        ContentValues values = new ContentValues();
        values.put("username", "admin");
        values.put("password", "123456");
        db.insert("Users", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Products");
        db.execSQL("DROP TABLE IF EXISTS Cart");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_items");
        db.execSQL("DROP TABLE IF EXISTS Messages");
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

    public int checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Users WHERE username = ? AND password = ?", new String[]{username, password});
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        } else {
            cursor.close();
            return -1; // Invalid login
        }
    }

    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String username = null;
        try {
            cursor = db.rawQuery("SELECT username FROM Users WHERE id = ?", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(0);
            } else {
                Log.e("DatabaseHelper", "No user found with ID: " + userId);  // Log if no user found
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (username == null) {
            Log.e("ChatActivity", "Sender name not found for userId: " + userId);  // Log error if null
        }
        return username;
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

    public boolean addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Product product = getProductById(productId);
        if (product.quantity <= 0)
            return false;
        Cursor check = db.rawQuery("SELECT quantity FROM Cart WHERE userId = ? AND productId = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        if (check.getCount() > 0) {
            check.moveToFirst();
            int existingQuantity = check.getInt(0);
            int newQuantity = existingQuantity + quantity;
            check.close();
            ContentValues values = new ContentValues();
            values.put("quantity", newQuantity);
            int result = db.update("Cart", values, "userId = ? AND productId = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            return result > 0;
        } else {
            check.close();
            ContentValues values = new ContentValues();
            values.put("userId", userId);
            values.put("productId", productId);
            values.put("quantity", quantity);

            long result = db.insert("Cart", null, values);
            return result != -1;
        }
    }

    public int getCartQuantity(int userId, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT quantity FROM Cart WHERE userId = ? AND productId = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            int qty = cursor.getInt(0);
            cursor.close();
            return qty;
        } else {
            cursor.close();
            return 0;
        }
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

    public List<Product> getCartItems(int userId) {
        List<Product> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Fixed query with correct column names
        String query = "SELECT p.id, p.name, p.`desc`, p.price, p.quantity, p.sold, c.quantity as cart_quantity " +
                "FROM Products p " +
                "INNER JOIN Cart c ON p.id = c.productId " +
                "WHERE c.userId = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                // Use column index constants to avoid -1 errors
                int idIdx = cursor.getColumnIndexOrThrow("id");
                int nameIdx = cursor.getColumnIndexOrThrow("name");
                int descIdx = cursor.getColumnIndexOrThrow("desc");  // Changed from "description"
                int priceIdx = cursor.getColumnIndexOrThrow("price");
                int cartQtyIdx = cursor.getColumnIndexOrThrow("cart_quantity");
                int soldIdx = cursor.getColumnIndexOrThrow("sold");

                Product product = new Product(
                        cursor.getInt(idIdx),
                        cursor.getString(nameIdx),
                        cursor.getString(descIdx),
                        cursor.getDouble(priceIdx),
                        cursor.getInt(cartQtyIdx),  // This should be the cart quantity
                        cursor.getInt(soldIdx)
                );
                cartItems.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartItems;
    }

    // DatabaseHelper.java
    public boolean updateCartItemQuantity(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Product product = getProductById(productId);
            if (product == null || product.quantity < quantity) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put("quantity", quantity);

            int result = db.update("cart", values,
                    "user_id = ? AND product_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});

            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating cart quantity", e);
            return false;
        } finally {
            db.close();
        }
    }
    public boolean deleteCartItem(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Cart", "userId = ? AND productId = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}) > 0;
    }

    public boolean processCheckout(List<Product> selectedProducts, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Product product : selectedProducts) {
                Product currentProduct = getProductById(product.id);
                if (currentProduct == null) {
                    throw new Exception("Product not found: " + product.id);
                }

                if (currentProduct.quantity < product.quantity) {
                    throw new Exception("Not enough quantity for product: " + product.name);
                }

                ContentValues productValues = new ContentValues();
                productValues.put("sold", currentProduct.sold + product.quantity);
                productValues.put("quantity", currentProduct.quantity - product.quantity);

                int rowsUpdated = db.update("Products", productValues, "id = ?",
                        new String[]{String.valueOf(product.id)});

                if (rowsUpdated == 0) {
                    throw new Exception("Failed to update product: " + product.id);
                }

                // Remove from Cart
                db.delete("Cart", "productId = ? AND userId = ?",
                        new String[]{String.valueOf(product.id), userId});
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean saveOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Lưu Order
            ContentValues orderValues = new ContentValues();
            orderValues.put("order_id", order.orderId);
            orderValues.put("user_id", order.userId);
            orderValues.put("amount", order.amount);
            orderValues.put("total_fee", order.totalFee);
            orderValues.put("order_date", order.orderDate);
            orderValues.put("status", order.status);

            long orderId = db.insert("orders", null, orderValues);

            if (orderId == -1) {
                return false;
            }

            // Lưu OrderItems
            for (OrderItem item : order.orderItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("order_id", order.orderId);
                itemValues.put("product_id", item.productId);
                itemValues.put("product_name", item.productName);
                itemValues.put("quantity", item.quantity);
                itemValues.put("price", item.price);
                itemValues.put("total_price", item.totalPrice);

                long itemId = db.insert("order_items", null, itemValues);
                if (itemId == -1) {
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saving order: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public List<Order> getUserOrders(String userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.orderId = getStringFromCursor(cursor, "order_id");
                order.userId = getStringFromCursor(cursor, "user_id");
                order.amount = getDoubleFromCursor(cursor, "amount");
                order.totalFee = getDoubleFromCursor(cursor, "total_fee");
                order.orderDate = getLongFromCursor(cursor, "order_date");
                order.status = getStringFromCursor(cursor, "status");

                order.orderItems = getOrderItems(order.orderId, db);

                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
    }

    private List<OrderItem> getOrderItems(String orderId, SQLiteDatabase db) {
        List<OrderItem> items = new ArrayList<>();

        String query = "SELECT * FROM order_items WHERE order_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{orderId});

        if (cursor.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
                item.productId = getIntFromCursor(cursor, "product_id");
                item.productName = getStringFromCursor(cursor, "product_name");
                item.quantity = getIntFromCursor(cursor, "quantity");
                item.price = getDoubleFromCursor(cursor, "price");
                item.totalPrice = getDoubleFromCursor(cursor, "total_price");

                items.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    private String getStringFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getString(index) : null;
    }

    private int getIntFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getInt(index) : 0;
    }

    private double getDoubleFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getDouble(index) : 0.0;
    }

    private long getLongFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getLong(index) : 0L;
    }

    private boolean getBooleanFromCursor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 && cursor.getInt(index) == 1;
    }

    public void addMessage(String sender, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("message", message);
        values.put("timestamp", System.currentTimeMillis());
        db.insert("Messages", null, values);
    }

    public List<ChatMessage> getAllMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Messages ORDER BY timestamp ASC", null);

        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage(
                        cursor.getString(1), // sender
                        cursor.getString(2), // message
                        cursor.getLong(3)    // timestamp
                );
                message.setId(cursor.getInt(0)); // id
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    public int getCartItemCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery("SELECT SUM(quantity) FROM Cart WHERE userId = ?", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);  // Get the count of items in the cart
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }
}
