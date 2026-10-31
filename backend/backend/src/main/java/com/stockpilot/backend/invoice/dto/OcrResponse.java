package com.stockpilot.backend.invoice.dto;

public record OcrResponse(String fileName, String extractedText, String suggestedInvoiceNumber,
                          String suggestedDate, String reviewRequired) { }
