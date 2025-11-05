package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.service.IncotermService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.request.IncotermFormDataRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.response.IncotermCalculationResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incoterms")
public class IncotermController {

    private final IncotermService service;

    @PostMapping("/calculate")
    public ResponseEntity<IncotermCalculationResult> calculate(
            @Valid @RequestBody IncotermFormDataRequest req
    ) {
        return ResponseEntity.ok(service.calculate(req));
    }
}