package com.bookmap.sergey.custommodules;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.simplified.HistoricalDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("ATR Trailing Stop History")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AtrTrailingStopHist extends AtrTrailingStop implements HistoricalDataListener {
}
