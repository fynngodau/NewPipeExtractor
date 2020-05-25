// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampRadioExtractor}
 */
public class BandcampRadioExtractorTest {

    private static BandcampRadioExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampRadioExtractor) Bandcamp
                .getKioskList()
                .getExtractorById("Radio", null);
    }

    @Test
    public void testRadioCount() throws ExtractionException, IOException {
        List<InfoItem> list = Bandcamp.getKioskList().getExtractorById("Radio", null).getInitialPage().getItems();
        assertTrue(list.size() > 300);
    }
}