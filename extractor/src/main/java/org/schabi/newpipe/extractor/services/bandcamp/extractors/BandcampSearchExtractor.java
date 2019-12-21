// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampSearchExtractor extends SearchExtractor {

    public BandcampSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getSearchSuggestion() {
        return null;
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        // okay apparently this is where we DOWNLOAD the page and then COMMIT its ENTRIES to an INFOITEMPAGE
        String html = getDownloader().get(pageUrl).responseBody();

        InfoItemsSearchCollector collector = getInfoItemSearchCollector();


        Document d = Jsoup.parse(html);

        Elements searchResultsElements = d.getElementsByClass("searchresult");

        for (Element searchResult :
                searchResultsElements) {

            Element resultInfo = searchResult.getElementsByClass("result-info").first();

            String type = resultInfo
                    .getElementsByClass("itemtype").first().text();

            String image = null;
            Element img = searchResult.getElementsByClass("art").first()
                    .getElementsByTag("img").first();
            if (img != null) {
                image = img.attr("src");
            }

            String heading = resultInfo.getElementsByClass("heading").text();

            String subhead = resultInfo.getElementsByClass("subhead").text();

            String url = resultInfo.getElementsByClass("itemurl").text();

            switch (type) {
                default:
                    continue;
                case "FAN":
                    //collector.commit Channel (?) with heading, url, image
                    break;

                case "ARTIST":
                    String id = resultInfo.getElementsByClass("itemurl").first()
                            .getElementsByTag("a").first()
                            .attr("href") // the link contains the id
                            .split("search_item_id=")
                            [1] // the number is behind its name
                            .split("&") // there is another attribute behind the name
                            [0]; // get the number

                    //searchResults.add(new Artist(heading, Long.parseLong(id), image, subhead));
                    //collector.commit Channel with heading, id, image, subhead
                    break;

                case "ALBUM":
                    String artist = subhead.split(" by")[0];
                    //searchResults.add(new Album(heading, artist, url, image));
                    //collector.commit Playlist with heading, artist, url, image
                    break;

                case "TRACK":
                    String album = subhead.split("from ")[0].split(" by")[0];

                    String[] splitBy = subhead.split(" by");
                    String artist1 = null;
                    if (splitBy.length > 1) {
                        artist1 = subhead.split(" by")[1];
                    }
                    collector.commit(new BandcampStreamInfoItemExtractor(heading, url, image, artist1, album));
                    break;
            }

        }


        return new InfoItemsPage<>(getInfoItemSearchCollector(), null);
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getUrl());//new InfoItemsPage<>(getInfoItemSearchCollector(), null);
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

    }
}