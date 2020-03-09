package com.serena.air.plugin.fod.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Proxy {
    private URI proxyUri;
    private String username;
    private String password;
    private String ntDomain;
    private String ntWorkstation;

    /**
     * Creates a Proxy object
     * @param
     */
    public Proxy(String uri, String username, String password, String ntDomain, String ntWorkstation) {
        try {

            this.proxyUri = (!uri.isEmpty()) ? new URI(uri) : null;
            this.username = (!username.isEmpty() ? username : null);
            this.password = (!password.isEmpty() ? password : null);
            this.ntDomain = (!ntDomain.isEmpty() ? ntDomain : null);
            this.ntWorkstation = (!ntWorkstation.isEmpty() ? ntWorkstation : null);

        } catch(URISyntaxException | ArrayIndexOutOfBoundsException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public URI getProxyUri() {
        return proxyUri;
    }

    public String getUsername() {
        return username;
    }
    public boolean hasUsername() { return username != null && !username.isEmpty(); }

    public String getPassword() {
        return password;
    }
    public boolean hasPassword() { return password != null && !password.isEmpty(); }

    public String getNTDomain() {
        return ntDomain;
    }
    public boolean hasNTDomain() { return ntDomain != null && !ntDomain.isEmpty(); }

    public String getNTWorkstation() {
        return ntWorkstation;
    }
    public boolean hasNTWorkstation() { return ntWorkstation!= null && !ntWorkstation.isEmpty(); }
}
