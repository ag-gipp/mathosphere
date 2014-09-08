package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.util.Collector;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Generates Arxiv Document cleaner plan.
 */
public class ArxivCleaner extends Cleaner {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(ArxivCleaner.class);
    /**
	 * Pattern for extracting the filename from the ARXIV tag.
	 */
	private static final Pattern FILENAME_PATTERN = Pattern
	         .compile("<ARXIVFILESPLIT.*?Filename=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">");

    private static final String DELIM = "</ARXIVFILESPLIT>";

    @Override
    public String getDelimiter() {
        return DELIM;
    }

    @Override
    public void flatMap(String in, Collector<RawDataTuple> out) {
        String doc = in;
        if (doc.length() < 200 && doc.trim().isEmpty()) {
            return;
        }

        final Matcher matcher = FILENAME_PATTERN.matcher(doc);
        String docID = "this_was_null";
        if (matcher.find()) {
            docID = matcher.group(1) + '_' + matcher.group(2) + '_' + matcher.group(3);
        } else {
            LOG.warn("Null docID, assigning this_was_null: ", doc);
        }
        //Strip Arxiv line
        try {
            doc = doc.substring(doc.indexOf("<?xml"));
        } catch (final StringIndexOutOfBoundsException e) {
            LOG.warn("Badly formatted xml title, exiting: ", doc);
        }
        doc = HtmlUtils.htmlUnescape(doc);
        out.collect(new RawDataTuple(docID, doc));
    }

}
