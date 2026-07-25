package salpim.umc10thsalpim.global.util;

import salpim.umc10thsalpim.domain.map.exception.MapException;
import salpim.umc10thsalpim.domain.map.exception.code.MapErrorCode;

import java.math.BigDecimal;

public class GeoUtils {

    private GeoUtils() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    public static void validateCoordinates(BigDecimal lat, BigDecimal lon) {
        if (lat == null || lon == null) {
            throw new MapException(MapErrorCode.INVALID_COORDINATES);
        }
        double latVal = lat.doubleValue();
        double lonVal = lon.doubleValue();

        if (latVal < -90.0 || latVal > 90.0 || lonVal < -180.0 || lonVal > 180.0) {
            throw new MapException(MapErrorCode.INVALID_COORDINATES);
        }
    }

    public static String calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return "거리 정보 없음";
        }

        double earthRadius = 6371.0; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c; // 결과는 km 단위

        if (distance < 1.0) {
            return (int) (distance * 1000) + "m";
        }
        return String.format("%.1fkm", distance);
    }
}
