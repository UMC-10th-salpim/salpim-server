package salpim.umc10thsalpim.domain.recommendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.recommendation.service.RecommedationService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommendationController {


}
