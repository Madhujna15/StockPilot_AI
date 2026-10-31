package com.stockpilot.backend.invoice;

import com.stockpilot.backend.invoice.dto.OcrResponse;
import com.stockpilot.backend.invoice.dto.ReceiveInvoiceRequest;
import com.stockpilot.backend.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasRole('OWNER')")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) { this.invoiceService = invoiceService; }

    @PostMapping("/ocr")
    public OcrResponse read(@RequestPart("file") MultipartFile file) {
        return invoiceService.readInvoice(file);
    }

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.CREATED)
    public void receive(@Valid @RequestBody ReceiveInvoiceRequest request,
                        @AuthenticationPrincipal AppUser actor) {
        invoiceService.confirmReceipt(request, actor);
    }
}
