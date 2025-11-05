package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.application.service;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.service.IncotermService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.request.IncotermFormDataRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.response.IncotermCalculationResult;

import java.util.List;
import java.util.Map;

@Service
public class IncotermServiceImpl implements IncotermService {
    @Override
    public IncotermCalculationResult calculate(IncotermFormDataRequest req) {
        double oceanFreight = getOceanFreight(req);
        double fuelSurcharge = oceanFreight * 0.12;
        double baf = oceanFreight * 0.05;

        double oph = 400 + (req.getCargoType().equals("dangerous") ? 150 : 0);
        double dph = 420;
        double containerHandling = req.getCargoType().equals("container") ? 180 : 0;
        double stevedoring = 160;

        double marineInsurance = req.getCargoValue() * 0.0035;
        double cargoInsurance = req.isInsurance() ? req.getCargoValue() * 0.0015 : 0;
        double warRisk = req.getCargoType().equals("dangerous") ? oceanFreight * 0.02 : oceanFreight * 0.005;

        double exportClear = 120;
        double importClear = 140;
        double importDuties = req.getCargoValue() * 0.05;
        double taxes = req.getCargoValue() * 0.10;

        double bl = 60;
        double coo = 45;
        double insp = req.getSpecialRequirements() != null && !req.getSpecialRequirements().isBlank() ? 80 : 0;
        double customsDocs = 35;

        double warehousing = 0;
        double demurrage = 0;
        double detention = 0;
        double currencyAdj = (oceanFreight + fuelSurcharge + baf) * 0.01;

        IncotermOption cif = buildOption(
                "CIF", "Cost, Insurance & Freight",
                "El vendedor cubre flete y seguro hasta puerto de destino. Riesgo se transfiere al cargar a bordo.",
                "A bordo en puerto de origen",
                req,
                buildBreakdown(oceanFreight, fuelSurcharge, baf, oph, 0, containerHandling, stevedoring,
                        marineInsurance, cargoInsurance, warRisk, exportClear, 0, 0, 0, bl, coo, insp, customsDocs,
                        0, 0, 0, currencyAdj,
                        List.of("oceanFreight","fuelSurcharge","bunkerAdjustmentFactor","originPortHandling",
                                "containerHandling","stevedoring","marineInsurance","cargoInsurance","warRiskInsurance",
                                "exportCustomsClearance","billOfLading","certificateOfOrigin","inspectionCertificate","customsDocumentation","currencyAdjustment"),
                        List.of("destinationPortHandling","importCustomsClearance","importDuties","taxes")
                ),
                List.of("Empaque y despacho exportación","Flete internacional","Seguro marítimo hasta destino"),
                List.of("Descarga y formalidades de importación","Derechos e impuestos"),
                List.of("Compradores principiantes","Pagos prepago/LC","Carga de valor medio/alto"),
                List.of("Seguro limitado (ampliar si es necesario)")
        );

        IncotermOption fob = buildOption(
                "FOB", "Free On Board",
                "El vendedor entrega en el puerto de origen; el comprador asume flete y riesgos desde el embarque.",
                "A bordo en puerto de origen",
                req,
                buildBreakdown(
                        0, 0, 0, oph, 0, containerHandling, stevedoring,
                        0, 0, 0, exportClear, 0, 0, 0, bl, coo, insp, customsDocs,
                        0, 0, 0, 0,
                        List.of("originPortHandling","containerHandling","stevedoring","exportCustomsClearance","billOfLading","certificateOfOrigin","inspectionCertificate","customsDocumentation"),
                        List.of("oceanFreight","fuelSurcharge","bunkerAdjustmentFactor","destinationPortHandling","importCustomsClearance","importDuties","taxes","currencyAdjustment","marineInsurance","cargoInsurance","warRiskInsurance")
                ),
                List.of("originPortHandling","containerHandling","stevedoring","exportCustomsClearance","billOfLading","certificateOfOrigin","inspectionCertificate","customsDocumentation"),
                List.of("oceanFreight","fuelSurcharge","bunkerAdjustmentFactor","destinationPortHandling","importCustomsClearance","importDuties","taxes","currencyAdjustment","marineInsurance","cargoInsurance","warRiskInsurance"),
                List.of("Compradores con experiencia","Control de naviera y costos"),
                List.of("Comprador gestiona seguro y riesgos desde el embarque")
        );

        IncotermOption dap = buildOption(
                "DAP", "Delivered At Place",
                "El vendedor entrega en lugar de destino (no incluye importación).",
                "A la entrega en destino (antes de aduana)",
                req,
                buildBreakdown(
                        oceanFreight, fuelSurcharge, baf, oph, dph, containerHandling, stevedoring,
                        marineInsurance, cargoInsurance, warRisk, exportClear, 0, 0, 0, bl, coo, insp, customsDocs,
                        warehousing, demurrage, detention, currencyAdj,
                        List.of("oceanFreight","fuelSurcharge","bunkerAdjustmentFactor","originPortHandling","destinationPortHandling","containerHandling","stevedoring","marineInsurance","cargoInsurance","warRiskInsurance","exportCustomsClearance","billOfLading","certificateOfOrigin","inspectionCertificate","customsDocumentation","warehousing","demurrage","detention","currencyAdjustment"),
                        List.of("importCustomsClearance","importDuties","taxes")
                ),
                List.of("oceanFreight","fuelSurcharge","bunkerAdjustmentFactor","originPortHandling","destinationPortHandling","containerHandling","stevedoring","marineInsurance","cargoInsurance","warRiskInsurance","exportCustomsClearance","billOfLading","certificateOfOrigin","inspectionCertificate","customsDocumentation","warehousing","demurrage","detention","currencyAdjustment"),
                List.of("importCustomsClearance","importDuties","taxes"),
                List.of("Quien busca simplicidad logística en destino"),
                List.of("No incluye trámites ni pagos de importación")
        );

        Map<String,Integer> baseScores = Map.of(
                "CIF", switch (req.getExperienceLevel()) { case "principiante" -> 90; case "intermedio" -> 75; default -> 60; },
                "FOB", switch (req.getExperienceLevel()) { case "experto" -> 90; case "intermedio" -> 70; default -> 50; },
                "DAP", 80
        );
        cif.setRecommendationScore(baseScores.get("CIF"));
        fob.setRecommendationScore(baseScores.get("FOB"));
        dap.setRecommendationScore(baseScores.get("DAP"));

        if (req.isInsurance()) {
            cif.setRecommendationScore(
                Math.min(100, cif.getRecommendationScore() + 5)
            );
        }
        if ("container".equals(req.getCargoType())) {
            fob.setRecommendationScore(fob.getRecommendationScore() + 3);
        }
        if ("prepago".equals(req.getPaymentTerms())) {
            cif.setRecommendationScore(cif.getRecommendationScore() + 3);
        }

        IncotermOption[] options = {cif, fob, dap};
        IncotermOption lowest = options[0], highest = options[0];
        for (IncotermOption op : options) {
            if (op.getCostBreakdown().getTotal() < lowest.getCostBreakdown().getTotal()) {
                lowest = op;
            }
            if (op.getCostBreakdown().getTotal() > highest.getCostBreakdown().getTotal()) {
                highest = op;
            }
        }

        MarketConditions market = MarketConditions.builder()
                .fuelPrices("Alta (Q4)")
                .exchangeRates("USD estable")
                .portCongestion("Media")
                .seasonalDemand("Alta (campaña)")
                .build();

        String routeRisk = req.getCargoType().equals("dangerous") ? "Alto" : "Medio";
        RouteDetails route = RouteDetails.builder()
                .distance(req.getDistance())
                .estimatedTime(estimateTime(req.getDistance()))
                .routeComplexity(routeRisk.equals("Alto") ? "Alta" : "Media")
                .riskLevel(routeRisk)
                .seasonalFactors(List.of("Monzones en tramo índico","Vientos alisios variables"))
                .build();

        List<String> warnings = routeRisk.equals("Alto")
                ? List.of("Mercancía peligrosa: revisar requisitos y seguros adicionales")
                : List.of();

        IncotermOption recommended = cif;
        for (IncotermOption op : options) {
            if (op.getRecommendationScore() > recommended.getRecommendationScore()) {
                recommended = op;
            }
        }

        CostComparison cmp = CostComparison.builder()
                .lowestCost(lowest.getCode())
                .highestCost(highest.getCode())
                .costDifference(highest.getCostBreakdown().getTotal() - lowest.getCostBreakdown().getTotal())
                .build();

        return IncotermCalculationResult.builder()
                .marketConditions(market)
                .recommendedIncoterm(recommended)
                .alternatives(List.of(cif.getCode().equals(recommended.getCode()) ? fob : cif, dap))
                .costComparison(cmp)
                .routeDetails(route)
                .warnings(warnings)
                .build();
    }

