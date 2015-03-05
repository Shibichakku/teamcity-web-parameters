package ru.mail.teamcity.web.parameters.manager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.teamcity.web.parameters.Authentication.WebServiceAuthentication;
import ru.mail.teamcity.web.parameters.data.Options;
import ru.mail.teamcity.web.parameters.parser.OptionParser;
import ru.mail.teamcity.web.parameters.parser.ParserFactory;

import java.io.*;
import java.net.Authenticator;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 29/06/14
 * Time: 17:34
 */
public class WebOptionsManagerImpl implements WebOptionsManager {

    private final static Logger LOG = Logger.getLogger(WebOptionsManagerImpl.class);

    @NotNull
    public Options read(@NotNull String url, @NotNull String format, @NotNull Map<String, String> errors) {
        Options options = null;
        if(url.startsWith("http")) {
            Authenticator.setDefault(new WebServiceAuthentication());
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url);
            LOG.debug(String.format("Requesting parameters from %s", url));
            try {
                HttpResponse response = httpClient.execute(getRequest);
                int code = response.getStatusLine().getStatusCode();
                if (code != 200) {
                    errors.put(String.format("Unexpected status code %d", code), response.getStatusLine().getReasonPhrase());
                    LOG.error("Method failed: " + response.getStatusLine());
                    return Options.empty();
                }
                options = parse(response.getEntity().getContent(), format, errors);
            } catch (IOException e) {
                errors.put("Failed to execute request", e.getMessage() != null ? e.getMessage() : e.getCause().getMessage());
                LOG.trace(e);
            } finally {
                getRequest.releaseConnection();
            }
        }else{
            LOG.debug(String.format("Requesting parameters from %s", url));
            try {
                File inputFile = new File(url);
                if (!inputFile.exists()) {
                    errors.put("File not exists :", url);
                    LOG.error("File not exists :" + url);
                    return Options.empty();
                }
                InputStream inputstream = new FileInputStream(inputFile);
                options = parse(inputstream , format, errors);
            } catch (Exception e) {
                errors.put("Failed to execute request", e.getMessage() != null ? e.getMessage() : e.getCause().getMessage());
                LOG.trace(e);
            } finally {

            }
        }
        return null == options ? Options.empty() : options;
    }

    @Nullable
    public OptionParser guess(@NotNull String url, @NotNull Map<String, String> errors) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url);
        LOG.debug(String.format("Requesting parameters from %s", url));
        try {
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.error("Method failed: " + response.getStatusLine());
                return null;
            }
            String content;
            try {
                content = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                errors.put("Failed to convert stream to string", e.getMessage());
                return null;
            }
            for (OptionParser parser : ParserFactory.registry) {
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

                Map<String, String> parseErrors = new HashMap<String, String>();
                if (null != parser.parse(stream, parseErrors)) {
                    return parser;
                }
            }
        } catch (IOException e) {
            errors.put("Failed to execute request", e.getMessage());
        } finally {
            getRequest.releaseConnection();
        }
        return null;
    }

    @Nullable
    private Options parse(@NotNull InputStream inputStream, @NotNull String format, @NotNull Map<String, String> errors) {
        OptionParser parser = ParserFactory.getOptionParser(format);
        if (null != parser) {
            return parser.parse(inputStream, errors);
        }
        return null;
    }
}
