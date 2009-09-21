package com.haystacksoftware.crashreportserver;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

public interface Transaction {
    HttpMethod getMethod();
    Pattern getPattern();
    void execute(Matcher m) throws ServletException, IOException;
}
