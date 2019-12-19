package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;

    public SoundcloudStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(downloader, getOriginalUrl());

        String policy = track.getString("policy", "");
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return track.getInt("id") + "";
    }

    @Nonnull
    @Override
    public String getName() {
        return track.getString("title");
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() {
        return track.getString("created_at");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDate(getTextualUploadDate()));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        String artworkUrl = track.getString("artwork_url", "");
        if (artworkUrl.isEmpty()) {
            artworkUrl = track.getObject("user").getString("avatar_url", "");
        }
        String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
        return artworkUrlBetterResolution;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return track.getString("description");
    }

    @Override
    public int getAgeLimit() {
        return NO_AGE_LIMIT;
    }

    @Override
    public long getLength() {
        return track.getNumber("duration", 0).longValue() / 1000L;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
    }

    @Override
    public long getViewCount() {
        return track.getNumber("playback_count", 0).longValue();
    }

    @Override
    public long getLikeCount() {
        return track.getNumber("favoritings_count", -1).longValue();
    }

    @Override
    public long getDislikeCount() {
        return -1;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return SoundcloudParsingHelper.getUploaderUrl(track);
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return SoundcloudParsingHelper.getUploaderName(track);
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return SoundcloudParsingHelper.getAvatarUrl(track);
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        List<AudioStream> audioStreams = new ArrayList<>();
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api.soundcloud.com/i1/tracks/" + urlEncode(getId()) + "/streams"
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId()) //+ "/streams"
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        String response = dl.get(apiUrl, getExtractorLocalization()).responseBody();
        JsonObject responseObject;
        try {
            responseObject = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        // streams can now be streamable and downloadable - or explicitly not
        // for playing the track, it is only necessary to have a streamable track;
        // if this is not the case, this track might not be published yet
        if (!responseObject.getBoolean("streamable")) return audioStreams;

        try {
            JsonArray transcodings = responseObject.getObject("media").getArray("transcodings");

            // get information about what stream formats are available
            for (Object transcoding : transcodings) {

                JsonObject t = (JsonObject) transcoding;
                String url = t.getString("url");

                if (url != null && !url.isEmpty()) {
                    // this url points to the endpoint which generates a unique and short living url to a m3u file
                    // we need to move this into a separate method to generate valid urls when needed (e.g. resuming a stream)
                    url += "?client_id=" + SoundcloudParsingHelper.clientId();
                    String res = dl.get(url).responseBody();
                    JsonObject jsonObject2;
                    try {
                        jsonObject2= JsonParser.object().from(res);
                    } catch (JsonParserException e) {
                        throw new ParsingException("Could not parse streamable url", e);
                    }


                    // links in the m3u file are also only valid for a short period
                    String m3uUrl = jsonObject2.getString("url");

                    // a complete list of all formats is missing, we need to check if there are more in use
                    if (t.getString("preset").contains("mp3")) {
                        // there are multiple presets containing mp3:
                        // mp3_0_0, mp3_0_1,
                        // I did not have the time to check the difference
                        audioStreams.add(new AudioStream(m3uUrl, MediaFormat.MP3, 128));
                    } else if (t.getString("preset").contains("opus")) {
                        // I could not verify the opus bitrate
                        audioStreams.add(new AudioStream(m3uUrl, MediaFormat.OPUS, 128));
                    }

                }

            }

        } catch (NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's track audio url", e);
        }

        return audioStreams;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId()) + "/related"
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
