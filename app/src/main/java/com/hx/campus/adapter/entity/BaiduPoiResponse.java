package com.hx.campus.adapter.entity;

import java.util.List;
//百度POI接口返回结果
public class BaiduPoiResponse {
    private int status;
    private String message;
    private List<BaiduPoiResult> results;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<BaiduPoiResult> getResults() { return results; }
    public void setResults(List<BaiduPoiResult> results) { this.results = results; }

    public static class BaiduPoiResult {
        private String name;
        private String address;
        private BaiduLocation location;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public BaiduLocation getLocation() { return location; }
        public void setLocation(BaiduLocation location) { this.location = location; }
    }

    public static class BaiduLocation {
        private double lat;
        private double lng;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }
}