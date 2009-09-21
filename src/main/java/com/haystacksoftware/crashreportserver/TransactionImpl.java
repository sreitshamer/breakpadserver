package com.haystacksoftware.crashreportserver;

import java.util.regex.Pattern;

abstract class TransactionImpl implements Transaction {
    HttpMethod method;
    Pattern pattern;
    TransactionImpl(HttpMethod theMethod, Pattern thePattern) {
        method = theMethod;
        pattern = thePattern;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public Pattern getPattern() {
        return pattern;
    }
}