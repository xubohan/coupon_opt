package com.hexin.gift.interfaces.rest.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple value object parsed from attr string, e.g. "period:30".
 */
public class SellInfoAttr {

    private final Map<String, String> attributes;

    private SellInfoAttr(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public static SellInfoAttr parse(String attrText) {
        Map<String, String> attrs = new HashMap<>();
        if (attrText == null || attrText.trim().isEmpty()) {
            return new SellInfoAttr(attrs);
        }
        String[] tokens = attrText.split("\\|");
        for (String token : tokens) {
            String[] kv = token.split(":", 2);
            if (kv.length == 2) {
                attrs.put(kv[0].trim(), kv[1].trim());
            }
        }
        return new SellInfoAttr(attrs);
    }

    public String getValue(String key) {
        return attributes.get(key);
    }

    public Map<String, String> asMap() {
        return new HashMap<>(attributes);
    }
}
