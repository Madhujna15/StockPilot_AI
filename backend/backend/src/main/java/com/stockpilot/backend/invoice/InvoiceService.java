package com.stockpilot.backend.invoice;

import com.stockpilot.backend.invoice.dto.OcrResponse;
import com.stockpilot.backend.invoice.dto.ReceiveInvoiceRequest;
import com.stockpilot.backend.product.MovementType;
import com.stockpilot.backend.product.Product;
import com.stockpilot.backend.product.ProductRepository;
import com.stockpilot.backend.product.StockMovement;
import com.stockpilot.backend.product.StockMovementRepository;
import com.stockpilot.backend.user.AppUser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvoiceService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern INVOICE_NO = Pattern.compile("(?im)\\b(?:invoice\\s*(?:no|number|#)?|bill\\s*(?:no|number|#))\\s*[:#.-]?\\s*([A-Z0-9][A-Z0-9/-]{2,})");
    private static final Pattern DATE = Pattern.compile("\\b(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}|\\d{4}-\\d{2}-\\d{2})\\b");

    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final InvoiceReceiptRepository receipts;
    private final String tesseractPath;

    public InvoiceService(ProductRepository products, StockMovementRepository movements,
                          InvoiceReceiptRepository receipts,
                          @Value("${app.ocr.tesseract-path:tesseract}") String tesseractPath) {
        this.products = products;
        this.movements = movements;
        this.receipts = receipts;
        this.tesseractPath = tesseractPath;
    }

    @Transactional(readOnly = true)
    public OcrResponse readInvoice(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an invoice file.");
        if (file.getSize() > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Invoice must be 5 MB or smaller.");
        String name = file.getOriginalFilename() == null ? "invoice" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!Set.of("jpg", "jpeg", "png", "pdf").contains(ext)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Upload a JPG, PNG, or PDF invoice.");
        }
        try {
            byte[] bytes = file.getBytes();
            String text = ext.equals("pdf") ? readPdf(bytes) : readImage(bytes);
            if (text.isBlank()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No readable text found. Try a clearer image or enter the invoice manually.");
            Matcher invoice = INVOICE_NO.matcher(text);
            Matcher date = DATE.matcher(text);
            return new OcrResponse(name, text, invoice.find() ? invoice.group(1).trim() : null,
                    date.find() ? date.group(1) : null,
                    "OCR may misread invoice values. Check every field before confirming stock receipt.");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The invoice file is damaged or unreadable.");
        }
    }

    private String readImage(byte[] bytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a readable image.");
        Path temp = Files.createTempFile("stockpilot-invoice-", ".png");
        try {
            ImageIO.write(image, "png", temp.toFile());
            return runTesseract(temp);
        } finally { Files.deleteIfExists(temp); }
    }

    private String readPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password-protected PDFs are not supported.");
            if (document.getNumberOfPages() == 0 || document.getNumberOfPages() > 5)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF must contain between 1 and 5 pages.");
            StringBuilder result = new StringBuilder();
            String embedded = new org.apache.pdfbox.text.PDFTextStripper().getText(document);
            if (embedded != null && embedded.replaceAll("\\s", "").length() > 20) return embedded;
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                Path temp = Files.createTempFile("stockpilot-invoice-", ".png");
                try {
                    ImageIO.write(renderer.renderImageWithDPI(page, 180), "png", temp.toFile());
                    result.append(runTesseract(temp)).append('\n');
                } finally { Files.deleteIfExists(temp); }
            }
            return result.toString();
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password-protected PDFs are not supported.");
        }
    }

    private String runTesseract(Path image) {
        try {
            Process process = new ProcessBuilder(tesseractPath, image.toString(), "stdout", "-l", "eng", "--psm", "6")
                    .redirectErrorStream(true).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "OCR took too long. Try a smaller or clearer invoice.");
            }
            byte[] output;
            try (var stream = process.getInputStream()) { output = stream.readAllBytes(); }
            if (process.exitValue() != 0) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "OCR could not run. Install Tesseract OCR and set TESSERACT_PATH to its executable.");
            return new String(output, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tesseract OCR is not installed or TESSERACT_PATH is invalid.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OCR was interrupted.");
        }
    }

    @Transactional
    public void confirmReceipt(ReceiveInvoiceRequest request, AppUser actor) {
        String reference = request.reference().trim();
        if (receipts.existsByBusiness_IdAndReferenceIgnoreCase(actor.getBusiness().getId(), reference))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This invoice reference was already received.");
        Set<java.util.UUID> seen = new HashSet<>();
        List<Product> lockedProducts = new ArrayList<>();
        for (var line : request.lines()) {
            if (line.productId() == null || line.quantity() == null || line.quantity().signum() <= 0 || line.quantity().scale() > 3)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each line needs a product and a positive quantity with at most 3 decimal places.");
            if (!seen.add(line.productId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each product can appear only once per invoice.");
            Product product = products.findActiveForUpdate(line.productId(), actor.getBusiness().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "A product was not found in your business."));
            lockedProducts.add(product);
        }
        InvoiceReceipt receipt = receipts.saveAndFlush(new InvoiceReceipt(actor.getBusiness(), actor, reference));
        for (int i = 0; i < request.lines().size(); i++) {
            var line = request.lines().get(i);
            Product product = lockedProducts.get(i);
            product.applyStockDelta(line.quantity());
            movements.save(new StockMovement(product, actor, MovementType.PURCHASE, line.quantity(), "Invoice " + receipt.getReference()));
        }
    }
}
