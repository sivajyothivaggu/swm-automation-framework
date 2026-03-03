package com.swm.api.payloads;

public class RoutePayload {
    private String routeName;
    private String startPoint;
    private String endPoint;
    
    public RoutePayload(String routeName, String startPoint, String endPoint) {
        this.routeName = routeName;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }
    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }
}
