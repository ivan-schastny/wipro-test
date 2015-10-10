package com.wipro.crawler;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;


public class WebLink {
    private String url;
    private LinkType linkType;

    public WebLink(String url, LinkType linkType) {
        this.url = url;
        this.linkType = linkType;
    }

    public String getUrl() {
        return url;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(url).append(linkType).build();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WebLink)) {
            return false;
        }
        WebLink other = (WebLink) obj;
        return Objects.equals(url, other.url) && Objects.equals(linkType, other.linkType);
    }

    @Override
    public String toString() {
        return "WebLink{" +
                "url='" + url + '\'' +
                ", linkType=" + linkType +
                '}';
    }

    public enum LinkType {
        INTERNAL, EXTERNAL, STATIC, OTHER
    }
}
