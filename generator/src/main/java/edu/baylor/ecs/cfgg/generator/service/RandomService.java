package edu.baylor.ecs.cfgg.generator.service;

import org.springframework.stereotype.Service;

@Service
public class RandomService {

    private final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String randomString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Math.random()*ALPHA_NUMERIC_STRING.length());
        return stringBuilder.toString();
    }
}