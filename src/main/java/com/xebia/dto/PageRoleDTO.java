package com.xebia.dto;

import java.util.List;

/**
 * Created by Pgupta on 02-09-2016.
 */
public class PageRoleDTO {

    private String name;

    private String url;

    private List<String> userRole;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getUserRole() {
        return userRole;
    }

    public void setUserRole(List<String> userRole) {
        this.userRole = userRole;
    }
}
