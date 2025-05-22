package uz.iqbolshoh.socialchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MessageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IS_USER = "isUser";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_IS_USER + " INTEGER,"
                + COLUMN_TIMESTAMP + " INTEGER"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Xabar qo'shish
    public void addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, message.content);
        values.put(COLUMN_IS_USER, message.isUser ? 1 : 0);
        values.put(COLUMN_TIMESTAMP, message.timestamp);

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // Barcha xabarlarni olish (timestamp bo'yicha o'sish tartibida)
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                message.content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                message.isUser = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_USER)) == 1;
                message.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return messages;
    }
}
