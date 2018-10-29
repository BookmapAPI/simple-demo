package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;

import com.bookmap.api.simple.demo.utils.data.MovingAverage;

import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.InitialState;

public class MovingVWAP extends MovingAveragePrice {
    Indicator indicatorVwap;
    MovingAverage maVwap;
    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        super.initialize(alias, info, api, initialState);
        indicatorVwap = api.registerIndicator("Moving VWAP", GraphType.PRIMARY);
        indicatorVwap.setColor(Color.PINK);
    }

}
