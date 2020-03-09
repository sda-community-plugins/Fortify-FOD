package com.serena.air.plugin.fod.parser.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.fortify.fod.parser.BsiTokenParser;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class BsiTokenValidator implements IParameterValidator {

    private static BsiTokenParser parser = new BsiTokenParser();

    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            parser.parse(value);
        } catch (URISyntaxException e) {
            throw new ParameterException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ParameterException(e.getMessage());
        }
    }
}
