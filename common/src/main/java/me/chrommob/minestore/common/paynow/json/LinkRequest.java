package me.chrommob.minestore.common.paynow.json;

public class LinkRequest {
    public String ip;
    public String hostname;
    public String platform;
    public String version;

    public LinkRequest(String ip, String hostname, String platform, String version) {
        this.ip = ip;
        this.hostname = hostname;
        this.platform = platform;
        this.version = version;
    }

}