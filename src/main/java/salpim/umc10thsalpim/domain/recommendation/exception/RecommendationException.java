package salpim.umc10thsalpim.domain.recommendation.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class RecommendationException extends ProjectException {
  public RecommendationException(BaseErrorCode code) {
    super(code);
  }
}
