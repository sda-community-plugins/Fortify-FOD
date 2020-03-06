package com.serena.air.plugin.fod.parser.converters

import com.beust.jcommander.IStringConverter

public class FileConverter implements IStringConverter<File> {

    @Override
    public File convert(String value) {
        return new File(value);
    }
}
