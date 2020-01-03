package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.core.network.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public abstract class MangaProvider {

    private final SharedPreferences mPrefs;

    protected final Document getPage(String url) throws IOException {
        return NetworkUtils.httpGet(url, getAuthCookie());
    }

    protected final Document getPage(String url, @NonNull String cookie) throws IOException {
        return NetworkUtils.httpGet(url, AppHelper.concatStr(getAuthCookie(), cookie));
    }

    protected final Document postPage(String url, String... data) throws IOException {
        return NetworkUtils.httpPost(url, getAuthCookie(), data);
    }

    @NonNull
    protected final String getRaw(String url) throws IOException {
        return NetworkUtils.getRaw(url, getAuthCookie());
    }

    public MangaProvider(Context context) {
        mPrefs = context.getSharedPreferences("prov_" + this.getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    @NonNull
    protected final String getStringPreference(@NonNull String key, @NonNull String defValue) {
        return mPrefs.getString(key, defValue);
    }

    protected final boolean getBooleanPreference(@NonNull String key, boolean defValue) {
        return mPrefs.getBoolean(key, defValue);
    }

    protected final int getIntPreference(@NonNull String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    //content access methods

    /**
     * Used to get MangaList of all manga that it provider have.
     *
     * @param page page number. Used for pagination
     * @param sort Sort order. Implementation depends on concrete manga provider
     * @param genre Genre of the anime. Implementation depends on concrete manga provider
     * @return MangaList containing MangaInfo of all manga
     */
    public abstract MangaList getList(int page, int sort, int genre) throws Exception;

    @Deprecated
    public MangaList getList(int page, int sort) throws Exception {
        return getList(page, sort, 0);
    }

    @Deprecated
    public MangaList getList(int page) throws Exception {
        return getList(page, 0, 0);
    }

    /**
     * Used to get detailed summary of manga by MangaInfo
     * @param mangaInfo MangaInfo created by #getList or #search methods
     * @return MangaSummary that contains additional detailed information
     */
    public abstract MangaSummary getDetailedInfo(MangaInfo mangaInfo);

    /**
     * Method to get manga pages from the manga link.
     * @param readLink link for the manga
     * @return list of MangaPage
     */
    public abstract ArrayList<MangaPage> getPages(String readLink);

    public abstract String getPageImage(MangaPage mangaPage);

    //optional content access methods
    @Nullable
    public MangaList search(String query, int page) throws Exception {
        return null;
    }

    /**
     * Used for "local" providers
     * @param ids ids of mangas to delete
     * @return true if operation was successful
     */
    public boolean remove(long[] ids) {
        return false;
    }

    //other methods

    /**
     * @return human-readable name, that will be shown to user
     */
    public abstract String getName();

    /**
     * Can be executed if {@link #hasSort()} returns true
     * @return array of strings containing available sorts
     */
    @Nullable
    public String[] getSortTitles(Context context) {
        return null;
    }

    /**
     * Can be executed if {@link #hasGenres()} returns true
     * @return array of strings containing available genres

     */
    @Nullable
    public String[] getGenresTitles(Context context) {
        return null;
    }

    @Deprecated
    protected final String[] getTitles(Context context, int[] ids) {
        return AppHelper.getStringArray(context, ids);
    }

    /**
     * If provider has genres, then this method should return true.
     * @return true if provider has genres, false otherwise
     * @see #getGenresTitles(Context)
     */
    public boolean hasGenres() {
        return false;
    }

    /**
     * If provider has sorts, then this method should return true
     * @return true if provider has sorts, false otherwise
     * @see #getSortTitles(Context)
     */
    public boolean hasSort() {
        return false;
    }

    /**
     * @return true - if method {@link #remove(long[])} is implemented
     * @see #remove(long[])
     */
    public boolean isItemsRemovable() {
        return false;
    }

    /**
     * @return true - if method {@link #search(String, int)} is implemented
     * @see #search(String, int)
     */
    public boolean isSearchAvailable() {
        return false;
    }

    public boolean isMultiPage() {
        return true;
    }

    @Nullable
    protected String getAuthCookie() {
        return null;
    }

    protected static String concatUrl(String root, String url) {
        return url == null || url.startsWith("http://") || url.startsWith("https://") ? url : root + (url.startsWith("/") ? url.substring(1) : url);
    }
}
