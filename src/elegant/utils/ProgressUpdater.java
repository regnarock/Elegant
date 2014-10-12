/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Wilgenbus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elegant.utils;

import javafx.scene.control.ProgressIndicator;
import lombok.extern.java.Log;
import org.eclipse.jgit.lib.BatchingProgressMonitor;

import java.util.concurrent.TimeUnit;

/**
 * Created by regnarock on 12/10/2014.
 */
@Log
public class ProgressUpdater extends BatchingProgressMonitor
{
    private ProgressIndicator progressIndicator;

    public ProgressUpdater(ProgressIndicator progressBar) {
        super();
        this.progressIndicator = progressBar;
        progressIndicator.setVisible(true);
        this.setDelayStart(300, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onUpdate(String s, int i) {

    }

    @Override
    protected void onEndTask(String s, int i) {
        progressIndicator.setProgress(1);
    }

    @Override
    protected void onUpdate(String s, int i, int i2, int i3) {
        progressIndicator.setProgress(i3 / 100);
    }

    @Override
    protected void onEndTask(String s, int i, int i2, int i3) {
        progressIndicator.setProgress(1);
    }
}
