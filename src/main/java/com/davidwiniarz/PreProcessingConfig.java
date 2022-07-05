package com.davidwiniarz;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@ToString
@Getter
@Setter
public class PreProcessingConfig {

    @ToString
    @Getter
    @Setter
    public static class Header {
        public String name;
        public String type;
        public String value;
    }

    @ToString
    @Getter
    @Setter
    public static class Query {
        public String name;
        public String type;
        public String value;
    }


    public String clientId;
    public ArrayList<Header> headers;
    public ArrayList<Query> query;


}


