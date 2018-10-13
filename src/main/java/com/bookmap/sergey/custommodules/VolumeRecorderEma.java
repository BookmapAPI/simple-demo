package com.bookmap.sergey.custommodules;

import com.bookmap.sergey.custommodules.utils.EmaBars;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BarDataListener;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.Intervals;

@Layer1SimpleAttachable
@Layer1StrategyName("Volume Recorder Ema")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class VolumeRecorderEma extends DataRecorderBase implements CustomModule, BarDataListener {
    private final double[] halfLifeBars = new double[] {0, 4, 12, 36};
    private EmaBars[] emaBuy = new EmaBars[halfLifeBars.length];
    private EmaBars[] emaSell = new EmaBars[halfLifeBars.length];
    private Object[] output = new Object[2 * halfLifeBars.length];

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api) {
        for (int i = 0; i < halfLifeBars.length; i++) {
            emaBuy[i] = new EmaBars(halfLifeBars[i]);
            emaSell[i] = new EmaBars(halfLifeBars[i]);
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
    public long getBarInterval() {
        return Intervals.INTERVAL_1_SECOND;
    }

    @Override
    protected String getFilename() {
        return "VolumeEma_" + System.currentTimeMillis() + ".txt";
    }
}
