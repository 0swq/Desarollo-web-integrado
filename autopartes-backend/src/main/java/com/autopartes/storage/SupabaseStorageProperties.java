package com.autopartes.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.supabase")
public class SupabaseStorageProperties {
    private String url = "";
    private String key = "";
    private String bucket = "autopartes-imagenes";
    private long maxBytes = 10 * 1024 * 1024;
    private int timeoutMillis = 30000;
    private boolean createBucket = true;
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public long getMaxBytes() { return maxBytes; }
    public void setMaxBytes(long maxBytes) { this.maxBytes = maxBytes; }
    public int getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    public boolean isCreateBucket() { return createBucket; }
    public void setCreateBucket(boolean createBucket) { this.createBucket = createBucket; }
}
