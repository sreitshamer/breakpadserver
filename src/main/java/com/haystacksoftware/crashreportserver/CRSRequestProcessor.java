package com.haystacksoftware.crashreportserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

public class CRSRequestProcessor {
    private static final Logger logger = Logger.getLogger(CRSRequestProcessor.class);
    private static final Pattern NEW_CR = Pattern.compile("^/([^/]+)/new");
    private Set<Transaction> transactions = new HashSet<Transaction>();
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String uri;
    private String mainURI;
    
    public CRSRequestProcessor(HttpServletRequest arg0, HttpServletResponse arg1) {
        request = arg0;
        response = arg1;
        uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        mainURI = contextPath;
        if (mainURI.length() == 0) {
            mainURI = "/";
        }
        int contextPathLen = contextPath.length();
        if (uri.length() >= contextPathLen) {
            uri = uri.substring(contextPathLen);
        }

        transactions.add(new TransactionImpl(HttpMethod.POST, NEW_CR) {
            public void execute(Matcher m) throws ServletException, IOException {
                postNewCR(m.group(1));
            }
        });
    }
    public void service() throws ServletException, IOException {
        for (Transaction txn : transactions) {
            Matcher m = txn.getPattern().matcher(uri);
            if (txn.getMethod().toString().equals(request.getMethod()) && m.find()) {
                try {
                    txn.execute(m);
                } catch(ServletException e) {
                    logger.error(e);
                    throw e;
                } catch(IOException e) {
                    logger.error(e);
                    throw e;
                }
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    void postNewCR(String appName) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(100*1024*1024);
        String uuid = UUID.randomUUID().toString();
        File dir = new File("/tmp/crashreports/" + appName + "/" + uuid);
        dir.mkdirs();
        try {
            List<?> items = upload.parseRequest(request);
            Iterator<?> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                File itemDir = new File(dir, item.getFieldName());
                if (item.isFormField()) {
                    writeString(item.getString(), itemDir, "string");
                } else {
                    writeString(item.getFieldName(), itemDir, "fieldName");
                    writeString(item.getName(), itemDir, "name");
                    writeString(item.getContentType(), itemDir, "contentType");
                    writeString(new Long(item.getSize()).toString(), itemDir, "size");
                    File dataFile = new File(itemDir, "data");
                    item.write(dataFile);
                }
            }
        } catch(Exception e) {
            throw new IOException(e);
        }
    }
    private void writeString(String string, File itemDir, String fileName) throws IOException {
        File dataFile = new File(itemDir, fileName);
        FileOutputStream fos = new FileOutputStream(dataFile);
        fos.write(string.getBytes("ASCII"));
        fos.flush();
        fos.close();
    }
}
