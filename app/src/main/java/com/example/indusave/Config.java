package com.example.indusave;

public class Config {
    public static final String IP_SERVIDOR = "192.168.0.11";
    public static final String PUERTO = "5986";
    public static final String DB_NAME = "indusave_db";
    public static final String USUARIO = "admin";
    public static final String PASS = "12345";

    public static final String URL_COUCHDB = "http://" + IP_SERVIDOR + ":" + PUERTO + "/" + DB_NAME + "/";
}