    private static double getOceanFreight(IncotermFormDataRequest req) {
        double weightTons = req.getCargoWeight() / 1000.0;
        double chargeableTons = Math.max(weightTons, req.getCargoVolume() * 0.167);
        double baseRatePerNmTon = switch (req.getCargoType()) {
            case "container" -> 14.0 / 1000.0;
            case "bulk" -> 10.0 / 1000.0;
            case "liquid" -> 11.0 / 1000.0;
            case "refrigerated" -> 15.0 / 1000.0;
            case "dangerous" -> 18.0 / 1000.0;
            default -> 12.0 / 1000.0;
        };
        return req.getDistance() * chargeableTons * baseRatePerNmTon;
    }

    private String estimateTime(double distanceNm) {
        double hours = distanceNm / 14.0;
        int d = (int)(hours / 24);
        int h = (int)(hours % 24);
        int m = (int)Math.round((hours - Math.floor(hours)) * 60);
        return String.format("%dd %dh %dm", d, h, m);
    }

    private IncotermOption buildOption(String code, String name, String desc, String riskPoint,
        IncotermFormDataRequest req, CostBreakdown breakdown,
        List<String> sellerResp, List<String> buyerResp,
        List<String> suitableFor, List<String> considerations) {
        return IncotermOption.builder()
                .code(code).name(name).description(desc)
                .riskTransferPoint(riskPoint)
                .sellerResponsibilities(sellerResp)
                .buyerResponsibilities(buyerResp)
                .suitableFor(suitableFor)
                .considerations(considerations)
                .costBreakdown(breakdown)
                .build();
    }

