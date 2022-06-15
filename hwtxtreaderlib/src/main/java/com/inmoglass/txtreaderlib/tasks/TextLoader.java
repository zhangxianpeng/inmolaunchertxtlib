package com.inmoglass.txtreaderlib.tasks;

import com.inmoglass.txtreaderlib.interfaces.IChapter;
import com.inmoglass.txtreaderlib.interfaces.ILoadListener;
import com.inmoglass.txtreaderlib.interfaces.IParagraphData;
import com.inmoglass.txtreaderlib.interfaces.ITxtTask;
import com.inmoglass.txtreaderlib.main.ParagraphData;
import com.inmoglass.txtreaderlib.main.TxtReaderContext;
import com.inmoglass.txtreaderlib.utils.ELogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bifan-wei
 * on 2018/1/28.
 */

public class TextLoader {
    private final String tag = "FileDataLoadTask";

    public void load(String text, TxtReaderContext readerContext, ILoadListener callBack) {
        IParagraphData paragraphData = new ParagraphData();
        List<IChapter> chapter = new ArrayList<>();
        callBack.onMessage("start read text");
        ELogger.log(tag, "start read text");
        paragraphData.addParagraph(text + "");
        readerContext.setParagraphData(paragraphData);
        readerContext.setChapters(chapter);
        ITxtTask txtTask = new TxtConfigInitTask();
        txtTask.Run(callBack, readerContext);
    }
}
