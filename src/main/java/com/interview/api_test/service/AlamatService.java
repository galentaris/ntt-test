package com.interview.api_test.service;

import tools.jackson.databind.JsonNode;
import com.interview.api_test.dto.AlamatRequest;
import com.interview.api_test.dto.AlamatResponse;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Service
public class AlamatService {

    private final WebClient webClient;

    public AlamatService() {
        // Opsi 1: Menggunakan WebClient default tanpa bypass SSL
        // Asumsinya sertifikat SSL https://alamat.thecloudalert.com 
        // sudah di-import secara manual ke Java Keystore lokal.
        // this.webClient = WebClient.create("https://alamat.thecloudalert.com/api");

        // Opsi 2: Menggunakan WebClient dengan bypass SSL
        // CATATAN UNTUK REVIEWER:
        // Konfigurasi SSL bypass ditambahkan secara sengaja untuk 
        // memastikan project ini dapat langsung dijalankan (plug-and-play) oleh reviewer.
        // Hal ini dikarenakan endpoint https://alamat.thecloudalert.com mengalami issue SSLHandshakeException 
        // (PKIX path building failed) pada konfigurasi sertifikat Java bawaan.

        try {
            // Membuat konfigurasi SSL untuk Bypass
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

            // Inisialisasi WebClient
            this.webClient = WebClient.builder()
                    .baseUrl("https://alamat.thecloudalert.com/api")
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
                    
        } catch (SSLException e) {
            throw new RuntimeException("Gagal menginisialisasi konfigurasi SSL WebClient", e);
        }
    }

    public AlamatResponse cekKesesuaianAlamat(AlamatRequest request) {
        try {
            // API Provinsi
            JsonNode provincesResponse = webClient.get()
                    .uri("/provinsi/get/")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String idProvinsi = null;

            if (provincesResponse != null && provincesResponse.has("result")) {
                JsonNode resultProvinsi = provincesResponse.get("result");
                
                for (JsonNode prov : resultProvinsi) {
                    String namaProv = prov.get("text").asString();
                    if (namaProv.equalsIgnoreCase(request.getProvinsi())) {
                        idProvinsi = prov.get("id").asString();
                        break;
                    }
                }
            }

            // Jika provinsi dari request tidak ditemukan, otomatis abnormal
            if (idProvinsi == null) {
                return new AlamatResponse("0", "Tidak Sesuai");
            }

            // API KabKota dengan parameter ID Provinsi
            JsonNode citiesResponse = webClient.get()
                    .uri("/kabkota/get/?d_provinsi_id=" + idProvinsi)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            boolean isMatch = false;

            if (citiesResponse != null && citiesResponse.has("result")) {
                JsonNode resultKabKota = citiesResponse.get("result");
                
                for (JsonNode city : resultKabKota) {
                    String namaKota = city.get("text").asString();
                    if (namaKota.equalsIgnoreCase(request.getKabkota())) {
                        isMatch = true;
                        break;
                    }
                }
            }
            
            if (isMatch) {
                return new AlamatResponse("1", "Sesuai");
            } else {
                return new AlamatResponse("0", "Tidak Sesuai");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AlamatResponse("0", "Terjadi Error Eksternal");
        }
    }
}