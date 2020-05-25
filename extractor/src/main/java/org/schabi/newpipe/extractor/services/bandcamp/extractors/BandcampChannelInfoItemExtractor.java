// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class BandcampChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private String name, url, image, location;

    public BandcampChannelInfoItemExtractor(Element searchResult) {

        Element resultInfo = searchResult.getElementsByClass("result-info").first();

        Element img = searchResult.getElementsByClass("art").first()
                .getElementsByTag("img").first();
        if (img != null) {
            image = img.attr("src");
        }

        name = resultInfo.getElementsByClass("heading").text();

        location = resultInfo.getElementsByClass("subhead").text();

        url = resultInfo.getElementsByClass("itemurl").text();
    }

    @Override
    public String getName() throws ParsingException {
        return name;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return image;
    }

    @Override
    public String getDescription() {
        return location;
    }

    @Override
    public long getSubscriberCount() {
        return -1;
    }

    @Override
    public long getStreamCount() {
        return -1;
    }
}