package org.nv95.openmanga.providers.online

import android.content.Context
import org.jsoup.nodes.Element
import org.nv95.openmanga.R
import org.nv95.openmanga.feature.manga.domain.MangaInfo
import org.nv95.openmanga.items.MangaChapter
import org.nv95.openmanga.items.MangaPage
import org.nv95.openmanga.items.MangaSummary
import org.nv95.openmanga.lists.MangaList
import org.nv95.openmanga.providers.MangaProvider
import org.nv95.openmanga.utils.AppHelper
import java.net.URLEncoder
import kotlin.collections.ArrayList

/**
 * MangaProvider that gets manga from https://mangaread.co
 */
class MangaReadProvider(ctx: Context) : MangaProvider(ctx) {

	/**
	 * @property resId string resource id used to get localized name of genre
	 * @property urlId is used in url to retrieve list
	 */
	data class GenreEntry(
			val resId: Int,
			val urlId: String,
			val url: String = createGenreUrl(urlId)
	) {
		companion object {
			private fun createGenreUrl(id: String): String {
				return "https://mangaread.co/m-gen/$id/"
			}
		}

		fun getUrl(page: Int): String {
			var result = url
			if (page > 1) {
				result += "page/$page/"
			}
			return result
		}

		fun getUrl(page: Int, sort: SortEntry?): String {
			var result = getUrl(page)
			if (sort != null) {
				result += sort.getUrlPart()
			}
			return result
		}
	}

	data class SortEntry(
			val resId: Int,
			val id: String
	) {
		fun getUrlPart(): String {
			return id
		}
	}

	companion object {
		val genres = arrayOf(
				GenreEntry(R.string.genre_all, "all", "https://mangaread.co/manga/"),
				GenreEntry(R.string.genre_action, "action"),
				GenreEntry(R.string.genre_adventure, "adventure"),
				GenreEntry(R.string.genre_comedy, "comedy"),
				GenreEntry(R.string.genre_doujinshi, "doujinshi"),
				GenreEntry(R.string.genre_drama, "drama"),
				GenreEntry(R.string.genre_ecchi, "ecchi"),
				GenreEntry(R.string.genre_fantasy, "fantasy"),
				GenreEntry(R.string.genre_genderbender, "gender-bender"),
				GenreEntry(R.string.genre_harem, "harem"),
				GenreEntry(R.string.genre_historical, "historical"),
				GenreEntry(R.string.genre_horror, "horror"),
				GenreEntry(R.string.genre_josei, "josei"),
				GenreEntry(R.string.genre_mature, "mature"),
				GenreEntry(R.string.genre_mystery, "mystery"),
				GenreEntry(R.string.genre_psychological, "psychological"),
				GenreEntry(R.string.genre_romance, "romance"),
				GenreEntry(R.string.genre_school, "school-life"),
				GenreEntry(R.string.genre_sci_fi, "sci-fi"),
				GenreEntry(R.string.genre_shoujo, "shoujo"),
				GenreEntry(R.string.genre_slice_of_life, "slice-of-life"),
				GenreEntry(R.string.genre_smut, "smut"),
				GenreEntry(R.string.genre_sports, "sports"),
				GenreEntry(R.string.genre_supernatural, "supernatural"),
				GenreEntry(R.string.genre_yaoi, "yaoi"),
				GenreEntry(R.string.genre_yuri, "yuri")
		)
		val genreIndexes = genres.map { it.resId }.toIntArray()

		val sorts = arrayOf(
				SortEntry(R.string.sort_latest, "?m_orderby=latest"),
				SortEntry(R.string.sort_alphabetical, "?m_orderby=alphabet"),
				SortEntry(R.string.sort_rating, "?m_orderby=rating"),
				SortEntry(R.string.sort_trending, "?m_orderby=trending"),
				SortEntry(R.string.sort_views, "?m_orderby=views"),
				SortEntry(R.string.sort_new, "?m_orderby=new-manga")
		)
		val sortsIndexes = sorts.map { it.resId }.toIntArray()
	}

	override fun getName(): String = "MangaRead"

	override fun getList(page: Int, sort: Int, genre: Int): MangaList {
		val mangaList = MangaList()

		val document = getPage(getUrl(genre, page, sort))
		val elements = document.body().select(".manga")

		for (element in elements) {
			mangaList.add(extractDataFromMangaElement(element))
		}

		return mangaList
	}

