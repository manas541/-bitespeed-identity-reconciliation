package com.bitespeed.identity.service;


import com.bitespeed.identity.dto.*;
import com.bitespeed.identity.entity.Contact;
import com.bitespeed.identity.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContactService {

    @Autowired
    private ContactRepository repository;

    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();


        List<Contact> matched =
                repository.findByEmailOrPhoneNumber(email, phone);


        if (matched.isEmpty()) {

            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phone);
            newContact.setLinkPrecedence("primary");
            newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());

            repository.save(newContact);

            return buildResponse(newContact);
        }



        Set<Contact> allRelated = getAllLinkedContacts(matched);


        Contact primary = allRelated.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .get();


        for (Contact contact : allRelated) {

            if (!contact.getId().equals(primary.getId())
                    && "primary".equals(contact.getLinkPrecedence())) {

                contact.setLinkPrecedence("secondary");
                contact.setLinkedId(primary.getId());
                contact.setUpdatedAt(LocalDateTime.now());

                repository.save(contact);
            }
        }


        boolean alreadyExists = allRelated.stream()
                .anyMatch(c ->
                        Objects.equals(c.getEmail(), email)
                                && Objects.equals(c.getPhoneNumber(), phone));

        if (!alreadyExists) {

            Contact secondary = new Contact();
            secondary.setEmail(email);
            secondary.setPhoneNumber(phone);
            secondary.setLinkedId(primary.getId());
            secondary.setLinkPrecedence("secondary");
            secondary.setCreatedAt(LocalDateTime.now());
            secondary.setUpdatedAt(LocalDateTime.now());

            repository.save(secondary);
        }

        return buildResponse(primary);
    }

    private IdentifyResponse buildResponse(Contact primary) {

        List<Contact> allContacts = new ArrayList<>();
        allContacts.add(primary);
        allContacts.addAll(repository.findByLinkedId(primary.getId()));

        Set<String> emails = new LinkedHashSet<>();
        Set<String> phones = new LinkedHashSet<>();
        List<Long> secondaryIds = new ArrayList<>();

        for (Contact c : allContacts) {

            if (c.getEmail() != null)
                emails.add(c.getEmail());

            if (c.getPhoneNumber() != null)
                phones.add(c.getPhoneNumber());

            if ("secondary".equals(c.getLinkPrecedence()))
                secondaryIds.add(c.getId());
        }

        IdentifyResponse response = new IdentifyResponse();
        IdentifyResponse.ContactResponse data =
                new IdentifyResponse.ContactResponse();

        data.setPrimaryContactId(primary.getId());
        data.setEmails(new ArrayList<>(emails));
        data.setPhoneNumbers(new ArrayList<>(phones));
        data.setSecondaryContactIds(secondaryIds);

        response.setContact(data);

        return response;
    }

    private Set<Contact> getAllLinkedContacts(List<Contact> matched) {

        Set<Contact> allRelated = new HashSet<>(matched);
        Queue<Contact> queue = new LinkedList<>(matched);

        while (!queue.isEmpty()) {

            Contact current = queue.poll();


            if (current.getLinkedId() != null) {
                repository.findById(current.getLinkedId())
                        .ifPresent(parent -> {
                            if (allRelated.add(parent)) {
                                queue.add(parent);
                            }
                        });
            }


            List<Contact> children =
                    repository.findByLinkedId(current.getId());

            for (Contact child : children) {
                if (allRelated.add(child)) {
                    queue.add(child);
                }
            }
        }

        return allRelated;
    }

}

