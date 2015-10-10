package com.wipro.crawler;

import java.util.HashMap;
import java.util.Map;


public class WebPage {
    private String url;
    private Map<WebLink, WebPage> siteMap = new HashMap<>();

    public WebPage(String url) {
        this.url = url;
    }

    public Map<WebLink, WebPage> getSiteMap() {
        return new HashMap<>(siteMap);
    }

    public String getUrl() {
        return url;
    }

    public void addLink(WebLink link, WebPage page) {
        siteMap.put(link, page);
    }

    @Override
    public String toString() {
        return "WebPage{" +
                "siteMap=" + siteMap +
                ", url='" + url + '\'' +
                '}';
    }
}
