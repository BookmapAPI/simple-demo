package com.bookmap.api.simple.demo.recorders;

import java.util.HashMap;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.MultiInstrumentListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Data Recorder All")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class DataRecorderAll extends DataRecorder implements MultiInstrumentListener {

    String currentAlias;
    HashMap<String, Integer> alias2id = new HashMap<>();
    String[] filter = new String[] { "ES", "CL" };

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
    }

    @Override
    protected String getFilename() {
        return "DataRecorderAll_" + System.currentTimeMillis() + ".txt";
    }

    @Override
    public void onCurrentInstrument(String alias) {
        currentAlias = alias;
    }

    private boolean acceptEvent() {
        for (String s : filter) {
            if (currentAlias.contains(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInstrumentAdded(InstrumentInfo info) {
        if (!acceptEvent()) {
            return;
        }
        if (alias2id.containsKey(currentAlias)) {
            writeObjects("InstrumentRemoved");
        } else {
            alias2id.put(currentAlias, alias2id.size());
        }
        addInstrument(currentAlias, info);
    }
    
    @Override
    protected void writeObjects(Object... objects) {
        if (acceptEvent()) {
            super.writeObjects(objects);
        }
    }
    
    @Override
    protected void appendFirst(final StringBuilder s) {
        Integer id = alias2id.get(currentAlias);
        s.append(getTimestamp()).append(delimiter).append(id);
    }
}
