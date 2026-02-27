package com.bitespeed.identity.controller;

import com.bitespeed.identity.dto.*;
import com.bitespeed.identity.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class IdentifyController {

    @Autowired
    private ContactService service;

    @PostMapping("/identify")
    public IdentifyResponse identify(
            @RequestBody IdentifyRequest request) {

        return service.identify(request);
    }
}