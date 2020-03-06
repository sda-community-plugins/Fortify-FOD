package com.serena.air.plugin.fod.parser.converters

import com.beust.jcommander.IStringConverter
import com.fortify.fod.parser.BsiToken
import com.fortify.fod.parser.BsiTokenParser

class BsiTokenConverter implements IStringConverter<BsiToken> {

    private static BsiTokenParser parser = new BsiTokenParser();

    @Override
    public BsiToken convert(String value) {
        try {
            return parser.parse(value);
        } catch (URISyntaxException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
