// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory;

import static org.junit.Assert.*;

/**
 * Test for {@link BandcampChannelLinkHandlerFactory}
 */
public class BandcampChannelLinkHandlerFactoryTest {
    private static BandcampChannelLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = new BandcampChannelLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        // Bandcamp URLs
        assertTrue(linkHandler.acceptUrl("http://zachbenson.bandcamp.com"));
        assertTrue(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/"));
        assertTrue(linkHandler.acceptUrl("https://billwurtz.bandcamp.com/releases"));

        assertTrue(linkHandler.acceptUrl("http://zachbenson.bandcamp.com/"));

        assertFalse(linkHandler.acceptUrl("https://bandcamp.com"));
        assertFalse(linkHandler.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"));
        assertFalse(linkHandler.acceptUrl("https://daily.bandcamp.com/"));
        assertFalse(linkHandler.acceptUrl("https://daily.bandcamp.com/best-of-2020/bandcamp-daily-staffers-on-their-favorite-albums-of-2020"));

        // External URLs
        assertTrue(linkHandler.acceptUrl("http://interovgm.com/releases/"));
        assertTrue(linkHandler.acceptUrl("https://interovgm.com/releases"));

        assertFalse(linkHandler.acceptUrl("https://example.com/releases"));
    }

    @Test
    public void testGetId() throws ParsingException {
        assertEquals("1196681540", linkHandler.getId("https://macbenson.bandcamp.com/"));
        assertEquals("1196681540", linkHandler.getId("http://macbenson.bandcamp.com/"));
        assertEquals("1581461772", linkHandler.getId("https://interovgm.com/releases"));
        assertEquals("3321800855", linkHandler.getId("https://infiniteammo.bandcamp.com/"));
        assertEquals("3775652329", linkHandler.getId("https://npet.bandcamp.com/"));
    }

    @Test
    public void testGetUrl() throws ParsingException {
        assertEquals("https://macbenson.bandcamp.com", linkHandler.getUrl("1196681540"));
        assertEquals("https://interovgm.com", linkHandler.getUrl("1581461772"));
        assertEquals("https://infiniteammo.bandcamp.com", linkHandler.getUrl("3321800855"));
    }

    @Test(expected = ParsingException.class)
    public void testGetUrlWithInvalidId() throws ParsingException {
        linkHandler.getUrl("0");
    }

    @Test(expected = ParsingException.class)
    public void testGetIdWithInvalidUrl() throws ParsingException {
        linkHandler.getId("https://bandcamp.com");
    }

}