	override fun getDetailedInfo(mangaInfo: MangaInfo?): MangaSummary {
		val mangaSummary = MangaSummary(mangaInfo)
		val root = getPage(mangaSummary.path)

		val postContent = root.select(".post-content")[0]
		mangaSummary.subtitle = postContent.select(".post-content_item")[2].select(".summary-content")[0].text()
		mangaSummary.genres = postContent.select(".genres-content > a").joinToString { it.text() }

		mangaSummary.status = parseStatus(root)
		mangaSummary.description = root.select(".summary__content")[0].text()

		val chapters = root.select(".wp-manga-chapter")

		chapters.reversed().forEachIndexed { index, chapter ->
			mangaSummary.chapters.add(extractChapterFromElement(chapter, index))
		}

		return mangaSummary
	}

	override fun getPages(readLink: String?): ArrayList<MangaPage> {
		val root = getPage(readLink)
		val pageElements = root.selectFirst("#single-pager.selectpicker").select("option")
		val mangaPageList = ArrayList<MangaPage>(pageElements.size)
		for (pageElement in pageElements) {
			mangaPageList.add(MangaPage(pageElement.attr("data-redirect"), this.javaClass))
		}

		return mangaPageList
	}

	override fun getPageImage(mangaPage: MangaPage?): String {
		return getPage(mangaPage!!.path).selectFirst(".wp-manga-chapter-img").attr("src")
	}

	override fun search(query: String?, page: Int): MangaList? {
		//prepare url
		val escapedQuery = URLEncoder.encode(query, "UTF-8")
		//pagination url
		var pageStr = ""
		if (page > 1) {
			pageStr = "page/${page + 1}/"
		}
		val root = getPage("https://mangaread.co/$pageStr?s=$escapedQuery&post_type=wp-manga")
		val mangaList = MangaList()
		val entries = root.select(".c-tabs-item__content")

		entries.forEach {
			parseSearchEntry(it).apply {
				mangaList.add(this)
			}
		}

		return mangaList
	}

	private fun parseSearchEntry(entry: Element): MangaInfo {
		return MangaInfo().apply {
			val titleElement = entry.select(".post-title > h3 > a")[0]
			name = titleElement.text()
			subtitle = entry.select(".post-content_item.mg_alternative > .summary-heading")[0].text()
			genres = entry.select(".mg_genres > .summary-content > a").joinToString { it.text() }
			path = titleElement.attr("href")
			preview = entry.select(".c-image-hover > a > img").attr("src")
			status = statusTextToInt(entry.select(".mg_status > .summary-content")[0].text())
			rating = ratingToByte(entry.selectFirst(".total_votes").text())
			id = path.hashCode()
		}.also {
			it.provider = this.javaClass
		}
	}

	private fun getUrl(genre: Int, page: Int, sort: Int): String = genres[genre].getUrl(page + 1, sorts[sort])

	private fun extractDataFromMangaElement(element: Element): MangaInfo {
		val titleHref = element.select("a")[0]
		val name = titleHref.attr("title")
		val link = titleHref.attr("href")
		val img = titleHref.select("img")[0].attr("src")

		//additional info
		val rating = element.select(".score.total_votes")[0].text()

		return MangaInfo(name, "", link, img).also {
			it.provider = javaClass
			it.rating = ratingToByte(rating)
			it.id = it.path.hashCode()
		}
	}

	private fun extractChapterFromElement(element: Element, number: Int): MangaChapter {
		val mangaChapter = MangaChapter()
		val href = element.select("a")
		mangaChapter.name = href.text()
		mangaChapter.number = number
		mangaChapter.provider = this.javaClass
		mangaChapter.readLink = href.attr("href")
		return mangaChapter
	}

	private fun ratingToByte(rating: String) : Byte{
		val ratingFloat = rating.toFloatOrNull() ?: 0f
		return (ratingFloat * 20).toByte()
	}
	private fun parseStatus(rootPageElement: Element): Int {
		return statusTextToInt(rootPageElement.select(".post-status > .post-content_item")[1].text())
	}

	private fun statusTextToInt(statusText: String?): Int {
		return when (statusText) {
			"OnGoing" -> MangaInfo.STATUS_ONGOING
			"Completed" -> MangaInfo.STATUS_COMPLETED
			else -> MangaInfo.STATUS_UNKNOWN
		}
	}

	override fun getSortTitles(context: Context?): Array<String>? = AppHelper.getStringArray(context, sortsIndexes)
	override fun getGenresTitles(context: Context?): Array<String>? = AppHelper.getStringArray(context, genreIndexes)

	override fun hasGenres(): Boolean = true
	override fun hasSort(): Boolean = true
	override fun isSearchAvailable(): Boolean = true
}