package com.example.demo.services;

import java.util.UUID;

// Evento pubblicato quando un documento deve passare dall'OCR
public record DocumentoDaElaborare(UUID id) {
}
