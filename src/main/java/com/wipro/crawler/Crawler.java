package com.wipro.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Crawler {
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            log.warn("Please pass web url as a program argument!");
            return;
        }

        String url = args[0];
        Crawler crawler = new Crawler();
        WebPage result;
        try {
            result = crawler.buildMap(url, url, System.getProperty("mode", "").equals("deep"));
        } catch (IOException e) {
            log.error("Error building the site map.", e);
            return;
        }
        StringBuilder builder = new StringBuilder();
        prettyPrent(result, builder, "");
        log.info(builder.toString());
    }

    public WebPage buildMap(String url, String baseUrl, boolean deep) throws IOException {
        return buildMap(url, baseUrl, deep, new HashSet<String>());
    }

    public WebPage buildMap(String url, String baseUrl, boolean deep, Set<String> processedLinks) throws IOException {
        WebPage result = new WebPage(url);
        Set<WebLink> links;
        try {
            links = new WebPageLoader().loadAndExtractLinks(url, baseUrl);
        } catch (Exception e) {
            log.info("Error processing URL", e);
            return result;
        }
        log.info("Processing page " + url);
        for (WebLink link : links) {
            switch (link.getLinkType()) {
                case INTERNAL:
                    if (deep) {
                        if (processedLinks.add(link.getUrl())) {
                            result.addLink(link, buildMap(link.getUrl(), baseUrl, true, processedLinks));
                        } else {
                            result.addLink(link, null);
                        }
                    } else {
                        result.addLink(link, new WebPage(link.getUrl()));
                    }
                    break;
                case EXTERNAL:
                case STATIC:
                case OTHER:
                    result.addLink(link, new WebPage(link.getUrl()));
                    break;
            }
        }
        return result;
    }

    private static void prettyPrent(WebPage webPage, StringBuilder builder, String identation) {
        if (!webPage.getSiteMap().isEmpty()) {
            for (Map.Entry<WebLink, WebPage> entry : webPage.getSiteMap().entrySet()) {
                WebLink webLink = entry.getKey();
                WebPage targetPage = entry.getValue();
                builder.append("\n").append(identation).append(webLink.getLinkType()).append(" - ").append(webLink.getUrl());
                if (targetPage == null) {
                    builder.append(" - VISITED");
                } else {
                    prettyPrent(targetPage, builder, identation + "\t");
                }
            }
        }
    }
}