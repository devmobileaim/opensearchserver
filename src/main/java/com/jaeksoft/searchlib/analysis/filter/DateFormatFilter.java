/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.analysis.filter;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.FormatUtils;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatFilter extends FilterFactory {

    private final String OUTPUT_DEFAULT_FORMAT = "dd-MM-yyyy";

    private String output_format = OUTPUT_DEFAULT_FORMAT;

    @Override
    public void initProperties() throws SearchLibException {
        super.initProperties();
        addProperty(ClassPropertyEnum.OUTPUT_DATE_FORMAT, OUTPUT_DEFAULT_FORMAT, null, 20, 1);
    }

    @Override
    public void checkValue(ClassPropertyEnum prop, String value) throws SearchLibException {
        if (value == null || value.length() == 0)
            return;
        if (prop == ClassPropertyEnum.OUTPUT_DATE_FORMAT) {
            new SimpleDateFormat(value);
            output_format = value;
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DateFormatTermFilter(tokenStream, output_format);
    }

    private static DateFormat newDateFormat(String formatPattern) {
        DateFormat format = new SimpleDateFormat(formatPattern);

        return format;
    }

    public class DateFormatTermFilter extends AbstractTermFilter {

        private final DateFormat outputDateFormat;

        public DateFormatTermFilter(TokenStream input, String outputFormat) {
            super(input);
            outputDateFormat = newDateFormat(outputFormat);
        }

        FormatUtils.ThreadSafeSimpleDateFormat[] inputFormats = new FormatUtils.ThreadSafeSimpleDateFormat[]{
                new FormatUtils.ThreadSafeSimpleDateFormat("dd 'de' MMMMMM 'de' yyyy", new Locale("pt")),
                new FormatUtils.ThreadSafeSimpleDateFormat("dd MM yy", new Locale("en")),
                new FormatUtils.ThreadSafeSimpleDateFormat("MM dd yyyy", new Locale("en"))
        };

        @Override
        public final boolean incrementToken() throws IOException {
            if (!input.incrementToken())
                return false;
            Date date = null;
            for (FormatUtils.ThreadSafeDateFormat inputFormat : inputFormats) {
                try {
                    date = inputFormat.parse(termAtt.toString().toLowerCase());
                    break;
                } catch (ParseException e) {
                }
            }
            // ignore date older or later of 50 years from current date
            long diffTime = (date == null)?0:System.currentTimeMillis() - date.getTime();
            if (date != null && (diffTime < 50L * 365L * 86400000L)
                    && (diffTime > -5L * 365L * 86400000L))  {
                String term = outputDateFormat.format(date);
                if (term == null) {
                    term = outputDateFormat.format(new Date());
                }
                if (term != null) {
                    createToken(term);
                }
            } else {
                String term = outputDateFormat.format(new Date());
                createToken(term);
            }

            return true;
        }

    }

    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'DE' MMMMMM 'DE' yyyy", new Locale("pt"));
        ParsePosition parsePosition = new ParsePosition(0);
        Date date = dateFormat.parse("29 DE MARÇO DE 2017", parsePosition);
        System.out.println(date);
        dateFormat = new SimpleDateFormat("MM dd yyyy", new Locale("en"));
        parsePosition = new ParsePosition(0);
        date = dateFormat.parse("12 01 2017", parsePosition);
        if (Math.abs(date.getTime() - System.currentTimeMillis()) < 5L * 365L * 86400000L) {
            System.out.println(date);
        }
    }
}
