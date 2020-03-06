package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

/**
 * A YoutubePlaylistExtractor for a mix (auto-generated playlist).
 * It handles urls in the format of "youtube.com/watch?v=videoId&list=playlistId"
 */
public class YoutubeMixPlaylistExtractor extends PlaylistExtractor {

  private JsonObject initialData;
  private Document doc;

  public YoutubeMixPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
    super(service, linkHandler);
  }

  @Override
  public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
    final String url = getUrl() + "&pbj=1";
    final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());
    initialData = ajaxJson.getObject(3).getObject("response");
    JsonObject a = initialData.getObject("contents");
    JsonObject b = initialData.getObject("contents").getObject("twoColumnWatchNextResults");
    JsonObject c = initialData.getObject("contents");
    JsonObject playlist = initialData.getObject("contents").getObject("twoColumnWatchNextResults").getObject("playlist").getObject("playlist");
    System.out.println();
  }

  @Nonnull
  @Override
  public String getName() throws ParsingException {
    try {
      return doc.select("div[class=\"playlist-info\"] h3[class=\"playlist-title\"]").first().text();
    } catch (Exception e) {
      throw new ParsingException("Could not get playlist name", e);
    }
  }

  @Override
  public String getThumbnailUrl() throws ParsingException {
    try {
      Element li = doc.select("ol[class*=\"playlist-videos-list\"] li").first();
      String videoId = li.attr("data-video-id");
      if (videoId != null && !videoId.isEmpty()) {
        //higher quality
        return getThumbnailUrlFromId(videoId);
      } else {
        //lower quality
        return doc.select("ol[class*=\"playlist-videos-list\"] li").first()
            .attr("data-thumbnail-url");
      }
    } catch (Exception e) {
      throw new ParsingException("Could not get playlist thumbnail", e);
    }
  }

  @Override
  public String getBannerUrl() {
    return "";
  }

  @Override
  public String getUploaderUrl() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public String getUploaderName() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public String getUploaderAvatarUrl() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public long getStreamCount() {
    // Auto-generated playlist always start with 25 videos and are endless
    // But the html doesn't have a continuation url
    return doc.select("ol[class*=\"playlist-videos-list\"] li").size();
  }

  @Nonnull
  @Override
  public InfoItemsPage<StreamInfoItem> getInitialPage() {
    StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
    Element ol = doc.select("ol[class*=\"playlist-videos-list\"]").first();
    collectStreamsFrom(collector, ol);
    return new InfoItemsPage<>(collector, getNextPageUrl());
  }

  @Override
  public String getNextPageUrl() {
    return "";
  }

  @Override
  public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) {
    //Continuations are not implemented
    return null;
  }

  private void collectStreamsFrom(
      @Nonnull StreamInfoItemsCollector collector,
      @Nullable Element element) {
    collector.reset();

    if (element == null) {
      return;
    }

    final LinkHandlerFactory streamLinkHandlerFactory = getService().getStreamLHFactory();
    final TimeAgoParser timeAgoParser = getTimeAgoParser();

//    for (final Element li : element.children()) {
//
//      collector.commit(new YoutubeStreamInfoItemExtractor(li, timeAgoParser) {
//
//        @Override
//        public boolean isAd() {
//          return false;
//        }
//
//        @Override
//        public String getUrl() throws ParsingException {
//          try {
//            return streamLinkHandlerFactory.fromId(li.attr("data-video-id")).getUrl();
//          } catch (Exception e) {
//            throw new ParsingException("Could not get web page url for the video", e);
//          }
//        }
//
//        @Override
//        public String getName() throws ParsingException {
//          try {
//            return li.attr("data-video-title");
//          } catch (Exception e) {
//            throw new ParsingException("Could not get name", e);
//          }
//        }
//
//        @Override
//        public long getDuration() {
//          //Not present in doc
//          return 0;
//        }
//
//        @Override
//        public String getUploaderName() throws ParsingException {
//          String uploaderName = li.attr("data-video-username");
//          if (uploaderName == null || uploaderName.isEmpty()) {
//            throw new ParsingException("Could not get uploader name");
//          } else {
//            return uploaderName;
//          }
//        }
//
//        @Override
//        public String getUploaderUrl() {
//          //Not present in doc
//          return "";
//        }
//
//        @Override
//        public String getTextualUploadDate() {
//          //Not present in doc
//          return "";
//        }
//
//        @Override
//        public long getViewCount() {
//          return -1;
//        }
//
//        @Override
//        public String getThumbnailUrl() throws ParsingException {
//          try {
//            return getThumbnailUrlFromId(streamLinkHandlerFactory.fromUrl(getUrl()).getId());
//          } catch (Exception e) {
//            throw new ParsingException("Could not get thumbnail url", e);
//          }
//        }
//      });
//    }
  }

  private String getThumbnailUrlFromId(String videoId) {
    return "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";
  }
}
