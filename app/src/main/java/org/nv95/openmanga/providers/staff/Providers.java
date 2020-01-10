package org.nv95.openmanga.providers.staff;

import androidx.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.online.DesuMeProvider;
import org.nv95.openmanga.providers.online.EHentaiProvider;
import org.nv95.openmanga.providers.online.HentaichanProvider;
import org.nv95.openmanga.providers.online.MangaFoxProvider;
import org.nv95.openmanga.providers.online.MangaReadProvider;
import org.nv95.openmanga.providers.online.MangaReaderProvider;
import org.nv95.openmanga.providers.online.MangaTownProvider;
import org.nv95.openmanga.providers.online.MangachanProvider;
import org.nv95.openmanga.providers.online.MintMangaProvider;
import org.nv95.openmanga.providers.online.PuzzmosProvider;
import org.nv95.openmanga.providers.online.ReadmangaRuProvider;
import org.nv95.openmanga.providers.online.ScanFRProvider;
import org.nv95.openmanga.providers.online.SelfmangaRuProvider;
import org.nv95.openmanga.providers.online.TruyenTranhProvider;
import org.nv95.openmanga.providers.online.YaoiChanProvider;

/**
 * Created by nv95 on 27.07.16.
 */

public class Providers {

    private static final ProviderSummary[] mAllProviders = {
            new ProviderSummary(0, "ReadManga", ReadmangaRuProvider.class, Languages.RU, R.xml.pref_readmanga),
            new ProviderSummary(1, "MintManga", MintMangaProvider.class, Languages.RU, R.xml.pref_readmanga),
            new ProviderSummary(2, "Манга-тян", MangachanProvider.class, Languages.RU, R.xml.pref_anychan),
            new ProviderSummary(3, "Desu.me", DesuMeProvider.class, Languages.RU, 0),
            new ProviderSummary(4, "SelfManga", SelfmangaRuProvider.class, Languages.RU, R.xml.pref_readmanga),
            new ProviderSummary(5, "MangaFox", MangaFoxProvider.class, Languages.EN, 0),
            new ProviderSummary(6, "MangaTown", MangaTownProvider.class, Languages.EN, 0),
            new ProviderSummary(7, "MangaReader", MangaReaderProvider.class, Languages.EN, 0),
            new ProviderSummary(8, "E-Hentai", EHentaiProvider.class, Languages.MULTI, R.xml.pref_ehentai),
            new ProviderSummary(9, "PuzzManga", PuzzmosProvider.class, Languages.TR, 0),
            new ProviderSummary(10, "Яой-тян", YaoiChanProvider.class, Languages.RU, R.xml.pref_anychan),
            new ProviderSummary(11, "TruyenTranh", TruyenTranhProvider.class, Languages.VIE, 0),
            new ProviderSummary(12, "Хентай-тян", HentaichanProvider.class, Languages.RU, R.xml.pref_henchan),
            new ProviderSummary(13, "ScanFR", ScanFRProvider.class, Languages.FR, 0),
            new ProviderSummary(14, "MangaRead", MangaReadProvider.class, Languages.EN, 0)
    };

    public static ProviderSummary[] getAll() {
        return mAllProviders;
    }

    @Nullable
    public static ProviderSummary getById(int id) {
        for (ProviderSummary o : mAllProviders) {
            if (id == o.id) {
                return o;
            }
        }
        return null;
    }

    public static int getCount() {
        return mAllProviders.length;
    }
}
