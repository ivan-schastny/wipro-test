package com.wipro.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebPageLoader {
    private static final Logger log = LoggerFactory.getLogger(WebPageLoader.class);

    private static final Pattern urlPattern = Pattern.compile(".*href=[\",'](.*?)[\",',?].*");
    private static final Pattern typePattern = Pattern.compile(".*type=[\",'](.*?)[\",'].*");
    private static final Pattern srcPattern = Pattern.compile(".*src=[\",'](.*?)[\",',?].*");
    private static final Pattern relPattern = Pattern.compile(".*rel=[\",'](.*?)[\",'].*");
    private static final Pattern linkPattern = Pattern.compile("(<link .*?>)");
    private static final Pattern aPattern = Pattern.compile("(<a .*?>)");
    private static final Pattern imgPattern = Pattern.compile("(<img .*?>)");

    public Set<WebLink>  loadAndExtractLinks(String urlAddress, String baseUrl) throws IOException {
        URL ur = new URL(urlAddress);
        HttpURLConnection yc =(HttpURLConnection) ur.openConnection();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return extractLinks(stringBuilder.toString(), baseUrl);
    }

    public Set<WebLink> extractLinks(String page, String baseUrl) {
        Set<WebLink> result = new HashSet<>();
        result.addAll(extractLinksFromPage(linkPattern, page, baseUrl));
        result.addAll(extractLinksFromPage(aPattern, page, baseUrl));
        result.addAll(extractsImages(page));
        return result;
    }

    private Set<WebLink> extractLinksFromPage(Pattern pattern, String page, String baseUrl) {
        Matcher matcher = pattern.matcher(page);
        Set<WebLink> result = new HashSet<>();
        while (matcher.find()) {
            String linkContents = matcher.group(1);
            Matcher urlMatcher = urlPattern.matcher(linkContents);
            Matcher typeMatcher = typePattern.matcher(linkContents);
            Matcher relMatcher = relPattern.matcher(linkContents);
            if (urlMatcher.matches()) {
                String url = urlMatcher.group(1);
                if (isAnchor(url)) {
                    continue;
                }
                String type = typeMatcher.matches() ? typeMatcher.group(1) : null;
                String rel = relMatcher.matches() ? relMatcher.group(1) : null;
                WebLink.LinkType linkType = getLinkType(url, baseUrl, type, rel);
                result.add(new WebLink(url, linkType));
            } else {
                log.debug("Invalid link " + linkContents + ". Skipping.");
            }
        }
        return result;
    }

    public Set<WebLink> extractsImages(String page) {
        Matcher matcher = imgPattern.matcher(page);
        Set<WebLink> result = new HashSet<>();
        while (matcher.find()) {
            String linkContents = matcher.group(1);
            Matcher srcMatcher = srcPattern.matcher(linkContents);
            if (srcMatcher.matches()) {
                String url = srcMatcher.group(1);
                result.add(new WebLink(url, WebLink.LinkType.STATIC));
            } else {
                log.debug("Invalid link " + linkContents + ". Skipping.");
            }
        }
        return result;
    }

    public static WebLink.LinkType getLinkType(String url, String baseUrl, String type, String rel) {
        if (isCss(url, rel) || isImage(url, type)) {
            return WebLink.LinkType.STATIC;
        } else if (isInternalUrl(url, baseUrl, type)) {
            return WebLink.LinkType.INTERNAL;
        } else if (isExternalUrl(url, baseUrl, type)) {
            return WebLink.LinkType.EXTERNAL;
        } else {
            return WebLink.LinkType.OTHER;
        }
    }

    public static boolean isAnchor(String url) {
        return url.startsWith("#") || url.contains("/#");
    }

    public static boolean isImage(String url, String type) {
        return (type != null && type.startsWith("image/"))
                || url.contains(".png")
                || url.contains(".jpeg")
                || url.contains(".jpg");
    }

    public static boolean isCss(String url, String rel) {
        return Objects.equals(rel,"stylesheet")
                || url.endsWith(".css");
    }

    public static boolean isInternalUrl(String url, String baseUrl, String type) {
        return (type == null || type.equals("text/html")) && url.startsWith(baseUrl);
    }

    public static boolean isExternalUrl(String url, String baseUrl, String type) {
        return (type == null || type.equals("text/html")) && !url.startsWith(baseUrl);
    }
}