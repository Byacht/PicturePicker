package com.byacht.picturepicker.bean;

import java.util.Map;

/**
 * Created by dn on 2017/10/1.
 */

public class SelectedPicturesEvent {
    public Map<Integer, String> selectedPicturesMap;

    public SelectedPicturesEvent(Map<Integer, String> selectedPicturesMap) {
        this.selectedPicturesMap = selectedPicturesMap;
    }
}
