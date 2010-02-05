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

import javax.servlet.ServletContext;
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
    private CRSServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String uri;
    private String mainURI;
    
    public CRSRequestProcessor(CRSServlet theServlet, HttpServletRequest arg0, HttpServletResponse arg1) {
        servlet = theServlet;
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
    // Overly complicated request routing mechanism copied/pasted from another larger project.
    // Reluctant to delete it because it works.
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
        logger.debug("postNewCR");
        if (!ServletFileUpload.isMultipartContent(request)) {
            logger.debug("not multipart!");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(10*1024*1024);
        String uuid = UUID.randomUUID().toString();
        ServletContext sc = servlet.getServletContext();
        CrashReport cr = new CrashReport(appName, uuid);
        File dir = new File(sc.getInitParameter("crash.reports.dir") + "/" + appName + "/" + uuid);
        try {
            List<?> items = upload.parseRequest(request);
            Iterator<?> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                logger.debug("got item: name=" + item.getName() + ", fieldName=" + item.getFieldName() + ", isFormField=" + (item.isFormField() ? "YES" : "NO"));
                if (item.isFormField()) {
                    File dataFile = new File(dir, item.getFieldName());
                    cr.setValue(item.getFieldName(), item.getString());
                    writeStringToFile(item.getString(), dataFile);
                } else {
                    String fileName = item.getFieldName();
                    if (fileName.equals("log")) {
                        fileName = "log.tar.bz2"; // More convenient filename -- allows double-clicking of the file in the email.
                    }
                    File dataFile = new File(dir, fileName);
                    dataFile.getParentFile().mkdirs();
                    item.write(dataFile);
                    cr.setValue(fileName, dataFile);
                }
            }
            cr.setValue("remoteipaddr", request.getRemoteAddr());
            File ipAddr = new File(dir, "remoteipaddr");
            writeStringToFile(request.getRemoteAddr(), ipAddr);
        } catch(Exception e) {
            throw new IOException(e);
        }
        cr.sendEmail(sc);
    }
    private void writeStringToFile(String str, File file) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(str.getBytes("UTF-8"));
        fos.write("\n".getBytes("UTF-8"));
        fos.flush();
        fos.close();
    }
}
