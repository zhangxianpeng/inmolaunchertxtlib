package com.inmoglass.txtreaderlib.tasks;

import android.content.Context;
import android.graphics.Color;

import com.inmoglass.txtreaderlib.interfaces.ILoadListener;
import com.inmoglass.txtreaderlib.interfaces.ITxtTask;
import com.inmoglass.txtreaderlib.main.PaintContext;
import com.inmoglass.txtreaderlib.main.TxtConfig;
import com.inmoglass.txtreaderlib.main.TxtReaderContext;
import com.inmoglass.txtreaderlib.utils.ELogger;

/**
 * Created by bifan-wei
 * on 2017/11/27.
 */

public class DrawPrepareTask implements ITxtTask {
    private String tag = "DrawPrepareTask";

    @Override
    public void Run(ILoadListener callBack, TxtReaderContext readerContext) {
        callBack.onMessage("start do DrawPrepare");
        ELogger.log(tag, "do DrawPrepare");
        initPainContext(readerContext.context,readerContext.getPaintContext(), readerContext.getTxtConfig());
        readerContext.getPaintContext().textPaint.setColor(Color.WHITE);
        ITxtTask txtTask = new BitmapProduceTask();
        txtTask.Run(callBack, readerContext);
    }

    private void initPainContext(Context context,PaintContext paintContext, TxtConfig txtConfig) {
        TxtConfigInitTask.initPainContext(context,paintContext, txtConfig);
    }
}
