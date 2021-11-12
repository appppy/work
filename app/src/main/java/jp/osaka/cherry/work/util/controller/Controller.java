package jp.osaka.cherry.work.util.controller;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 制御
 */
public class Controller implements IWorker {

    /**
     * 一覧
     */
    private final Collection<IWorker> mCollection = new ArrayList<>();

    /**
     * 登録
     *
     * @param worker 項目
     */
    public void register(IWorker worker) {
        mCollection.add(worker);
    }

    /**
     * 解除
     */
    public void unregisterAll() {
        mCollection.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BaseCommand command) {
        for(IWorker worker : mCollection) {
            worker.start(command);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        for(IWorker worker : mCollection) {
            worker.stop();
        }
    }
}
