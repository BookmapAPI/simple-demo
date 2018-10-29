package com.bookmap.api.simple.demo.recorders;

import com.bookmap.api.simple.demo.utils.data.ExponentialSumBars;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.InitialState;

@Layer1SimpleAttachable
@Layer1StrategyName("Volume Recorder Exponential")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class VolumeRecorderExponential extends VolumeRecorder {
    private final int[] halfLifeBars = new int[] { 0, 4, 12, 36 };
    private ExponentialSumBars[] emaBuy = new ExponentialSumBars[halfLifeBars.length];
    private ExponentialSumBars[] emaSell = new ExponentialSumBars[halfLifeBars.length];
    private Object[] output = new Object[2 * halfLifeBars.length];

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        for (int i = 0; i < halfLifeBars.length; i++) {
            emaBuy[i] = new ExponentialSumBars(halfLifeBars[i]);
            emaSell[i] = new ExponentialSumBars(halfLifeBars[i]);
        }
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        for (int i = 0; i < halfLifeBars.length; i++) {
            emaBuy[i].onBar(bar.getVolumeBuy());
            emaSell[i].onBar(bar.getVolumeSell());

            output[2 * i] = emaBuy[i].getValueLong();
            output[2 * i + 1] = emaSell[i].getValueLong();
        }
        writeObjects(output);
    }

    @Override
    protected String getFilename() {
        return "VolumeExponential_" + System.currentTimeMillis() + ".txt";
    }
}
