package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.service;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.request.IncotermFormDataRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.response.IncotermCalculationResult;

public interface IncotermService {
    IncotermCalculationResult calculate(IncotermFormDataRequest req);
}