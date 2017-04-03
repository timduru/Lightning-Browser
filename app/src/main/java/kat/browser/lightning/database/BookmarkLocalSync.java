package kat.browser.lightning.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.anthonycr.bonsai.Single;
import com.anthonycr.bonsai.SingleAction;
import com.anthonycr.bonsai.SingleSubscriber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kat.browser.lightning.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class BookmarkLocalSync {

    private static final String TAG = BookmarkLocalSync.class.getSimpleName();

    private static final String STOCK_BOOKMARKS_CONTENT = "content://browser/bookmarks";
    private static final String CHROME_BOOKMARKS_CONTENT = "content://com.android.chrome.browser/bookmarks";
    private static final String CHROME_BETA_BOOKMARKS_CONTENT = "content://com.chrome.beta.browser/bookmarks";
    private static final String CHROME_DEV_BOOKMARKS_CONTENT = "content://com.chrome.dev.browser/bookmarks";

    @SuppressLint("SdCardPath")
    private static final String CHROME_BOOKMARKS_FILEPATH = "/data/data/com.android.chrome/app_chrome/Default/Bookmarks";
    @SuppressLint("SdCardPath")
    private static final String CHROME_BETA_BOOKMARKS_FILEPATH = "/data/data/com.chrome.beta/app_chrome/Default/Bookmarks";
    @SuppressLint("SdCardPath")
    private static final String CHROME_DEV_BOOKMARKS_FILEPATH = "/data/data/com.chrome.dev/app_chrome/Default/Bookmarks";

    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_BOOKMARK = "bookmark";

    @NonNull private final Context mContext;

    public enum Source {
        STOCK,
        CHROME_STABLE,
        CHROME_BETA,
        CHROME_DEV
    }

    public BookmarkLocalSync(@NonNull Context context) {
        mContext = context;
    }

    private List<HistoryItem> getBookmarksFromContentUri(String contentUri) {
        List<HistoryItem> list = new ArrayList<>();
        Cursor cursor = getBrowserCursor(contentUri);
        try {
            if (cursor != null) {
                for (int n = 0; n < cursor.getColumnCount(); n++) {
                    Log.d(TAG, cursor.getColumnName(n));
                }

                while (cursor.moveToNext()) {
                    if (cursor.getInt(2) == 1) {
                        String url = cursor.getString(0);
                        String title = cursor.getString(1);
                        if (url.isEmpty()) {
                            continue;
                        }
                        if (title == null || title.isEmpty()) {
                            title = Utils.getDomainName(url);
                        }
                        if (title != null) {
                            list.add(new HistoryItem(url, title));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.close(cursor);
        return list;
    }

    @Nullable
    @WorkerThread
    private Cursor getBrowserCursor(String contentUri) {
        Cursor cursor;
        Uri uri = Uri.parse(contentUri);
        try {
            cursor = mContext.getContentResolver().query(uri,
                    new String[]{COLUMN_URL, COLUMN_TITLE, COLUMN_BOOKMARK}, null, null, null);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return cursor;
    }

    @NonNull
    public Single<List<Source>> getSupportedBrowsers() {
        return Single.create(new SingleAction<List<Source>>() {
            @Override
            public void onSubscribe(@NonNull SingleSubscriber<List<Source>> subscriber) {
                List<Source> sources = new ArrayList<>();
                if (isBrowserSupported(STOCK_BOOKMARKS_CONTENT)) {
                    sources.add(Source.STOCK);
                }
                if (isBrowserSupported(CHROME_BOOKMARKS_CONTENT)) {
                    sources.add(Source.CHROME_STABLE);
                }
                if (isBrowserSupported(CHROME_BETA_BOOKMARKS_CONTENT)) {
                    sources.add(Source.CHROME_BETA);
                }
                if (isBrowserSupported(CHROME_DEV_BOOKMARKS_CONTENT)) {
                    sources.add(Source.CHROME_DEV);
                }
                subscriber.onItem(sources);
                subscriber.onComplete();
            }
        });
    }

    private boolean isBrowserSupported(String contentUri) {
        Cursor cursor = getBrowserCursor(contentUri);
        boolean supported = cursor != null;
        Utils.close(cursor);
        return supported;
    }

    @NonNull
    @WorkerThread
    public List<HistoryItem> getBookmarksFromStockBrowser() {
        return getBookmarksFromContentUri(STOCK_BOOKMARKS_CONTENT);
    }


    @NonNull
    @WorkerThread
    public List<HistoryItem> getBookmarksFromChrome() {
        return getBookmarksFromChromeFile(CHROME_BOOKMARKS_FILEPATH);
//        return getBookmarksFromContentUri(CHROME_BOOKMARKS_CONTENT);
    }

@NonNull
    @WorkerThread
    public List<HistoryItem> getBookmarksFromChromeBeta() {
        return getBookmarksFromChromeFile(CHROME_BETA_BOOKMARKS_FILEPATH);
        //return getBookmarksFromContentUri(CHROME_BETA_BOOKMARKS_CONTENT);
    }

    @NonNull
    @WorkerThread
    public List<HistoryItem> getBookmarksFromChromeDev() {
        return getBookmarksFromChromeFile(CHROME_DEV_BOOKMARKS_FILEPATH);
        //return getBookmarksFromContentUri(CHROME_DEV_BOOKMARKS_CONTENT);
    }

    @WorkerThread
    public boolean isBrowserImportSupported() {
        Cursor chrome = getChromeCursor();
        Utils.close(chrome);
        Cursor dev = getChromeDevCursor();
        Utils.close(dev);
        Cursor beta = getChromeBetaCursor();
        Cursor stock = getStockCursor();
        Utils.close(stock);
        return chrome != null || dev != null || beta != null || stock != null;
    }

    @Nullable
    @WorkerThread
    private Cursor getChromeBetaCursor() {
        return getBrowserCursor(CHROME_BETA_BOOKMARKS_CONTENT);
    }

    @Nullable
    @WorkerThread
    private Cursor getChromeDevCursor() {
        return getBrowserCursor(CHROME_DEV_BOOKMARKS_CONTENT);
    }

    @Nullable
    @WorkerThread
    private Cursor getChromeCursor() {
        return getBrowserCursor(CHROME_BOOKMARKS_CONTENT);
    }

    @Nullable
    @WorkerThread
    private Cursor getStockCursor() {
        return getBrowserCursor(STOCK_BOOKMARKS_CONTENT);
    }

    public void printAllColumns() {
        printColumns(CHROME_BETA_BOOKMARKS_CONTENT);
        printColumns(CHROME_BOOKMARKS_CONTENT);
        printColumns(CHROME_DEV_BOOKMARKS_CONTENT);
        printColumns(STOCK_BOOKMARKS_CONTENT);
    }

    private void printColumns(String contentProvider) {
        Cursor cursor = null;
        Log.e(TAG, contentProvider);
        Uri uri = Uri.parse(contentProvider);
        try {
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error Occurred", e);
        }
        if (cursor != null) {
            for (int n = 0; n < cursor.getColumnCount(); n++) {
                Log.d(TAG, cursor.getColumnName(n));
            }
            cursor.close();
        }
    }

    private void addChromeJSONTree(String parentName, JSONObject tree, List<HistoryItem> list) {
        try
        {
            String maintype = tree.getString("type"); //"folder", "url"
            String folderName = null;
            if (maintype.equals("folder")) folderName = tree.getString("name");
            String folderCrumbs = (!parentName.equals("")? parentName +"\\":"") +  folderName;

            JSONArray children = tree.getJSONArray("children");

            if(children != null)
                for (int i = 0; i < children.length(); i++)
                {
                    JSONObject node = children.getJSONObject(i);
                    String name = node.getString("name");
                    String type = node.getString("type"); //"folder", "url"

                    String url = null;
                    if (type.equals("url")) url = node.getString("url");
                    else if (type.equals("folder")) addChromeJSONTree(folderCrumbs, node, list);

                    if (url != null)
                    {
                        HistoryItem item = new HistoryItem(url, name);
                        if(folderName != null) item.setFolder( folderCrumbs);
                        list.add(item);
                    }
                }
        }
        catch (Exception e) {Log.e(TAG, "",e);}
    }


    @NonNull
    @WorkerThread
    private List<HistoryItem> getBookmarksFromChromeFile(String path) {
        List<HistoryItem> list = new ArrayList<>();
        StringBuilder sBookmark = new StringBuilder();

        String targetFilePath = mContext.getFilesDir().getPath() + "/" + "Bookmarks";
        Utils.runAsRoot(new String[] {"/system/bin/cp " + path + " " + targetFilePath
                , "/system/bin/chmod 755 " + targetFilePath
        });

        try {
            File file = new File(targetFilePath);
            InputStream inputStream = new FileInputStream(file);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null)
                sBookmark.append(line);
        }catch (Exception e) { Log.e(TAG,"getBookmarksFromChromeFile",e );}

        JSONObject jsonBook;
        try {
            jsonBook = new JSONObject(sBookmark.toString());
            JSONObject root = jsonBook.getJSONObject("roots");

            Iterator<String> it = root.keys();
            while (it.hasNext()) {
                String key = it.next();

                try{
                    addChromeJSONTree("",root.getJSONObject(key), list);}
                catch(Exception e) { Log.w(TAG, "",e); }
            }
        }
        catch (Exception e) { Log.e(TAG, "",e); }

        File tmpBook = new File(targetFilePath);
        if(tmpBook.exists()) tmpBook.delete();

        return list;
    }

}
