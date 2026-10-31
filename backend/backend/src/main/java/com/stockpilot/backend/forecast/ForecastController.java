package com.stockpilot.backend.forecast;

import com.stockpilot.backend.user.AppUser;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecasts")
@PreAuthorize("hasAnyRole('OWNER', 'STAFF')")
public class ForecastController {
    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) { this.forecastService = forecastService; }

    @GetMapping
    public List<ForecastResponse> all(@AuthenticationPrincipal AppUser actor) {
        return forecastService.all(actor);
    }

    @GetMapping("/{productId}")
    public ForecastResponse get(@PathVariable UUID productId, @AuthenticationPrincipal AppUser actor) {
        return forecastService.get(productId, actor);
    }
}
