package com.interview.api_test.controller;

import com.interview.api_test.dto.AlamatRequest;
import com.interview.api_test.dto.AlamatResponse;
import com.interview.api_test.service.AlamatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlamatController {

    private final AlamatService alamatService;

    @Autowired
    public AlamatController(AlamatService alamatService) {
        this.alamatService = alamatService;
    }

    @PostMapping("/cekAlamat")
    public ResponseEntity<AlamatResponse> cekAlamat(@RequestBody AlamatRequest request) {
        AlamatResponse response = alamatService.cekKesesuaianAlamat(request);
        
        return ResponseEntity.ok(response);
    }
}