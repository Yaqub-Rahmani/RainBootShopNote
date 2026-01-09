package com.example.rainbootshopnote.databaseConfiger;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.IntegerRes;
import androidx.annotation.Nullable;

import com.example.rainbootshopnote.Dto.CashDTO;
import com.example.rainbootshopnote.Dto.PDF.AvailableProductDTO;
import com.example.rainbootshopnote.Dto.ProductDTO;
import com.example.rainbootshopnote.Dto.UserDTO;
import com.example.rainbootshopnote.Dto.userDTOS.UserCashSummaryDTO;
import com.example.rainbootshopnote.entities.admins;
import com.example.rainbootshopnote.entities.deposits;
import com.example.rainbootshopnote.entities.insertProducts;
import com.example.rainbootshopnote.entities.products;
import com.example.rainbootshopnote.entities.sales;
import com.example.rainbootshopnote.entities.users;
import com.example.rainbootshopnote.entities.withdrawals;
import com.example.rainbootshopnote.utils.date.AppDate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class DBHelper extends SQLiteOpenHelper {

    String DB_adress = null;
    public static final String DB_name = "rainbootnote.db";
    private static final int DB_version = 1;
    private final Context myContext;

    //tables
    private static final String table_admins = "admins";
    private static final String table_deposit = "deposit";
    private static final String table_insertProducts = "insertProducts";
    private static final String table_products = "products";
    private static final String table_sales = "sales";
    private static final String table_users = "users";
    private static final String table_withdrawals = "withdrawals";

    //columns
    private static final String [] column_admins = {"admin_id", "name", "user_name", "user_password", "status", "deleteStatus"}; //"int admin_id", "name", "user_name", "user_password", "status", "delete"
    private static final String column_deposit [] = {"deposit_id", "name", "amount", "create_date", "update_date", "status", "description", "user_id","deleteStatus"}; //"deposit_id", "name", "amount", "create_date", "update_date", "status", "description", "user_id","delete"
    private static final String column_insertProducts [] = {"insert_id", "product_id", "create_date", "update_date", "quantity", "price", "description", "status","deleteStatus"}; //"insert_id", "product_id", "create_date", "update_date", "quantity, "price", "description", "status","delete"
    private static final String column_products [] = {"product_id", "code", "name", "price", "quantity", "status", "description", "deleteStatus"}; //"product_id, "code", "name", "price", "quantity", "status", "description", "delete"
    private static final String column_sales [] = {"sale_id", "product_id", "price", "quantity", "update_date", "create_date", "description", "status","deleteStatus"}; //"sale_id", "String "product_id", "float "price", " int "quantity", "update_date", "create_date", "description", "status","delete"
    private static final String column_users [] = {"user_id", "name", "mobile", "create_date", "update_date", "status", "description","deleteStatus"}; //"user_id", "name", "mobile", "create_date", "update_date", "status", "description","delete"
    private static final String column_withdrawals [] = {"withdrawal_id", "name", "amount", "create_date", "update_date", "user_id", "status", "description","deleteStatus"}; //"withdrawal_id", "name", "amount", "create_date", "update_date", "user_id", "status", "description","delete"


    //tables query string
    String createAdminsTable = "CREATE TABLE admins (" +
            "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "user_name TEXT, " +
            "user_password TEXT, " +
            "status BLOB, " +
            "deleteStatus BLOB" +
            ");";

    String createDepositsTable = "CREATE TABLE deposits (" +
            "deposit_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "amount REAL, " +
            "create_date TEXT, " +
            "update_date TEXT, " +
            "status BLOB, " +
            "description TEXT, " +
            "user_id INTEGER, " +
            "deleteStatus BLOB" +
            ");";

    String createInsertProductsTable = "CREATE TABLE insertProducts (" +
            "insert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "product_id INTEGER, " +
            "create_date TEXT, " +
            "update_date TEXT, " +
            "quantity INTEGER, " +
            "price REAL, " +
            "description TEXT, " +
            "status BLOB, " +
            "deleteStatus BLOB" +
            ");";

    String createSalesTable = "CREATE TABLE sales (" +
            "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "product_id TEXT, " +
            "price REAL, " +
            "win REAL, " +
            "quantity INTEGER, " +
            "create_date TEXT, " +
            "update_date TEXT, " +
            "description TEXT, " +
            "status BLOB, " +
            "deleteStatus BLOB" +
            ");";

    String createProductsTable = "CREATE TABLE products (" +
            "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "price REAL, " +
            "quantity INTEGER, " +
            "status BLOB, " +
            "description TEXT, " +
            "deleteStatus BLOB, " +
            "code TEXT" +
            ");";


    String createUsersTable = "CREATE TABLE users (" +
            "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "mobile TEXT, " +
            "address TEXT, " +
            "create_date TEXT, " +
            "update_date TEXT, " +
            "status BLOB, " +
            "description TEXT, " +
            "deleteStatus BLOB" +
            ");";

    String createWithdrawalsTable = "CREATE TABLE withdrawals (" +
            "withdrawal_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "amount REAL, " +
            "create_date TEXT, " +
            "update_date TEXT, " +
            "user_id INTEGER, " +
            "status BLOB, " +
            "description TEXT, " +
            "deleteStatus BLOB" +
            ");";


    String createTableQueries =
            createAdminsTable +
                    createDepositsTable +
                    createInsertProductsTable +
                    createProductsTable +
                    createSalesTable+
                    createUsersTable +
                    createWithdrawalsTable;

    public String getDatabasePath() {
        return DB_adress + DB_name;
    }

    public File backupDatabase(Context context) {
        File backupFile = null;
        try {
            // Veritabanı dosyasını bul
            File dbFile = context.getDatabasePath(DB_name);

            // Harici depolama yerine uygulamaya özel dizine yedekleme dosyasını oluştur
            backupFile = new File(context.getExternalFilesDir(null), "backup_" + DB_name);

            // Dosya giriş/çıkış akışlarını kullanarak veritabanını kopyala
            FileInputStream fis = new FileInputStream(dbFile);
            FileOutputStream fos = new FileOutputStream(backupFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            // Akışları kapat
            fos.flush();
            fos.close();
            fis.close();

        } catch (IOException e) {
            Log.e("Backup Error", "Error backing up database", e);
        }
        return backupFile; // Yedek dosyasını geri döndür
    }

    public DBHelper(@Nullable Context context) {
        // super(context, new File(Environment.getExternalStorageDirectory(),DB_name).toString(),null,DB_version);
        super(context,DB_name, null, DB_version);

        this.DB_adress = "data/data/" + context.getPackageName() + "/databases/";
        this.myContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createAdminsTable);
        sqLiteDatabase.execSQL(createDepositsTable);
        sqLiteDatabase.execSQL(createInsertProductsTable);
        sqLiteDatabase.execSQL(createProductsTable);
        sqLiteDatabase.execSQL(createSalesTable);
        sqLiteDatabase.execSQL(createUsersTable);
        sqLiteDatabase.execSQL(createWithdrawalsTable);

    }
    //score counter
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        if (newVersion > DB_version)
//            try {
//                copyDATABASE();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
    }

    //"admin_id", "name", "user_name", "user_password", "status", "delete"

    public boolean addAdmin(admins admin) {
        SQLiteDatabase sqLiteDatabase = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(column_admins[1], admin.getName());
            contentValues.put(column_admins[2], admin.getUser_name());
            contentValues.put(column_admins[3], admin.getUser_password());
            contentValues.put(column_admins[4], admin.getStatus());
            contentValues.put(column_admins[5], admin.getDeleteStatus());

            long result = sqLiteDatabase.insert(table_admins, null, contentValues);
            return result != -1; // Ekleme başarılı mı
        } catch (Exception e) {
            Log.e("Database Exception", "Error inserting admin data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    // Get All Admins
    public List<admins> getAllAdmins() {
        List<admins> adminList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM admins WHERE deleteStatus = false";

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    admins admin = new admins();
                    admin.setAdmin_id(Integer.parseInt(cursor.getString(0)));
                    admin.setName(cursor.getString(1));
                    admin.setUser_name(cursor.getString(2));
                    admin.setUser_password(cursor.getString(3));
                    admin.setStatus(Boolean.valueOf(cursor.getString(4)));
                    admin.setDeleteStatus(Boolean.valueOf(cursor.getString(5)));
                    adminList.add(admin);
                } while (cursor.moveToNext());

            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching data", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return adminList;
    }

    //Get last admin
    public admins getLastOrDefaultAdmin() {
        admins admin = null;
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            String query = "SELECT * FROM admins ORDER BY admin_id DESC LIMIT 1";

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                admin = new admins();
                admin.setAdmin_id(cursor.getInt(0));
                admin.setName(cursor.getString(1));
                admin.setUser_name(cursor.getString(2));
                admin.setUser_password(cursor.getString(3));
                admin.setStatus(Boolean.valueOf(cursor.getString(4)));
                admin.setDeleteStatus(Boolean.valueOf(cursor.getString(5)));
            } else {
                int length = 4;
                Random random = new Random();
                StringBuilder code = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    int digit = random.nextInt(9)+1;
                    code.append(digit);
                }

                admin = new admins();
                admin.setAdmin_id(-1);
                admin.setName("Yaqub Rahmani");
                admin.setUser_name("ABC");
                admin.setUser_password(code.toString());
                admin.setStatus(true);
                admin.setDeleteStatus(false);
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching data", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return admin;
    }

    // Get first admin
    public admins getFirstAdmin() {
        admins admin = null;
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            String query = "SELECT * FROM admins ORDER BY admin_id ASC LIMIT 1"; // İlk kaydı almak için ASC kullanıyoruz.

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                admin = new admins();
                admin.setAdmin_id(cursor.getInt(0));
                admin.setName(cursor.getString(1));
                admin.setUser_name(cursor.getString(2));
                admin.setUser_password(cursor.getString(3));
                admin.setStatus(Boolean.valueOf(cursor.getString(4)));
                admin.setDeleteStatus(Boolean.valueOf(cursor.getString(5)));
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching data", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return admin; // Eğer admin bulunmazsa null dönecektir.
    }


    // products tablosundan belirli bir ürünü almak için
    public products getProductById(int productId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        products product = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();
            // Belirli bir product_id'yi almak için sorgu
            String query = "SELECT * FROM products WHERE product_id = ? AND deleteStatus = false";

            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(productId)});

            // Eğer sonuç varsa ilk sonucu getir
            if (cursor.moveToFirst()) {
                product = new products();
                product.setProduct_id(Integer.parseInt(cursor.getString(0))); // product_id
                product.setName(cursor.getString(1)); // name
                product.setPrice(Float.parseFloat(cursor.getString(2))); // price
                product.setQuantity(Integer.parseInt(cursor.getString(3))); // quantity
                product.setStatus(Boolean.valueOf(cursor.getString(4))); // status
                product.setDescription(cursor.getString(5)); // description
                product.setDeleteStatus(Boolean.valueOf(cursor.getString(6))); // deleteStatus
                product.setCode(cursor.getString(7)); // code
            }
        } catch (Exception e) {
            Log.e("Database Error", "Product verisi alırken hata oluştu", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return product;
    }


    public boolean addProduct(products product) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Ürün ismini küçük harfe çevirip kontrol et
            String query = "SELECT * FROM products WHERE LOWER(name) = LOWER(?)";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{product.getName()});

            if (cursor != null && cursor.moveToFirst()) {
                // Aynı isimde birden fazla ürün olabilir, tümünü kontrol et
                do {
                    boolean deleteStatus = cursor.getInt(cursor.getColumnIndexOrThrow("deleteStatus")) == 1;
                    if (!deleteStatus) {
                        // Eğer herhangi bir ürünün deleteStatus değeri false ise eklemeyi durdur
                        return false;
                    }
                } while (cursor.moveToNext());

                // Tüm ürünler için deleteStatus true ise eklemeye izin ver
                BigDecimal priceBD = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
                float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat

                // Ürün ekleme işlemi
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", product.getName());
                contentValues.put("price", roundedPrice);
                contentValues.put("quantity", product.getQuantity());
                contentValues.put("status", product.isStatus());
                contentValues.put("description", product.getDescription());
                contentValues.put("deleteStatus", product.isDeleteStatus());
                contentValues.put("code", product.getCode());

                long result = sqLiteDatabase.insert("products", null, contentValues);
                return result != -1; // Ekleme başarılı mı
            } else {
                // Ürün yoksa, ekleme işlemi
                BigDecimal priceBD = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
                float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat

                // Ürün ekleme işlemi
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", product.getName());
                contentValues.put("price", roundedPrice);
                contentValues.put("quantity", product.getQuantity());
                contentValues.put("status", product.isStatus());
                contentValues.put("description", product.getDescription());
                contentValues.put("deleteStatus", product.isDeleteStatus());
                contentValues.put("code", product.getCode());

                long result = sqLiteDatabase.insert("products", null, contentValues);
                return result != -1; // Ekleme başarılı mı
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Error inserting or updating product data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }


    public boolean productUpdate(products product) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Ürün ismini kontrol et (kendi ismi dışında)
            String query = "SELECT * FROM products WHERE LOWER(name) = LOWER(?) AND product_id != ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{product.getName(), String.valueOf(product.getProduct_id())});

            if (cursor != null && cursor.moveToFirst()) {
                // Aynı isimde başka bir ürün var, deleteStatus kontrol et
                boolean deleteStatus = cursor.getInt(cursor.getColumnIndexOrThrow("deleteStatus")) == 1;
                if (deleteStatus) {
                    // deleteStatus true, güncellemeye izin ver
                    ContentValues contentValues = new ContentValues();

                    // Sadece güncellenecek alanları ekliyoruz
                    if (product.getPrice() > 0) {
                        // Fiyatı BigDecimal ile iki ondalık basamağa yuvarla
                        BigDecimal priceBD = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
                        float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat
                        contentValues.put("price", roundedPrice);
                    }

                    if (product.getQuantity() >= 0) {
                        contentValues.put("quantity", product.getQuantity());
                    }

                    contentValues.put("name", product.getName());
                    contentValues.put("status", product.isStatus());
                    contentValues.put("description", product.getDescription());
                    contentValues.put("deleteStatus", product.isDeleteStatus());
                    contentValues.put("code", product.getCode());

                    // Güncelleme işlemi
                    int rowsAffected = sqLiteDatabase.update("products", contentValues, "product_id = ?", new String[]{String.valueOf(product.getProduct_id())});
                    return rowsAffected > 0; // Güncelleme başarılıysa true döner
                } else {
                    // deleteStatus false, güncelleme yapılmasın
                    return false;
                }
            } else {
                // Ürün ismi kullanılabilir, direkt güncelle
                ContentValues contentValues = new ContentValues();

                // Sadece güncellenecek alanları ekliyoruz
                if (product.getPrice() > 0) {
                    // Fiyatı BigDecimal ile iki ondalık basamağa yuvarla
                    BigDecimal priceBD = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
                    float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat
                    contentValues.put("price", roundedPrice);
                }

                if (product.getQuantity() >= 0) {
                    contentValues.put("quantity", product.getQuantity());
                }

                contentValues.put("name", product.getName());
                contentValues.put("status", product.isStatus());
                contentValues.put("description", product.getDescription());
                contentValues.put("deleteStatus", product.isDeleteStatus());
                contentValues.put("code", product.getCode());

                // Güncelleme işlemi
                int rowsAffected = sqLiteDatabase.update("products", contentValues, "product_id = ?", new String[]{String.valueOf(product.getProduct_id())});
                return rowsAffected > 0; // Güncelleme başarılıysa true döner
            }
        } catch (Exception e) {
            e.printStackTrace(); // Hata durumunu logla
            return false; // Hata durumu
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }


    // Get All Products
    public List<products> getAllProducts() {
        List<products> productList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM products WHERE deleteStatus = false ORDER BY name ASC";


            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    products product = new products();
                    product.setProduct_id(Integer.parseInt(cursor.getString(0)));
                    product.setName(cursor.getString(1));
                    product.setPrice(cursor.getFloat(2)); // price için uygun metod
                    product.setQuantity(Integer.parseInt(cursor.getString(3))); // quantity için uygun metod
                    product.setStatus(Boolean.valueOf(cursor.getString(4))); // status için uygun metod
                    product.setDescription(cursor.getString(5)); // description için uygun metod
                    product.setDeleteStatus(Boolean.valueOf(cursor.getString(6))); // deleteStatus için uygun metod
                    product.setCode(cursor.getString(7)); // code için uygun metod
                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching data", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return productList;
    }

    // Get Total Price and Quantity Calculation
    public double getTotalPriceQuantity() {
        double totalPriceQuantity = 0;
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();

            // price * quantity toplamını hesaplayan SQL sorgusu
            String query = "SELECT SUM(price * quantity) AS totalPriceQuantity FROM products WHERE deleteStatus = false";

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                totalPriceQuantity = cursor.getDouble(0); // Sorgu sonucu toplamı alıyoruz
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching total price and quantity", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return totalPriceQuantity;
    }

    public boolean addInsertProduct(insertProducts insertProduct) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Ürün ID'sine göre kontrol et
            String query = "SELECT * FROM insertProducts WHERE insert_id = ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(insertProduct.getInsert_id())});

            if (cursor != null && cursor.moveToFirst()) {
                // Ürün zaten var
                return false; // Ürün zaten var
            } else {
                String currentDate = AppDate.nowDbDateTime();

                BigDecimal priceBD = BigDecimal.valueOf(insertProduct.getPrice()).setScale(2, RoundingMode.HALF_UP);
                float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat

                // Ürün yok, ekleme işlemi
                ContentValues contentValues = new ContentValues();
                contentValues.put("product_id", insertProduct.getProduct_id());
                contentValues.put("create_date", currentDate);
                contentValues.put("update_date", currentDate);
                contentValues.put("quantity", insertProduct.getQuantity());
                contentValues.put("price", roundedPrice);
                contentValues.put("description", insertProduct.getDescription());
                contentValues.put("status", insertProduct.isStatus());
                contentValues.put("deleteStatus", insertProduct.isDeleteStatus());

                long result = sqLiteDatabase.insert("insertProducts", null, contentValues);
                return result != -1; // Ekleme başarılı mı
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Error inserting or updating insert product data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    // Get All Inserted Products with deleteStatus = false
    public List<insertProducts> getAllInsertProducts() {
        List<insertProducts> insertProductList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM insertProducts WHERE deleteStatus = false ORDER BY create_date ASC";

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    insertProducts insertProduct = new insertProducts();
                    insertProduct.setInsert_id(Integer.parseInt(cursor.getString(0)));
                    insertProduct.setProduct_id(Integer.parseInt(cursor.getString(1)));
                    insertProduct.setQuantity(Integer.parseInt(cursor.getString(2))); // quantity için uygun metod
                    insertProduct.setPrice(cursor.getFloat(3)); // price için uygun metod
                    insertProduct.setDescription(cursor.getString(4)); // description için uygun metod
                    insertProduct.setStatus(Boolean.valueOf(cursor.getString(5))); // status için uygun metod
                    insertProduct.setDeleteStatus(Boolean.valueOf(cursor.getString(6))); // deleteStatus için uygun metod
                    insertProduct.setCreate_date(cursor.getString(7)); // create_date için uygun metod
                    insertProduct.setUpdate_date(cursor.getString(8)); // update_date için uygun metod

                    insertProductList.add(insertProduct);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching inserted products", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return insertProductList;
    }

    public boolean updateInsertProduct(insertProducts insertProduct) {
        SQLiteDatabase sqLiteDatabase = null;

        try {
            String currentDate = AppDate.nowDbDateTime();

            BigDecimal priceBD = BigDecimal.valueOf(insertProduct.getPrice()).setScale(2, RoundingMode.HALF_UP);
            float roundedPrice = priceBD.floatValue(); // Yuvarlanmış fiyat

            sqLiteDatabase = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("update_date", currentDate);
            contentValues.put("create_date", insertProduct.getCreate_date());
            contentValues.put("quantity", insertProduct.getQuantity());
            contentValues.put("price", roundedPrice);
            contentValues.put("description", insertProduct.getDescription());
            contentValues.put("status", insertProduct.isStatus());
            contentValues.put("deleteStatus", insertProduct.isDeleteStatus());

            // Ürün güncelleme işlemi
            int result = sqLiteDatabase.update("insertProducts", contentValues, "insert_id = ?", new String[]{String.valueOf(insertProduct.getInsert_id())});
            return result > 0; // Güncelleme başarılı mı
        } catch (Exception e) {
            Log.e("Database Exception", "Error updating insert product data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    // insertProducts tablosundan belirli bir kaydı almak için
    public insertProducts getInsertProductById(int insertId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        insertProducts insertProduct = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();
            // Belirli bir insert_id'yi almak için sorgu
            String query = "SELECT * FROM insertProducts WHERE insert_id = ?";

            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(insertId)});

            // Eğer sonuç varsa ilk sonucu getir
            if (cursor.moveToFirst()) {
                insertProduct = new insertProducts();
                insertProduct.setInsert_id(Integer.parseInt(cursor.getString(0))); // insert_id
                insertProduct.setProduct_id(Integer.parseInt(cursor.getString(1))); // product_id
                insertProduct.setCreate_date(cursor.getString(2)); // create_date
                insertProduct.setUpdate_date(cursor.getString(3)); // update_date
                insertProduct.setQuantity(Integer.parseInt(cursor.getString(4))); // quantity
                insertProduct.setPrice(Float.parseFloat(cursor.getString(5))); // price
                insertProduct.setDescription(cursor.getString(6)); // description
                insertProduct.setStatus(Boolean.valueOf(cursor.getString(7))); // status
                insertProduct.setDeleteStatus(Boolean.valueOf(cursor.getString(8))); // deleteStatus
            }
        } catch (Exception e) {
            Log.e("Database Error", "InsertProduct verisi alırken hata oluştu", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return insertProduct;
    }



    public boolean addSale(sales sale) {
        SQLiteDatabase sqLiteDatabase = null;
        String currentDate = AppDate.nowDbDateTime();

        BigDecimal priceBD = BigDecimal.valueOf(sale.getPrice()).setScale(2, RoundingMode.HALF_UP);
        float roundedSalePrice = priceBD.floatValue(); // Yuvarlanmış fiyat

        BigDecimal winBD = new BigDecimal(String.valueOf(sale.getWin())).setScale(2, RoundingMode.HALF_UP);
        float roundedWinPrice = winBD.floatValue();

        try {
            sqLiteDatabase = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("product_id", sale.getProduct_id());
            contentValues.put("price", roundedSalePrice);
            contentValues.put("win", roundedWinPrice);
            contentValues.put("quantity", sale.getQuantity());
            contentValues.put("create_date", currentDate); // Unix zaman damgası
            contentValues.put("update_date", currentDate); // Başlangıçta aynı
            contentValues.put("description", sale.getDescription());
            contentValues.put("status", sale.isStatus());
            contentValues.put("deleteStatus", sale.isDeleteStatus());

            long result = sqLiteDatabase.insert("sales", null, contentValues);
            return result != -1; // Ekleme başarılı mı
        } catch (Exception e) {
            Log.e("Database Exception", "Error inserting sale data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }


    // Get Sale By ID
    public sales getSaleById(int saleId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        sales sale = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();
            // Query to get a sale by sale_id
            String query = "SELECT * FROM sales WHERE sale_id = ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(saleId)});

            // If the result exists, retrieve the first record
            if (cursor.moveToFirst()) {
                sale = new sales();
                sale.setSale_id(Integer.parseInt(cursor.getString(0))); // sale_id
                sale.setProduct_id(cursor.getString(1)); // product_id
                sale.setPrice(Float.parseFloat(cursor.getString(2))); // price
                sale.setWin(Float.parseFloat(cursor.getString(3))); // win
                sale.setQuantity(Integer.parseInt(cursor.getString(4))); // quantity
                sale.setCreate_date(cursor.getString(5)); // create_date
                sale.setUpdate_date(cursor.getString(6)); // update_date
                sale.setDescription(cursor.getString(7)); // description
                sale.setStatus(Boolean.valueOf(cursor.getString(8))); // status
                sale.setDeleteStatus(Boolean.valueOf(cursor.getString(9))); // deleteStatus
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching sale by ID", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
        return sale;
    }

    public boolean updateSale(sales sale) {
        SQLiteDatabase sqLiteDatabase = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Güncellenecek değerler hazırlanıyor
            ContentValues contentValues = new ContentValues();
            contentValues.put("product_id", sale.getProduct_id());
            contentValues.put("price", sale.getPrice());
            contentValues.put("win", sale.getWin());
            contentValues.put("quantity", sale.getQuantity());
            contentValues.put("update_date", sale.getUpdate_date());
            contentValues.put("description", sale.getDescription());
            contentValues.put("status", sale.isStatus());
            contentValues.put("deleteStatus", sale.isDeleteStatus());

            // Güncelleme işlemi sale_id üzerinden yapılır
            int result = sqLiteDatabase.update("sales", contentValues, "sale_id = ?", new String[]{String.valueOf(sale.getSale_id())});
            return result > 0; // Güncelleme başarılı mı kontrol ediliyor

        } catch (Exception e) {
            Log.e("Database Exception", "Satış verilerini güncellerken hata oluştu", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close(); // Veritabanı bağlantısı kapatılıyor
            }
        }
    }


    public List<ProductDTO> igetProductDTOList() {
        List<ProductDTO> productDTOList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // SQLite veritabanı bağlantısı

        String query = "SELECT * FROM (" +
                "SELECT ip.insert_id AS itemId, ip.product_id, p.name, ip.price, ip.quantity, ip.create_date, ip.description, ip.status, 0 AS saleOrInsert, 0 AS win " +
                "FROM insertProducts ip " +
                "JOIN products p ON ip.product_id = p.product_id " +
                "WHERE ip.deleteStatus = 0 " +
                "UNION ALL " +
                "SELECT s.sale_id AS itemId, s.product_id, p.name, s.price, s.quantity, s.create_date, s.description, s.status, 1 AS saleOrInsert, s.win " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "WHERE s.deleteStatus = 0) " +
                "ORDER BY create_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null); // Sorguyu çalıştırıp sonucu cursor'a atıyoruz

            if (cursor.moveToFirst()) {
                do {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    productDTO.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
                    productDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    productDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    productDTO.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    productDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    productDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    productDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1); // Boolean dönüştürme
                    productDTO.setSaleOrInsert(cursor.getInt(cursor.getColumnIndexOrThrow("saleOrInsert")) == 1);
                    productDTO.setWin(cursor.getFloat(cursor.getColumnIndexOrThrow("win")));

                    productDTOList.add(productDTO);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", "Error while reading data", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor'ı kapat
            }
            db.close(); // Veritabanı bağlantısını kapat
        }

        return productDTOList;
    }

    public List<ProductDTO> getProductDTOList() {
        List<ProductDTO> productDTOList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // SQLite veritabanı bağlantısı

        String query = "SELECT * FROM (" +
                "SELECT ip.insert_id AS itemId, ip.product_id, p.name, ip.price, ip.quantity, ip.create_date, ip.description, ip.status, 0 AS saleOrInsert, 0 AS win " +
                "FROM insertProducts ip " +
                "JOIN products p ON ip.product_id = p.product_id " +
                "WHERE ip.deleteStatus = 0 AND p.deleteStatus = 0 " + // Hem insertProducts hem de products tablolarında deleteStatus = 0 kontrolü
                "UNION ALL " +
                "SELECT s.sale_id AS itemId, s.product_id, p.name, s.price, s.quantity, s.create_date, s.description, s.status, 1 AS saleOrInsert, s.win " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "WHERE s.deleteStatus = 0 AND p.deleteStatus = 0) " + // Hem sales hem de products tablolarında deleteStatus = 0 kontrolü
                "ORDER BY create_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null); // Sorguyu çalıştırıp sonucu cursor'a atıyoruz

            if (cursor.moveToFirst()) {
                do {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    productDTO.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
                    productDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    productDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    productDTO.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    productDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    productDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    productDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1); // Boolean dönüştürme
                    productDTO.setSaleOrInsert(cursor.getInt(cursor.getColumnIndexOrThrow("saleOrInsert")) == 1);
                    productDTO.setWin(cursor.getFloat(cursor.getColumnIndexOrThrow("win")));

                    productDTOList.add(productDTO);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", "Error while reading data", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor'ı kapat
            }
            db.close(); // Veritabanı bağlantısını kapat
        }

        return productDTOList;
    }

    public List<ProductDTO> getProductDTOListForProduct(int productId) {
        List<ProductDTO> productDTOList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // SQLite veritabanı bağlantısı

        String query = "SELECT * FROM (" +
                "SELECT ip.insert_id AS itemId, ip.product_id, p.name, ip.price, ip.quantity, ip.create_date, ip.description, ip.status, 0 AS saleOrInsert, 0 AS win " +
                "FROM insertProducts ip " +
                "JOIN products p ON ip.product_id = p.product_id " +
                "WHERE ip.deleteStatus = 0 AND ip.product_id = ? " +
                "UNION ALL " +
                "SELECT s.sale_id AS itemId, s.product_id, p.name, s.price, s.quantity, s.create_date, s.description, s.status, 1 AS saleOrInsert, s.win " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "WHERE s.deleteStatus = 0 AND s.product_id = ?) " +
                "ORDER BY create_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(productId), String.valueOf(productId)}); // Sorguya product_id eklenir

            if (cursor.moveToFirst()) {
                do {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    productDTO.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
                    productDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    productDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    productDTO.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    productDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    productDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    productDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1); // Boolean dönüştürme
                    productDTO.setSaleOrInsert(cursor.getInt(cursor.getColumnIndexOrThrow("saleOrInsert")) == 1);
                    productDTO.setWin(cursor.getFloat(cursor.getColumnIndexOrThrow("win")));

                    productDTOList.add(productDTO);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", "Error while reading data", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor'ı kapat
            }
            db.close(); // Veritabanı bağlantısını kapat
        }

        return productDTOList;
    }


    public boolean addUser(users user) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Aynı isimdeki tüm kullanıcıları getir
            String query = "SELECT * FROM users WHERE LOWER(name) = LOWER(?)";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{user.getName()});

            if (cursor != null && cursor.moveToFirst()) {
                // Eşleşen tüm kullanıcıların deleteStatus alanını kontrol et
                do {
                    boolean deleteStatus = cursor.getInt(cursor.getColumnIndexOrThrow("deleteStatus")) == 1;
                    if (!deleteStatus) {
                        // Eğer herhangi bir kullanıcıda deleteStatus false ise, ekleme yapılmaz
                        return false;
                    }
                } while (cursor.moveToNext());

                // Eğer tüm kullanıcıların deleteStatus alanı true ise eklemeye izin ver
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", user.getName());
                contentValues.put("mobile", user.getMobile());
                contentValues.put("address", user.getAddress());
                contentValues.put("create_date", user.getCreate_date());
                contentValues.put("update_date", user.getUpdate_date());
                contentValues.put("status", user.isStatus());
                contentValues.put("description", user.getDescription());
                contentValues.put("deleteStatus", user.isDeleteStatus());

                long result = sqLiteDatabase.insert("users", null, contentValues);
                return result != -1; // Ekleme başarılı mı
            } else {
                // Eğer isimde kullanıcı yoksa, doğrudan ekleme yap
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", user.getName());
                contentValues.put("mobile", user.getMobile());
                contentValues.put("address", user.getAddress());
                contentValues.put("create_date", user.getCreate_date());
                contentValues.put("update_date", user.getUpdate_date());
                contentValues.put("status", user.isStatus());
                contentValues.put("description", user.getDescription());
                contentValues.put("deleteStatus", user.isDeleteStatus());

                long result = sqLiteDatabase.insert("users", null, contentValues);
                return result != -1; // Ekleme başarılı mı
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Error inserting or updating user data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }


    public boolean updateUser(users user) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // Kullanıcı adının başka bir kayıtta olup olmadığını kontrol et
            String query = "SELECT * FROM users WHERE LOWER(name) = LOWER(?) AND user_id != ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{user.getName(), String.valueOf(user.getUser_id())});

            if (cursor != null && cursor.moveToFirst()) {
                // Aynı ada sahip başka bir kullanıcı var, deleteStatus kontrol et
                boolean deleteStatus = cursor.getInt(cursor.getColumnIndexOrThrow("deleteStatus")) == 1;
                if (deleteStatus) {
                    // deleteStatus true, güncellemeye izin ver
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name", user.getName());
                    contentValues.put("mobile", user.getMobile());
                    contentValues.put("address", user.getAddress());
                    contentValues.put("update_date", user.getUpdate_date());
                    contentValues.put("status", user.isStatus());
                    contentValues.put("description", user.getDescription());
                    contentValues.put("deleteStatus", user.isDeleteStatus());

                    // Güncelleme işlemi user_id üzerinden yapılır
                    int result = sqLiteDatabase.update("users", contentValues, "user_id = ?", new String[]{String.valueOf(user.getUser_id())});
                    return result > 0; // Güncelleme başarılı mı kontrolü
                } else {
                    // deleteStatus false, güncelleme yapılmasın
                    return false;
                }
            } else {
                // Aynı isimde başka kullanıcı yok, güncelle
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", user.getName());
                contentValues.put("mobile", user.getMobile());
                contentValues.put("address", user.getAddress());
                contentValues.put("update_date", user.getUpdate_date());
                contentValues.put("status", user.isStatus());
                contentValues.put("description", user.getDescription());
                contentValues.put("deleteStatus", user.isDeleteStatus());

                // Güncelleme işlemi user_id üzerinden yapılır
                int result = sqLiteDatabase.update("users", contentValues, "user_id = ?", new String[]{String.valueOf(user.getUser_id())});
                return result > 0; // Güncelleme başarılı mı kontrolü
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Error updating user data", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    // Get All Users
    public List<users> getAllUsers() {
        List<users> userList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        try {
            sqLiteDatabase = this.getReadableDatabase();
            // deleteStatus = false olan kullanıcıları çekmek için sorgu
            String query = "SELECT * FROM users WHERE deleteStatus = false ORDER BY name ASC";

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    users user = new users();
                    user.setUser_id(Integer.parseInt(cursor.getString(0))); // user_id
                    user.setName(cursor.getString(1)); // name
                    user.setMobile(cursor.getString(2)); // mobile
                    user.setAddress(cursor.getString(3)); // address
                    user.setCreate_date(cursor.getString(4)); // create_date
                    user.setUpdate_date(cursor.getString(5)); // update_date
                    user.setStatus(Boolean.valueOf(cursor.getString(6))); // status
                    user.setDescription(cursor.getString(7)); // description
                    user.setDeleteStatus(Boolean.valueOf(cursor.getString(8))); // deleteStatus

                    userList.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching users", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return userList;
    }

    // Get User By ID
    public users getUserById(int userId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        users user = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();
            // Belirli bir user_id'yi almak için sorgu
            String query = "SELECT * FROM users WHERE user_id = ? AND deleteStatus = false";

            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(userId)});

            // Eğer sonuç varsa ilk sonucu getir
            if (cursor.moveToFirst()) {
                user = new users();
                user.setUser_id(Integer.parseInt(cursor.getString(0))); // user_id
                user.setName(cursor.getString(1)); // name
                user.setMobile(cursor.getString(2)); // mobile
                user.setAddress(cursor.getString(3)); // address
                user.setCreate_date(cursor.getString(4)); // create_date
                user.setUpdate_date(cursor.getString(5)); // update_date
                user.setStatus(Boolean.valueOf(cursor.getString(6))); // status
                user.setDescription(cursor.getString(7)); // description
                user.setDeleteStatus(Boolean.valueOf(cursor.getString(8))); // deleteStatus
            }
        } catch (Exception e) {
            Log.e("Database Error", "Error fetching user by ID", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return user;
    }

    public List<UserDTO> getUsersWithAmounts() {
        List<UserDTO> userDTOList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            // Kullanıcıları ve toplam çekim/yatırım miktarlarını düzgün bir şekilde almak için alt sorgular
            String query = "SELECT u.user_id, u.name, u.mobile, " +
                    "(SELECT IFNULL(SUM(w.amount), 0) FROM withdrawals w WHERE w.user_id = u.user_id AND w.status = 1 AND w.deleteStatus = 0) AS withdrawalsAmount, " +
                    "(SELECT IFNULL(SUM(d.amount), 0) FROM deposits d WHERE d.user_id = u.user_id AND d.status = 1 AND d.deleteStatus = 0) AS depositAmount " +
                    "FROM users u " +
                    "WHERE u.status = 1 AND u.deleteStatus = 0 " +
                    "GROUP BY u.user_id, u.name, u.mobile " +
                    "ORDER BY u.name DESC;";

            cursor = sqLiteDatabase.rawQuery(query, null);



            if (cursor != null && cursor.moveToFirst()) {
                // Sütun indekslerini önceden al
                int userIdIndex = cursor.getColumnIndex("user_id");
                int nameIndex = cursor.getColumnIndex("name");
                int mobileIndex = cursor.getColumnIndex("mobile");
                int withdrawalsAmountIndex = cursor.getColumnIndex("withdrawalsAmount");
                int depositAmountIndex = cursor.getColumnIndex("depositAmount");

                // Döngü cursor'daki tüm satırları dolaşır
                do {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUser_id(cursor.getInt(userIdIndex)); // user_id
                    userDTO.setName(cursor.getString(nameIndex)); // name
                    userDTO.setMobile(cursor.getString(mobileIndex)); // mobile
                    userDTO.setWithdrawalsAmount(cursor.getFloat(withdrawalsAmountIndex)); // withdrawalsAmount
                    userDTO.setDepositAmount(cursor.getFloat(depositAmountIndex)); // depositAmount

                    // Sonucu listeye ekleyelim
                    userDTOList.add(userDTO);
                } while (cursor.moveToNext()); // Tüm satırları gezmeye devam et
            }



        } catch (Exception e) {
            Log.e("Database Error", "Error fetching user data with amounts", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return userDTOList;
    }

    public List<UserDTO> getUserWithAmountsList(int userId) {
        List<UserDTO> userDTOList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            // Belirli bir kullanıcıyı ve onun toplam çekim/yatırım miktarlarını almak için sorgu
            String query = "SELECT u.user_id, u.name, u.mobile, " +
                    "(SELECT IFNULL(SUM(w.amount), 0) FROM withdrawals w WHERE w.user_id = u.user_id AND w.status = 1 AND w.deleteStatus = 0) AS withdrawalsAmount, " +
                    "(SELECT IFNULL(SUM(d.amount), 0) FROM deposits d WHERE d.user_id = u.user_id AND d.status = 1 AND d.deleteStatus = 0) AS depositAmount " +
                    "FROM users u " +
                    "WHERE u.user_id = ? AND u.status = 1 AND u.deleteStatus = 0 " +
                    "GROUP BY u.user_id, u.name, u.mobile " +
                    "ORDER BY u.name DESC;";

            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                // Sütun indekslerini önceden al
                int userIdIndex = cursor.getColumnIndex("user_id");
                int nameIndex = cursor.getColumnIndex("name");
                int mobileIndex = cursor.getColumnIndex("mobile");
                int withdrawalsAmountIndex = cursor.getColumnIndex("withdrawalsAmount");
                int depositAmountIndex = cursor.getColumnIndex("depositAmount");

                // Tek bir kullanıcı için değerleri al ve listeye ekle
                do {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUser_id(cursor.getInt(userIdIndex)); // user_id
                    userDTO.setName(cursor.getString(nameIndex)); // name
                    userDTO.setMobile(cursor.getString(mobileIndex)); // mobile
                    userDTO.setWithdrawalsAmount(cursor.getFloat(withdrawalsAmountIndex)); // withdrawalsAmount
                    userDTO.setDepositAmount(cursor.getFloat(depositAmountIndex)); // depositAmount

                    userDTOList.add(userDTO); // Listeye ekle
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("Database Error", "Error fetching user data with amounts", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return userDTOList; // Listeyi döndür
    }


    public boolean addDeposit(deposits deposit) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // DB format must be Latin digits (Locale.US)
            String currentDate = AppDate.nowDbDateTime();

            // 10 dakika önceki zamanı al
            long tenMinutesAgoMillis = System.currentTimeMillis() - (10 * 60 * 1000);
            String tenMinutesAgoDate = AppDate.formatDbDateTime(new Date(tenMinutesAgoMillis));

            // Aynı kullanıcının, aynı miktarda son 10 dakika içinde yatırdığı parayı kontrol eden sorgu
            String query = "SELECT * FROM deposits WHERE user_id = ? AND amount = ? AND create_date >= ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{
                    String.valueOf(deposit.getUser_id()),
                    String.valueOf(deposit.getAmount()),
                    tenMinutesAgoDate
            });

            if (cursor != null && cursor.moveToFirst()) {
                // Aynı kullanıcı son 10 dakika içinde benzer bir miktar yatırmış
                return false; // Kullanıcı benzer bir miktarı yakın zamanda yatırmış
            } else {
                // Kullanıcı son 10 dakika içinde benzer bir miktar yatırmamış, yeni yatırımı ekle
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", deposit.getName());
                contentValues.put("amount", deposit.getAmount());
                contentValues.put("create_date", currentDate); // Yatırma işlemi için mevcut tarih ve zaman
                contentValues.put("update_date", currentDate); // Güncelleme işlemi için de aynı tarih
                contentValues.put("status", deposit.getStatus());
                contentValues.put("description", deposit.getDescription());
                contentValues.put("user_id", deposit.getUser_id());
                contentValues.put("deleteStatus", deposit.isDeleteStatus());

                long result = sqLiteDatabase.insert("deposits", null, contentValues);
                return result != -1; // Eklenme başarılı mı kontrol et
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Yatırım verilerini eklerken veya güncellerken hata oluştu", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    public boolean updateDeposit(deposits deposit) {
        SQLiteDatabase sqLiteDatabase = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // DB format must be Latin digits (Locale.US)
            String currentDate = AppDate.nowDbDateTime();

            // Güncellenmek istenen depozito kaydını güncelle
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", deposit.getName());
            contentValues.put("amount", deposit.getAmount());
            contentValues.put("update_date", currentDate); // Güncelleme işlemi için mevcut tarih ve zaman
            contentValues.put("status", deposit.getStatus());
            contentValues.put("description", deposit.getDescription());
            contentValues.put("deleteStatus", deposit.isDeleteStatus());

            // Depozito kaydını güncelle
            int result = sqLiteDatabase.update("deposits", contentValues, "deposit_id = ?", new String[]{String.valueOf(deposit.getDeposit_id())});
            return result > 0; // Güncelleme başarılı mı kontrol et
        } catch (Exception e) {
            Log.e("Database Exception", "Yatırım verilerini güncellerken hata oluştu", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    public deposits depositGetByID(int depositId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        deposits deposit = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            // Belirli bir deposit ID ile veriyi al
            String query = "SELECT * FROM deposits WHERE deposit_id = ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(depositId)});

            if (cursor != null && cursor.moveToFirst()) {
                // Sütun indekslerini önceden al
                int idIndex = cursor.getColumnIndex("deposit_id");
                int nameIndex = cursor.getColumnIndex("name");
                int amountIndex = cursor.getColumnIndex("amount");
                int createDateIndex = cursor.getColumnIndex("create_date");
                int updateDateIndex = cursor.getColumnIndex("update_date");
                int statusIndex = cursor.getColumnIndex("status");
                int descriptionIndex = cursor.getColumnIndex("description");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int deleteStatusIndex = cursor.getColumnIndex("deleteStatus");

                // Deposit nesnesini oluştur ve doldur
                deposit = new deposits();
                deposit.setDeposit_id(cursor.getInt(idIndex));
                deposit.setName(cursor.getString(nameIndex));
                deposit.setAmount(cursor.getFloat(amountIndex));
                deposit.setCreate_date(cursor.getString(createDateIndex));
                deposit.setUpdate_date(cursor.getString(updateDateIndex));
                deposit.setStatus(cursor.getInt(statusIndex) == 1);
                deposit.setDescription(cursor.getString(descriptionIndex));
                deposit.setUser_id(cursor.getInt(userIdIndex));
                deposit.setDeleteStatus(cursor.getInt(deleteStatusIndex) == 1);
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Deposit verilerini alırken hata oluştu", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return deposit; // Eğer bulunamazsa null döner
    }

    public boolean addWithdrawals(withdrawals withdrawal) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // DB format must be Latin digits (Locale.US)
            String currentDate = AppDate.nowDbDateTime();

            // 10 dakika önceki zamanı al
            long tenMinutesAgoMillis = System.currentTimeMillis() - (10 * 60 * 1000);
            String tenMinutesAgoDate = AppDate.formatDbDateTime(new Date(tenMinutesAgoMillis));

            // Aynı kullanıcının, aynı miktarda son 10 dakika içinde çektiği parayı kontrol eden sorgu
            String query = "SELECT * FROM withdrawals WHERE user_id = ? AND amount = ? AND create_date >= ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{
                    String.valueOf(withdrawal.getUser_id()),
                    String.valueOf(withdrawal.getAmount()),
                    tenMinutesAgoDate
            });

            if (cursor != null && cursor.moveToFirst()) {
                // Aynı kullanıcı son 10 dakika içinde benzer bir miktar çekmiş
                return false; // Kullanıcı benzer bir miktarı yakın zamanda çekmiş
            } else {
                // Kullanıcı son 10 dakika içinde benzer bir miktar çekmemiş, yeni çekim işlemini ekle
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", withdrawal.getName());
                contentValues.put("amount", withdrawal.getAmount());
                contentValues.put("create_date", currentDate); // Çekim işlemi için mevcut tarih ve zaman
                contentValues.put("update_date", currentDate); // Güncelleme işlemi için de aynı tarih
                contentValues.put("status", withdrawal.isStatus());
                contentValues.put("description", withdrawal.getDescription());
                contentValues.put("user_id", withdrawal.getUser_id());
                contentValues.put("deleteStatus", withdrawal.isDeleteStatus());

                long result = sqLiteDatabase.insert("withdrawals", null, contentValues);
                return result != -1; // Eklenme başarılı mı kontrol et
            }
        } catch (Exception e) {
            Log.e("Database Exception", "Çekim verilerini eklerken hata oluştu", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    public boolean updateWithdrawals(withdrawals withdrawal) {
        SQLiteDatabase sqLiteDatabase = null;

        try {
            sqLiteDatabase = this.getWritableDatabase();

            // DB format must be Latin digits (Locale.US)
            String currentDate = AppDate.nowDbDateTime();

            // Çekim kaydını güncellemek için gerekli değerleri ayarlama
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", withdrawal.getName());
            contentValues.put("amount", withdrawal.getAmount());
            contentValues.put("update_date", currentDate); // Güncelleme tarihi
            contentValues.put("status", withdrawal.isStatus());
            contentValues.put("description", withdrawal.getDescription());
            contentValues.put("deleteStatus", withdrawal.isDeleteStatus());

            // Çekim kaydını güncelle
            int result = sqLiteDatabase.update("withdrawals", contentValues, "withdrawal_id = ?", new String[]{String.valueOf(withdrawal.getWithdrawal_id())});
            return result > 0; // Güncelleme başarılı mı kontrol et
        } catch (Exception e) {
            Log.e("Database Exception", "Çekim verilerini güncellerken hata oluştu", e);
            return false; // Hata durumunda başarısız
        } finally {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }

    public withdrawals withdrawalGetByID(int withdrawalId) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        withdrawals withdrawal = null;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            // Belirli bir withdrawal ID ile veriyi al
            String query = "SELECT * FROM withdrawals WHERE withdrawal_id = ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(withdrawalId)});

//            if (cursor != null && cursor.moveToFirst()) {
//                withdrawal = new withdrawals();
//                withdrawal.setWithdrawal_id(cursor.getInt(cursor.getColumnIndex("id")));
//                withdrawal.setName(cursor.getString(cursor.getColumnIndex("name")));
//                withdrawal.setAmount(cursor.getFloat(cursor.getColumnIndex("amount")));
//                withdrawal.setCreate_date(cursor.getString(cursor.getColumnIndex("create_date")));
//                withdrawal.setUpdate_date(cursor.getString(cursor.getColumnIndex("update_date")));
//                withdrawal.setStatus(cursor.getInt(cursor.getColumnIndex("status")) == 1);
//                withdrawal.setDescription(cursor.getString(cursor.getColumnIndex("description")));
//                withdrawal.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
//                withdrawal.setDeleteStatus(cursor.getInt(cursor.getColumnIndex("deleteStatus")) == 1);
//            }

            if (cursor != null && cursor.moveToFirst()) {
                // Sütun indekslerini önceden al
                int idIndex = cursor.getColumnIndex("withdrawal_id");
                int nameIndex = cursor.getColumnIndex("name");
                int amountIndex = cursor.getColumnIndex("amount");
                int createDateIndex = cursor.getColumnIndex("create_date");
                int updateDateIndex = cursor.getColumnIndex("update_date");
                int statusIndex = cursor.getColumnIndex("status");
                int descriptionIndex = cursor.getColumnIndex("description");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int deleteStatusIndex = cursor.getColumnIndex("deleteStatus");

                // Withdrawal nesnesini oluştur ve doldur
                withdrawal = new withdrawals();
                withdrawal.setWithdrawal_id(cursor.getInt(idIndex));
                withdrawal.setName(cursor.getString(nameIndex));
                withdrawal.setAmount(cursor.getFloat(amountIndex));
                withdrawal.setCreate_date(cursor.getString(createDateIndex));
                withdrawal.setUpdate_date(cursor.getString(updateDateIndex));
                withdrawal.setStatus(cursor.getInt(statusIndex) == 1);
                withdrawal.setDescription(cursor.getString(descriptionIndex));
                withdrawal.setUser_id(cursor.getInt(userIdIndex));
                withdrawal.setDeleteStatus(cursor.getInt(deleteStatusIndex) == 1);
            }

        } catch (Exception e) {
            Log.e("Database Exception", "Withdrawal verilerini alırken hata oluştu", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }

        return withdrawal; // Eğer bulunamazsa null döner
    }


    public List<CashDTO> igetAllCashDTO() {
        List<CashDTO> cashDTOList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // SQLite veritabanı bağlantısı

        String query = "SELECT * FROM (" +
                "SELECT w.withdrawal_id AS itemId, u.user_id, u.name, w.name AS payUser, w.amount AS price, 0 AS debit, 0 AS depositOrWithdrawal, w.create_date, w.description, w.status " +
                "FROM withdrawals w " +
                "JOIN users u ON w.user_id = u.user_id " +
                "WHERE w.deleteStatus = 0 " +
                "UNION ALL " +
                "SELECT d.deposit_id AS itemId, u.user_id, u.name, d.name AS payUser, d.amount AS price, 0 AS debit, 1 AS depositOrWithdrawal, d.create_date, d.description, d.status " +
                "FROM deposits d " +
                "JOIN users u ON d.user_id = u.user_id " +
                "WHERE d.deleteStatus = 0) " +
                "ORDER BY create_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null); // Sorguyu çalıştırıp sonucu cursor'a atıyoruz

            if (cursor.moveToFirst()) {
                do {
                    CashDTO cashDTO = new CashDTO();
                    cashDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    cashDTO.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                    cashDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    cashDTO.setPayUser(cursor.getString(cursor.getColumnIndexOrThrow("payUser")));
                    cashDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    cashDTO.setDepositOrWithdrawal(cursor.getInt(cursor.getColumnIndexOrThrow("depositOrWithdrawal")) == 1);
                    cashDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    cashDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    cashDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1); // Boolean dönüştürme

                    cashDTOList.add(cashDTO);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", "Error while reading data", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor'ı kapat
            }
            db.close(); // Veritabanı bağlantısını kapat
        }

        return cashDTOList;
    }

    public List<CashDTO> getAllCashDTO() {
        List<CashDTO> cashDTOList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // SQLite veritabanı bağlantısı

        String query = "SELECT * FROM (" +
                "SELECT w.withdrawal_id AS itemId, u.user_id, u.name, w.name AS payUser, w.amount AS price, 0 AS debit, 0 AS depositOrWithdrawal, w.create_date, w.description, w.status " +
                "FROM withdrawals w " +
                "JOIN users u ON w.user_id = u.user_id " +
                "WHERE w.deleteStatus = 0 AND u.deleteStatus = 0 " + // withdrawals ve users deleteStatus = 0 kontrolü
                "UNION ALL " +
                "SELECT d.deposit_id AS itemId, u.user_id, u.name, d.name AS payUser, d.amount AS price, 0 AS debit, 1 AS depositOrWithdrawal, d.create_date, d.description, d.status " +
                "FROM deposits d " +
                "JOIN users u ON d.user_id = u.user_id " +
                "WHERE d.deleteStatus = 0 AND u.deleteStatus = 0) " + // deposits ve users deleteStatus = 0 kontrolü
                "ORDER BY create_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null); // Sorguyu çalıştırıp sonucu cursor'a atıyoruz

            if (cursor.moveToFirst()) {
                do {
                    CashDTO cashDTO = new CashDTO();
                    cashDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    cashDTO.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                    cashDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    cashDTO.setPayUser(cursor.getString(cursor.getColumnIndexOrThrow("payUser")));
                    cashDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    cashDTO.setDepositOrWithdrawal(cursor.getInt(cursor.getColumnIndexOrThrow("depositOrWithdrawal")) == 1);
                    cashDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    cashDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    cashDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1); // Boolean dönüştürme

                    cashDTOList.add(cashDTO);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", "Error while reading data", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor'ı kapat
            }
            db.close(); // Veritabanı bağlantısını kapat
        }

        return cashDTOList;
    }

    // ------------------------------
    // Cash filtering (date-safe across locales)
    // ------------------------------

    /**
     * Returns cash rows with safe date filtering.
     *
     * Notes:
     * - DB stores create_date like: yyyy-MM-dd HH:mm:ss.
     * - Old rows may contain Persian/Arabic-Indic digits; we normalize them inside SQL.
     * - fromDay/toDay can be Gregorian (2025-12-23) or Afghan (1404-04-14 / 1404-جوزا-14).
     * - flowTypeIndex: 0=all, 1=out(withdrawals), 2=in(deposits)
     */
    public List<CashDTO> getCashDTOFiltered(@Nullable String search,
                                            @Nullable String fromDay,
                                            @Nullable String toDay,
                                            int flowTypeIndex,
                                            @Nullable Integer userId) {
        List<CashDTO> out = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        // Build date range (inclusive) in Gregorian, DB datetime format
        AppDate.Range r = AppDate.toDbDateTimeRangeInclusive(fromDay, toDay);

        String normCreate = normalizeDigitsSql("create_date");
        // Force SQLite to understand it as datetime
        String createDt = "datetime(" + normCreate + ")";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (")
                .append("SELECT w.withdrawal_id AS itemId, u.user_id, u.name, w.name AS payUser, w.amount AS price, 0 AS debit, 0 AS depositOrWithdrawal, w.create_date, w.description, w.status ")
                .append("FROM withdrawals w JOIN users u ON w.user_id = u.user_id ")
                .append("WHERE w.deleteStatus = 0 AND u.deleteStatus = 0 ")
                .append("UNION ALL ")
                .append("SELECT d.deposit_id AS itemId, u.user_id, u.name, d.name AS payUser, d.amount AS price, 0 AS debit, 1 AS depositOrWithdrawal, d.create_date, d.description, d.status ")
                .append("FROM deposits d JOIN users u ON d.user_id = u.user_id ")
                .append("WHERE d.deleteStatus = 0 AND u.deleteStatus = 0")
                .append(") WHERE 1=1 ");

        List<String> args = new ArrayList<>();

        // User filter
        if (userId != null) {
            sql.append(" AND user_id = ?");
            args.add(String.valueOf(userId));
        }

        // Date range filter
        sql.append(" AND ").append(createDt).append(" BETWEEN datetime(?) AND datetime(?)");
        args.add(r.from);
        args.add(r.to);

        // Flow type
        if (flowTypeIndex == 1) {
            sql.append(" AND depositOrWithdrawal = 0");
        } else if (flowTypeIndex == 2) {
            sql.append(" AND depositOrWithdrawal = 1");
        }

        // Search (name only, matches existing fragment behavior)
        if (search != null) {
            String s = search.trim().toLowerCase(Locale.US);
            if (!s.isEmpty()) {
                sql.append(" AND lower(name) LIKE ?");
                args.add("%" + s + "%");
            }
        }

        sql.append(" ORDER BY ").append(createDt).append(" DESC");

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

            if (cursor.moveToFirst()) {
                do {
                    CashDTO cashDTO = new CashDTO();
                    cashDTO.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("itemId")));
                    cashDTO.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                    cashDTO.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    cashDTO.setPayUser(cursor.getString(cursor.getColumnIndexOrThrow("payUser")));
                    cashDTO.setPrice(cursor.getFloat(cursor.getColumnIndexOrThrow("price")));
                    cashDTO.setDepositOrWithdrawal(cursor.getInt(cursor.getColumnIndexOrThrow("depositOrWithdrawal")) == 1);
                    cashDTO.setCreateDate(cursor.getString(cursor.getColumnIndexOrThrow("create_date")));
                    cashDTO.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    cashDTO.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1);
                    out.add(cashDTO);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error in getCashDTOFiltered", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return out;
    }

    /**
     * Converts Persian/Arabic-Indic digits inside SQLite TEXT column to Latin digits.
     * This enables correct SQLite datetime(...) parsing and BETWEEN comparisons.
     */
    private static String normalizeDigitsSql(String columnExpr) {
        String s = columnExpr;
        // Persian ۰۱۲۳۴۵۶۷۸۹
        s = "REPLACE(" + s + ",'۰','0')";
        s = "REPLACE(" + s + ",'۱','1')";
        s = "REPLACE(" + s + ",'۲','2')";
        s = "REPLACE(" + s + ",'۳','3')";
        s = "REPLACE(" + s + ",'۴','4')";
        s = "REPLACE(" + s + ",'۵','5')";
        s = "REPLACE(" + s + ",'۶','6')";
        s = "REPLACE(" + s + ",'۷','7')";
        s = "REPLACE(" + s + ",'۸','8')";
        s = "REPLACE(" + s + ",'۹','9')";
        // Arabic ٠١٢٣٤٥٦٧٨٩
        s = "REPLACE(" + s + ",'٠','0')";
        s = "REPLACE(" + s + ",'١','1')";
        s = "REPLACE(" + s + ",'٢','2')";
        s = "REPLACE(" + s + ",'٣','3')";
        s = "REPLACE(" + s + ",'٤','4')";
        s = "REPLACE(" + s + ",'٥','5')";
        s = "REPLACE(" + s + ",'٦','6')";
        s = "REPLACE(" + s + ",'٧','7')";
        s = "REPLACE(" + s + ",'٨','8')";
        s = "REPLACE(" + s + ",'٩','9')";
        return s;
    }

    /**
     * Profile screen helper: returns filtered rows + totals for a specific user.
     * Totals are computed only from rows where status=1.
     */
    public UserCashSummaryDTO getUserCashSummary(int userId,
                                                 @Nullable String fromDay,
                                                 @Nullable String toDay) {
        List<CashDTO> rows = getCashDTOFiltered(null, fromDay, toDay, 0, userId);

        float depositsSum = 0f;
        float withdrawalsSum = 0f;
        for (CashDTO r : rows) {
            if (!r.isStatus()) continue;
            if (r.isDepositOrWithdrawal()) depositsSum += r.getPrice();
            else withdrawalsSum += r.getPrice();
        }

        UserCashSummaryDTO dto = new UserCashSummaryDTO();
        dto.setUserId(userId);
        dto.setFromDay(fromDay);
        dto.setToDay(toDay);
        dto.setRows(rows);
        dto.setTotalDeposits(depositsSum);
        dto.setTotalWithdrawals(withdrawalsSum);
        dto.setNet(depositsSum - withdrawalsSum);
        return dto;
    }

    public List<AvailableProductDTO> getAvailableProducts(SQLiteDatabase db) {
        List<AvailableProductDTO> productList = new ArrayList<>();

        // SADECE status = 1 olan ürünleri getir
        String query = "SELECT name, price, quantity FROM " + table_products + " WHERE status = 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                float price = cursor.getFloat(1);
                int quantity = cursor.getInt(2);

                productList.add(new AvailableProductDTO(name, price, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }



}
