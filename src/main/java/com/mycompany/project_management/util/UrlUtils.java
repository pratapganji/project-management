package com.mycompany.project_management.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class UrlUtils {

    public static String extractBaseUrl(String urlString) throws URISyntaxException {
        URI uri = new URI(urlString);
        String scheme = uri.getScheme();         // e.g., "https"
        String authority = uri.getAuthority();   // e.g., "olympus.api.bulk.dev.cloudgsl.nam.nsroot.net"
        if (scheme == null || authority == null) {
            throw new IllegalArgumentException("Invalid URL: missing scheme or authority");
        }
        return scheme + "://" + authority;
    }
}