    private CostBreakdown buildBreakdown(
            double oceanFreight, double fuelSurcharge, double baf, double oph, double dph, double containerHandling,
            double stevedoring, double marineInsurance, double cargoInsurance, double warRisk, double exportClear,
            double importClear, double importDuties, double taxes, double bl, double coo, double insp, double customsDocs,
            double warehousing, double demurrage, double detention, double currencyAdj,
            List<String> sellerPays, List<String> buyerPays) {

        CostBreakdown b = CostBreakdown.builder()
                .oceanFreight(oceanFreight).fuelSurcharge(fuelSurcharge).bunkerAdjustmentFactor(baf)
                .originPortHandling(oph).destinationPortHandling(dph).containerHandling(containerHandling)
                .stevedoring(stevedoring)
                .marineInsurance(marineInsurance).cargoInsurance(cargoInsurance).warRiskInsurance(warRisk)
                .exportCustomsClearance(exportClear).importCustomsClearance(importClear)
                .importDuties(importDuties).taxes(taxes)
                .billOfLading(bl).certificateOfOrigin(coo).inspectionCertificate(insp).customsDocumentation(customsDocs)
                .warehousing(warehousing).demurrage(demurrage).detention(detention)
                .currencyAdjustment(currencyAdj)
                .build();

        double seller = 0, buyer = 0;
        Map<String,Double> map = Map.ofEntries(
                Map.entry("oceanFreight", b.getOceanFreight()),
                Map.entry("fuelSurcharge", b.getFuelSurcharge()),
                Map.entry("bunkerAdjustmentFactor", b.getBunkerAdjustmentFactor()),
                Map.entry("originPortHandling", b.getOriginPortHandling()),
                Map.entry("destinationPortHandling", b.getDestinationPortHandling()),
                Map.entry("containerHandling", b.getContainerHandling()),
                Map.entry("stevedoring", b.getStevedoring()),
                Map.entry("marineInsurance", b.getMarineInsurance()),
                Map.entry("cargoInsurance", b.getCargoInsurance()),
                Map.entry("warRiskInsurance", b.getWarRiskInsurance()),
                Map.entry("exportCustomsClearance", b.getExportCustomsClearance()),
                Map.entry("importCustomsClearance", b.getImportCustomsClearance()),
                Map.entry("importDuties", b.getImportDuties()),
                Map.entry("taxes", b.getTaxes()),
                Map.entry("billOfLading", b.getBillOfLading()),
                Map.entry("certificateOfOrigin", b.getCertificateOfOrigin()),
                Map.entry("inspectionCertificate", b.getInspectionCertificate()),
                Map.entry("customsDocumentation", b.getCustomsDocumentation()),
                Map.entry("warehousing", b.getWarehousing()),
                Map.entry("demurrage", b.getDemurrage()),
                Map.entry("detention", b.getDetention()),
                Map.entry("currencyAdjustment", b.getCurrencyAdjustment())
        );
        for (var k : sellerPays) seller += map.getOrDefault(k, 0.0);
        for (var k : buyerPays)  buyer  += map.getOrDefault(k, 0.0);

        b.setSellerCosts(seller);
        b.setBuyerCosts(buyer);
        b.setTotal(seller + buyer);
        return b;
    }
}
