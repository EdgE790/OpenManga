package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

/**
 * Created by koitharu on 24.12.17.
 */

public class HistoryRepository implements Repository<MangaHistory> {

	private static final String TABLE_NAME = "history";
	private static final String[] PROJECTION = new String[] {
				"id",						//0
				"name",						//1
				"summary",					//2
				"genres",					//3
				"url",						//4
				"thumbnail",				//5
				"provider",					//6
				"status",					//7
				"rating",					//8
				"chapter_id",				//9
				"page_id",					//10
				"updated_at",				//11
				"reader_preset",			//12
				"total_chapters",			//13
				"removed"					//14
	};

	private final StorageHelper mStorageHelper;

	public HistoryRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(MangaHistory mangaHistory) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(mangaHistory)) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(MangaHistory mangaHistory) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaHistory.id)}) > 0;
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
	}

	@Override
	public boolean update(MangaHistory mangaHistory) {
		try {
			return mStorageHelper.getWritableDatabase()
					.update(TABLE_NAME, toContentValues(mangaHistory),
							"id=?", new String[]{String.valueOf(mangaHistory.id)}) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Nullable
	@Override
	public ArrayList<MangaHistory> query(@NonNull SqlSpecification specification) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().query(
					TABLE_NAME,
					PROJECTION,
					specification.getSelection(),
					specification.getSelectionArgs(),
					null,
					null,
					specification.getOrderBy(),
					specification.getLimit()
			);
			ArrayList<MangaHistory> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new MangaHistory(
							cursor.getLong(0),
							cursor.getString(1),
							cursor.getString(2),
							cursor.getString(3),
							cursor.getString(4),
							cursor.getString(5),
							cursor.getString(6),
							cursor.getInt(7),
							cursor.getShort(8),
							cursor.getLong(9),
							cursor.getLong(10),
							cursor.getLong(11),
							cursor.getShort(12),
							cursor.getInt(13)
					));
				} while (cursor.moveToNext());
			}
			return list;
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Nullable
	public MangaHistory find(MangaHeader mangaHeader) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().query(
					TABLE_NAME,
					PROJECTION,
					"id = ?",
					new String[]{String.valueOf(mangaHeader.id)},
					null,
					null,
					null,
				null
			);
			if (cursor.moveToFirst()) {
				return new MangaHistory(
						cursor.getLong(0),
						cursor.getString(1),
						cursor.getString(2),
						cursor.getString(3),
						cursor.getString(4),
						cursor.getString(5),
						cursor.getString(6),
						cursor.getInt(7),
						cursor.getShort(8),
						cursor.getLong(9),
						cursor.getLong(10),
						cursor.getLong(11),
						cursor.getShort(12),
						cursor.getInt(13)
				);
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	public boolean quickUpdate(MangaHeader manga, MangaChapter chapter, MangaPage page) {
		try {
			final ContentValues cv = new ContentValues();
			cv.put(PROJECTION[9], chapter.id);
			cv.put(PROJECTION[10], page.id);
			cv.put(PROJECTION[11], System.currentTimeMillis());
			return mStorageHelper.getWritableDatabase()
					.update(TABLE_NAME, cv,
							"id=?", new String[]{String.valueOf(manga.id)}) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	private ContentValues toContentValues(MangaHistory mangaHistory) {
		final ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], mangaHistory.id);
		cv.put(PROJECTION[1], mangaHistory.name);
		cv.put(PROJECTION[2], mangaHistory.summary);
		cv.put(PROJECTION[3], mangaHistory.genres);
		cv.put(PROJECTION[4], mangaHistory.url);
		cv.put(PROJECTION[5], mangaHistory.thumbnail);
		cv.put(PROJECTION[6], mangaHistory.provider);
		cv.put(PROJECTION[7], mangaHistory.status);
		cv.put(PROJECTION[8], mangaHistory.rating);
		cv.put(PROJECTION[9], mangaHistory.chapterId);
		cv.put(PROJECTION[10], mangaHistory.pageId);
		cv.put(PROJECTION[11], mangaHistory.updatedAt);
		cv.put(PROJECTION[12], mangaHistory.readerPreset);
		cv.put(PROJECTION[13], mangaHistory.totalChapters);
		//cv.put(PROJECTION[14], 0);
		return cv;
	}
}