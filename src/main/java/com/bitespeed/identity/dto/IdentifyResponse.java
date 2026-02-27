package com.bitespeed.identity.dto;

import lombok.Data;

import java.util.List;

@Data
public class IdentifyResponse {

    private ContactResponse contact;

    @Data
    public static class ContactResponse {

        private Long primaryContactId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Long> secondaryContactIds;

    }
}
