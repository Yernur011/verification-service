package com.diploma.verivicationdipllom.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FaceDataDTO {
    private double[] embedding;
    private FacialArea facial_area = new FacialArea();
    private double face_confidence;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FacialArea {
        private int x;
        private int y;
        private int w;
        private int h;
        private int[] left_eye;
        private int[] right_eye;
    }
}
